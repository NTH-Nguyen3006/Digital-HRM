package com.company.hrm.module.payroll.dto;

import java.time.LocalDate;
import java.util.List;

public record PersonalTaxProfileResponse(
        Long personalTaxProfileId,
        Long employeeId,
        String employeeCode,
        String employeeName,
        boolean residentTaxpayer,
        boolean familyDeductionEnabled,
        LocalDate taxRegistrationDate,
        String taxFinalizationMethod,
        String status,
        String note,
        List<TaxDependentResponse> dependents
) {
}
