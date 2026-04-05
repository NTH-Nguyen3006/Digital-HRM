package com.company.hrm.module.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeCompensationListItemResponse(
        Long employeeCompensationId,
        Long employeeId,
        String employeeCode,
        String employeeName,
        String orgUnitName,
        BigDecimal baseSalary,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String status
) {
}
