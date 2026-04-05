package com.company.hrm.module.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PayrollPeriodResponse(
        Long payrollPeriodId,
        String periodCode,
        Integer periodYear,
        Integer periodMonth,
        LocalDate periodStartDate,
        LocalDate periodEndDate,
        Long attendancePeriodId,
        String attendancePeriodCode,
        Long formulaVersionId,
        String formulaCode,
        String formulaName,
        String periodStatus,
        LocalDateTime generatedAt,
        String generatedByUsername,
        LocalDateTime approvedAt,
        String approvedByUsername,
        LocalDateTime publishedAt,
        String publishedByUsername,
        Integer totalEmployeeCount,
        Integer managerConfirmedCount,
        BigDecimal totalGrossAmount,
        BigDecimal totalNetAmount,
        String note
) {
}
