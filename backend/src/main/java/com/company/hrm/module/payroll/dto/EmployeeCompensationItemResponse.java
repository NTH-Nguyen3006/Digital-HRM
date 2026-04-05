package com.company.hrm.module.payroll.dto;

import java.math.BigDecimal;

public record EmployeeCompensationItemResponse(
        Long employeeCompensationItemId,
        Long salaryComponentId,
        String componentCode,
        String componentName,
        String componentCategory,
        String amountType,
        BigDecimal amountValue,
        BigDecimal percentageValue,
        String note
) {
}
