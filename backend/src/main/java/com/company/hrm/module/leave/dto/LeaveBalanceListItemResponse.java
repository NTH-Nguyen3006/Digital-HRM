package com.company.hrm.module.leave.dto;

import java.math.BigDecimal;

public record LeaveBalanceListItemResponse(
        Long leaveBalanceId,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long leaveTypeId,
        String leaveTypeCode,
        String leaveTypeName,
        Integer leaveYear,
        BigDecimal availableUnits,
        String balanceStatus
) {
}
