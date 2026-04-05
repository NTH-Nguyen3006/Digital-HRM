package com.company.hrm.module.offboarding.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OffboardingAssetReturnResponse(
        Long offboardingAssetReturnId,
        String assetCode,
        String assetName,
        String assetType,
        LocalDate expectedReturnDate,
        LocalDate returnedDate,
        String status,
        String note,
        LocalDateTime updatedAtAction
) {
}
