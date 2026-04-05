package com.company.hrm.module.leave.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LeaveRequestListItemResponse(
        Long leaveRequestId,
        String requestCode,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long leaveTypeId,
        String leaveTypeCode,
        String leaveTypeName,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal requestedUnits,
        String requestStatus,
        String approvalRoleCode,
        LocalDate submittedDate
) {
}
