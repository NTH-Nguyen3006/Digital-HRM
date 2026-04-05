package com.company.hrm.module.offboarding.dto;

import java.time.LocalDateTime;

public record OffboardingHistoryResponse(
        Long offboardingHistoryId,
        String fromStatus,
        String toStatus,
        String actionCode,
        String actionNote,
        String changedByUsername,
        LocalDateTime changedAt
) {
}
