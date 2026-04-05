package com.company.hrm.module.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PayrollItemResponse(
        Long payrollItemId,
        Long payrollPeriodId,
        Long employeeId,
        String employeeCode,
        String employeeName,
        String orgUnitName,
        Integer scheduledDayCount,
        Integer presentDayCount,
        Integer paidLeaveDayCount,
        Integer unpaidLeaveDayCount,
        Integer absentDayCount,
        Integer approvedOtMinutes,
        BigDecimal baseSalaryMonthly,
        BigDecimal baseSalaryProrated,
        BigDecimal fixedEarningTotal,
        BigDecimal fixedDeductionTotal,
        BigDecimal employeeInsuranceAmount,
        BigDecimal personalDeductionAmount,
        BigDecimal dependentDeductionAmount,
        BigDecimal taxableIncome,
        BigDecimal pitAmount,
        BigDecimal grossIncome,
        BigDecimal netPay,
        String itemStatus,
        boolean managerConfirmationRequired,
        String managerConfirmedByUsername,
        LocalDateTime managerConfirmedAt,
        String managerConfirmNote,
        String hrApprovedByUsername,
        LocalDateTime hrApprovedAt,
        LocalDateTime publishedAt,
        String adjustmentNote,
        List<PayrollItemLineResponse> lines
) {
}
