/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.roomagent;

import cn.zhuatech.roomagent.service.MeetingRoomGovernanceService;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MeetingRoomGovernanceServiceTests {
    private final MeetingRoomGovernanceService service = new MeetingRoomGovernanceService();

    @Test void rejectsConflictingUndersizedRoom() {
        var start = OffsetDateTime.parse("2026-09-01T10:00:00+08:00");
        var result = service.govern(new MeetingRoomGovernanceService.Request(
                "ROOM-01", 20, 12, 1, false, true, true, true, 0, true, true,
                start, start.plusHours(1)));
        assertThat(result.route()).isEqualTo("ALTERNATIVE_ROOM_REQUIRED");
        assertThat(result.blockers()).hasSize(2);
    }

    @Test void confirmsGovernedVisitorBooking() {
        var start = OffsetDateTime.parse("2026-09-01T10:00:00+08:00");
        var result = service.govern(new MeetingRoomGovernanceService.Request(
                "ROOM-02", 8, 12, 0, true, true, true, true, 2, true, true,
                start, start.plusHours(1)));
        assertThat(result.route()).isEqualTo("BOOKING_CONFIRMED");
        assertThat(result.noShowReleaseAt()).isEqualTo(start.plusMinutes(15));
    }
}
