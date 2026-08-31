/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.roomagent.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/** 企业会议室预订治理，避免资源冲突并支持访客、安全和无障碍要求。 */
@Service
public class MeetingRoomGovernanceService {
    public Decision govern(Request request) {
        List<String> blockers = new ArrayList<>();
        if (!request.endAt().isAfter(request.startAt())) blockers.add("会议结束时间必须晚于开始时间");
        if (request.attendeeCount() > request.roomCapacity()) blockers.add("会议室容量不足");
        if (request.conflictingBookingCount() > 0) blockers.add("存在重叠预订");
        if (request.accessibilityRequired() && !request.accessibilityAvailable()) blockers.add("无障碍设施不满足需求");
        if (request.videoRequired() && !request.videoAvailable()) blockers.add("视频会议设备不可用");
        if (request.externalVisitorCount() > 0 && !request.visitorApproved()) blockers.add("外部访客尚未完成审批");
        if (!request.organizerAssigned()) blockers.add("缺少会议组织者");
        boolean bookingAllowed = blockers.isEmpty();
        String route = bookingAllowed ? "BOOKING_CONFIRMED"
                : request.conflictingBookingCount() > 0 ? "ALTERNATIVE_ROOM_REQUIRED"
                : "BOOKING_REVIEW";
        OffsetDateTime noShowReleaseAt = request.startAt().plusMinutes(15);
        List<String> controls = new ArrayList<>(blockers);
        if (bookingAllowed) controls.add("会前 15 分钟检查设备并保留签到状态");
        controls.add("开始后 15 分钟无人签到则自动释放资源");
        if (request.externalVisitorCount() > 0) controls.add("同步访客名单、门禁时段和接待责任人");
        return new Decision(request.bookingNo(), route, bookingAllowed, noShowReleaseAt,
                List.copyOf(blockers), List.copyOf(controls));
    }

    public record Request(@NotBlank String bookingNo, @Min(1) int attendeeCount,
                          @Min(1) int roomCapacity, @Min(0) int conflictingBookingCount,
                          boolean accessibilityRequired, boolean accessibilityAvailable,
                          boolean videoRequired, boolean videoAvailable,
                          @Min(0) int externalVisitorCount, boolean visitorApproved,
                          boolean organizerAssigned, @NotNull OffsetDateTime startAt,
                          @NotNull OffsetDateTime endAt) {}

    public record Decision(String bookingNo, String route, boolean bookingAllowed,
                           OffsetDateTime noShowReleaseAt, List<String> blockers,
                           List<String> controls) {}
}
