package com.company.hrm.module.onboarding.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OnboardingChecklistResponse(
        Long onboardingChecklistId,
        String itemCode,
        String itemName,
        boolean required,
        boolean completed,
        LocalDate dueDate,
        LocalDateTime completedAt,
        String completedByUsername,
        String note
) {
}
