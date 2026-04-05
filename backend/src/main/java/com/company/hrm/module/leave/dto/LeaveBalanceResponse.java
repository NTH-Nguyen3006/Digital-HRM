package com.company.hrm.module.leave.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record LeaveBalanceResponse(
        Long leaveBalanceId,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long leaveTypeId,
        String leaveTypeCode,
        String leaveTypeName,
        Integer leaveYear,
        BigDecimal openingUnits,
        BigDecimal accruedUnits,
        BigDecimal usedUnits,
        BigDecimal adjustedUnits,
        BigDecimal carriedForwardUnits,
        BigDecimal settledUnits,
        BigDecimal availableUnits,
        String balanceStatus,
        LocalDateTime lastTransactionAt,
        List<LeaveBalanceTransactionResponse> transactions
) {
}
