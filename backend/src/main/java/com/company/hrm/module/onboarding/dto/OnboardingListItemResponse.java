package com.company.hrm.module.onboarding.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record OnboardingListItemResponse(
        Long onboardingId,
        String onboardingCode,
        String fullName,
        String personalEmail,
        String personalPhone,
        String orgUnitCode,
        String orgUnitName,
        String jobTitleCode,
        String jobTitleName,
        LocalDate plannedStartDate,
        String status,
        Long linkedEmployeeId,
        UUID linkedUserId,
        Long firstContractId,
        LocalDateTime orientationConfirmedAt,
        String orientationConfirmedByUsername,
        LocalDateTime completedAt,
        String completedByUsername
) {
}
