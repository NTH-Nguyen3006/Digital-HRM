package com.company.hrm.module.payroll.dto;

import java.time.LocalDate;

public record TaxDependentResponse(
        Long taxDependentId,
        String fullName,
        String relationshipCode,
        String identificationNo,
        LocalDate dateOfBirth,
        LocalDate deductionStartMonth,
        LocalDate deductionEndMonth,
        String status,
        String note
) {
}
