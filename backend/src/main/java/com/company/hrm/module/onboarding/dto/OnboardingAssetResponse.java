package com.company.hrm.module.onboarding.dto;

import java.time.LocalDate;

public record OnboardingAssetResponse(
        Long onboardingAssetId,
        String assetCode,
        String assetName,
        String assetType,
        LocalDate assignedDate,
        LocalDate returnedDate,
        String status,
        String note
) {
}
