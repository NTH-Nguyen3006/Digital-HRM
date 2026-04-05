package com.company.hrm.module.onboarding.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OnboardingDetailResponse(
        Long onboardingId,
        String onboardingCode,
        String fullName,
        String personalEmail,
        String personalPhone,
        String genderCode,
        LocalDate dateOfBirth,
        LocalDate plannedStartDate,
        String employeeCode,
        Long orgUnitId,
        String orgUnitCode,
        String orgUnitName,
        Long jobTitleId,
        String jobTitleCode,
        String jobTitleName,
        Long managerEmployeeId,
        String managerEmployeeCode,
        String managerFullName,
        String workLocation,
        Long linkedEmployeeId,
        UUID linkedUserId,
        Long firstContractId,
        String status,
        String note,
        LocalDateTime orientationConfirmedAt,
        String orientationConfirmedByUsername,
        String orientationNote,
        LocalDateTime completedAt,
        String completedByUsername,
        String completedNote,
        List<OnboardingChecklistResponse> checklistItems,
        List<OnboardingDocumentResponse> documents,
        List<OnboardingAssetResponse> assets,
        List<OnboardingNotificationResponse> notifications
) {
}
