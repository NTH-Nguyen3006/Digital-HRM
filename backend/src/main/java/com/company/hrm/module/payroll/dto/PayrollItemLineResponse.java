package com.company.hrm.module.payroll.dto;

import java.math.BigDecimal;

public record PayrollItemLineResponse(
        Long payrollItemLineId,
        String componentCode,
        String componentName,
        String componentCategory,
        String lineSourceType,
        BigDecimal lineAmount,
        boolean taxable,
        Integer displayOrder,
        String lineNote
) {
}
