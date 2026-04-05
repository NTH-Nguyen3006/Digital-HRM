package com.company.hrm.module.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FormulaVersionResponse(
        Long formulaVersionId,
        String formulaCode,
        String formulaName,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        BigDecimal personalDeductionMonthly,
        BigDecimal dependentDeductionMonthly,
        BigDecimal socialInsuranceEmployeeRate,
        BigDecimal healthInsuranceEmployeeRate,
        BigDecimal unemploymentInsuranceEmployeeRate,
        BigDecimal insuranceSalaryCapAmount,
        BigDecimal unemploymentSalaryCapAmount,
        BigDecimal standardWorkHoursPerDay,
        BigDecimal overtimeMultiplierWeekday,
        String status,
        String note,
        List<FormulaTaxBracketResponse> taxBrackets
) {
}
