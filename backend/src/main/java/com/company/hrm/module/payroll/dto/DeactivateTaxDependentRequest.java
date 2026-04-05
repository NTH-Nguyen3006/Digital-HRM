package com.company.hrm.module.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record DeactivateTaxDependentRequest(
        @NotNull(message = "deductionEndMonth là bắt buộc.")
        LocalDate deductionEndMonth,

        @NotBlank(message = "note là bắt buộc.")
        @Size(max = 500, message = "note tối đa 500 ký tự.")
        String note
) {
}
