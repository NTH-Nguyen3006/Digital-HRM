package com.company.hrm.module.leave.dto;

import java.time.LocalDateTime;

public record LeaveRequestHistoryResponse(
        Long leaveRequestHistoryId,
        String fromStatus,
        String toStatus,
        String actionCode,
        String actionNote,
        LocalDateTime changedAt,
        String changedByUsername
) {
}
