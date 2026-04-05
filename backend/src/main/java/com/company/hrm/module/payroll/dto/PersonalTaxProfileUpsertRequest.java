package com.company.hrm.module.payroll.dto;

import com.company.hrm.common.constant.TaxFinalizationMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PersonalTaxProfileUpsertRequest(
        boolean residentTaxpayer,
        boolean familyDeductionEnabled,
        LocalDate taxRegistrationDate,

        @NotNull(message = "taxFinalizationMethod là bắt buộc.")
        TaxFinalizationMethod taxFinalizationMethod,

        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
