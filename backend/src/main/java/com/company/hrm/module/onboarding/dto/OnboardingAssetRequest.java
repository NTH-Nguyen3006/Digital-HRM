package com.company.hrm.module.onboarding.dto;

import com.company.hrm.common.constant.OnboardingAssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record OnboardingAssetRequest(
        @NotBlank(message = "assetCode là bắt buộc.")
        @Size(max = 50, message = "assetCode tối đa 50 ký tự.")
        String assetCode,

        @NotBlank(message = "assetName là bắt buộc.")
        @Size(max = 255, message = "assetName tối đa 255 ký tự.")
        String assetName,

        @NotBlank(message = "assetType là bắt buộc.")
        @Size(max = 50, message = "assetType tối đa 50 ký tự.")
        String assetType,

        LocalDate assignedDate,
        LocalDate returnedDate,

        @NotNull(message = "status là bắt buộc.")
        OnboardingAssetStatus status,

        @Size(max = 500, message = "note tối đa 500 ký tự.")
        String note
) {
}
