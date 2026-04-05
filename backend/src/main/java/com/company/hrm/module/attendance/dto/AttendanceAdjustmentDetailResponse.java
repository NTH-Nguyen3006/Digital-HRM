package com.company.hrm.module.attendance.dto;

import java.util.List;

public record AttendanceAdjustmentDetailResponse(
        AttendanceAdjustmentListItemResponse request,
        List<AttendanceAdjustmentHistoryResponse> history
) {
}
