package com.company.hrm.module.payroll.dto;

public record SalaryComponentResponse(
        Long salaryComponentId,
        String componentCode,
        String componentName,
        String componentCategory,
        String amountType,
        boolean taxable,
        boolean insuranceBaseIncluded,
        boolean payslipVisible,
        Integer displayOrder,
        String status,
        String description
) {
}
