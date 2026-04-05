package com.company.hrm.module.attendance.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttendanceAdjustmentHistoryResponse(
        Long adjustmentHistoryId,
        String fromStatus,
        String toStatus,
        String actionCode,
        String actionNote,
        LocalDateTime changedAt,
        UUID changedBy,
        String snapshotJson
) {
}
