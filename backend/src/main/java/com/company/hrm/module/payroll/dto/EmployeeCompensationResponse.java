package com.company.hrm.module.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EmployeeCompensationResponse(
        Long employeeCompensationId,
        Long employeeId,
        String employeeCode,
        String employeeName,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        BigDecimal baseSalary,
        BigDecimal insuranceSalaryBase,
        String salaryCurrency,
        String bankAccountName,
        String bankAccountNo,
        String bankName,
        String paymentNote,
        String status,
        List<EmployeeCompensationItemResponse> items
) {
}
