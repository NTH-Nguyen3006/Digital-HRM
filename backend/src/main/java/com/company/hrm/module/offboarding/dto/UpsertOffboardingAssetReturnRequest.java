package com.company.hrm.module.offboarding.dto;

import com.company.hrm.common.constant.OffboardingAssetReturnStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpsertOffboardingAssetReturnRequest(
        @Size(max = 50, message = "assetCode tối đa 50 ký tự.")
        String assetCode,

        @NotBlank(message = "assetName là bắt buộc.")
        @Size(max = 255, message = "assetName tối đa 255 ký tự.")
        String assetName,

        @Size(max = 50, message = "assetType tối đa 50 ký tự.")
        String assetType,

        LocalDate expectedReturnDate,
        LocalDate returnedDate,

        @NotNull(message = "status là bắt buộc.")
        OffboardingAssetReturnStatus status,

        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
