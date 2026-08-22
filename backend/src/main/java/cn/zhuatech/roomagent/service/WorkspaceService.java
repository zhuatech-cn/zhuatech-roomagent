/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.roomagent.service;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkspaceService {
    public RunResult run(RunRequest request) {
        List<String> warnings = new ArrayList<>();
        if (!request.humanReview()) warnings.add("未启用人工复核，结果不能进入正式业务流程");
        if (request.confidenceFloor() < 70) warnings.add("置信度阈值低于建议值 70，需扩大人工抽检范围");
        if (request.context() == null || request.context().isBlank()) warnings.add("缺少补充上下文，本次仅按基础规则处理");

        List<Insight> insights = List.of(
            new Insight("事实", "容量 16 人，满足 12 人会议", 94),
            new Insight("关注", "视频会议与双屏设备状态正常", 82),
            new Insight("边界", "核心参会人日程未发现冲突", 76)
        );
        List<Action> actions = List.of(
            new Action("预订浦江厅 13:45–16:00", "业务负责人", "今天"),
            new Action("通知行政准备访客席位", "审核人员", "本周"),
            new Action("会前一天再次检查设备", "系统管理员", "复核后")
        );
        Map<String, Object> providerPayload = new LinkedHashMap<>();
        providerPayload.put("subject", request.subject());
        providerPayload.put("scenario", request.scenario());
        providerPayload.put("context", request.context());
        providerPayload.put("confidenceFloor", request.confidenceFloor());
        providerPayload.put("provider", "deepseek-compatible");
        providerPayload.put("model", "deepseek-chat");

        String status = request.humanReview() ? "REVIEW_READY" : "HUMAN_REVIEW_REQUIRED";
        return new RunResult(status, "LOW", "浦江厅容量与视频会议设备满足需求，且与参会人日程无冲突。建议预留会前 15 分钟用于设备检查。", insights, actions,
            List.copyOf(warnings), providerPayload, "LOCAL_DEMO_PIPELINE", OffsetDateTime.now());
    }

    public record RunRequest(
        @NotBlank String subject,
        @NotBlank String scenario,
        @Min(0) @Max(100) int confidenceFloor,
        boolean humanReview,
        @Size(max = 1200) String context
    ) {}

    public record Insight(String type, String content, int confidence) {}
    public record Action(String task, String ownerRole, String dueHint) {}
    public record RunResult(String status, String riskLevel, String summary, List<Insight> insights,
                            List<Action> actions, List<String> warnings, Map<String, Object> providerPayload,
                            String executionMode, OffsetDateTime generatedAt) {}
}
