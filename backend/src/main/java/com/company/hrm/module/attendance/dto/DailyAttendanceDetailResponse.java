package com.company.hrm.module.attendance.dto;

import java.util.List;

public record DailyAttendanceDetailResponse(
        DailyAttendanceListItemResponse summary,
        List<AttendanceLogResponse> rawLogs,
        List<AttendanceAdjustmentListItemResponse> adjustmentRequests,
        List<OvertimeListItemResponse> overtimeRequests
) {
}
