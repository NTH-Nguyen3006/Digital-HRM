package com.company.hrm.module.payroll.dto;

import com.company.hrm.common.constant.RelationshipCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaxDependentUpsertRequest(
        @NotBlank(message = "fullName là bắt buộc.")
        @Size(max = 200, message = "fullName tối đa 200 ký tự.")
        String fullName,

        @NotNull(message = "relationshipCode là bắt buộc.")
        RelationshipCode relationshipCode,

        @Size(max = 50, message = "identificationNo tối đa 50 ký tự.")
        String identificationNo,

        LocalDate dateOfBirth,

        @NotNull(message = "deductionStartMonth là bắt buộc.")
        LocalDate deductionStartMonth,

        LocalDate deductionEndMonth,

        @Size(max = 500, message = "note tối đa 500 ký tự.")
        String note
) {
}
