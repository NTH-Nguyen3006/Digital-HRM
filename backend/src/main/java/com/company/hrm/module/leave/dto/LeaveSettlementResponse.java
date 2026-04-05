package com.company.hrm.module.leave.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LeaveSettlementResponse(
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long leaveTypeId,
        String leaveTypeCode,
        String leaveTypeName,
        LocalDate settlementDate,
        Integer leaveYear,
        BigDecimal availableUnitsBeforeSettlement,
        BigDecimal settledUnits,
        BigDecimal availableUnitsAfterSettlement,
        String balanceStatus
) {
}
