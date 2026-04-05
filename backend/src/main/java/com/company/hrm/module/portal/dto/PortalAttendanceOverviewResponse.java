package com.company.hrm.module.portal.dto;

import com.company.hrm.module.attendance.dto.AttendanceAdjustmentListItemResponse;
import com.company.hrm.module.attendance.dto.AttendanceLogResponse;
import com.company.hrm.module.attendance.dto.OvertimeListItemResponse;
import java.util.List;

public record PortalAttendanceOverviewResponse(
        List<AttendanceLogResponse> recentLogs,
        List<AttendanceAdjustmentListItemResponse> adjustmentRequests,
        List<OvertimeListItemResponse> overtimeRequests
) {
}
