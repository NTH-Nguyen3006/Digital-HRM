package com.company.hrm.module.payroll.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FormulaVersionUpsertRequest(
        @NotBlank(message = "formulaCode là bắt buộc.")
        @Size(max = 30, message = "formulaCode tối đa 30 ký tự.")
        String formulaCode,

        @NotBlank(message = "formulaName là bắt buộc.")
        @Size(max = 200, message = "formulaName tối đa 200 ký tự.")
        String formulaName,

        @NotNull(message = "effectiveFrom là bắt buộc.")
        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        @NotNull(message = "personalDeductionMonthly là bắt buộc.")
        @DecimalMin(value = "0.00", inclusive = true, message = "personalDeductionMonthly không hợp lệ.")
        BigDecimal personalDeductionMonthly,

        @NotNull(message = "dependentDeductionMonthly là bắt buộc.")
        @DecimalMin(value = "0.00", inclusive = true, message = "dependentDeductionMonthly không hợp lệ.")
        BigDecimal dependentDeductionMonthly,

        @NotNull(message = "socialInsuranceEmployeeRate là bắt buộc.")
        @DecimalMin(value = "0.00", inclusive = true, message = "socialInsuranceEmployeeRate không hợp lệ.")
        BigDecimal socialInsuranceEmployeeRate,

        @NotNull(message = "healthInsuranceEmployeeRate là bắt buộc.")
        @DecimalMin(value = "0.00", inclusive = true, message = "healthInsuranceEmployeeRate không hợp lệ.")
        BigDecimal healthInsuranceEmployeeRate,

        @NotNull(message = "unemploymentInsuranceEmployeeRate là bắt buộc.")
        @DecimalMin(value = "0.00", inclusive = true, message = "unemploymentInsuranceEmployeeRate không hợp lệ.")
        BigDecimal unemploymentInsuranceEmployeeRate,

        @DecimalMin(value = "0.00", inclusive = true, message = "insuranceSalaryCapAmount không hợp lệ.")
        BigDecimal insuranceSalaryCapAmount,

        @DecimalMin(value = "0.00", inclusive = true, message = "unemploymentSalaryCapAmount không hợp lệ.")
        BigDecimal unemploymentSalaryCapAmount,

        @NotNull(message = "standardWorkHoursPerDay là bắt buộc.")
        @DecimalMin(value = "0.01", inclusive = true, message = "standardWorkHoursPerDay phải lớn hơn 0.")
        BigDecimal standardWorkHoursPerDay,

        @NotNull(message = "overtimeMultiplierWeekday là bắt buộc.")
        @DecimalMin(value = "0.01", inclusive = true, message = "overtimeMultiplierWeekday phải lớn hơn 0.")
        BigDecimal overtimeMultiplierWeekday,

        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note,

        @Valid
        @NotEmpty(message = "taxBrackets là bắt buộc.")
        List<FormulaTaxBracketRequest> taxBrackets
) {
}
