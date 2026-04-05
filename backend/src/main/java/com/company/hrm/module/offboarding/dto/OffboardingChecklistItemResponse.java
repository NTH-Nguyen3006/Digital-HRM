package com.company.hrm.module.offboarding.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OffboardingChecklistItemResponse(
        Long offboardingChecklistItemId,
        String itemType,
        String itemName,
        String ownerRoleCode,
        LocalDate dueDate,
        String status,
        String note,
        LocalDateTime completedAt
) {
}
