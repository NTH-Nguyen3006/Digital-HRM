package com.company.hrm.module.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ContractAppendixRequest(
        @NotBlank(message = "appendixNumber là bắt buộc.")
        @Size(max = 50, message = "appendixNumber tối đa 50 ký tự.")
        String appendixNumber,

        @NotBlank(message = "appendixName là bắt buộc.")
        @Size(max = 255, message = "appendixName tối đa 255 ký tự.")
        String appendixName,

        @NotNull(message = "effectiveDate là bắt buộc.")
        LocalDate effectiveDate,

        String changedFieldsJson,

        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
