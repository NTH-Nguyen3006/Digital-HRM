package com.company.hrm.module.leave.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record LeaveRequestDetailResponse(
        Long leaveRequestId,
        String requestCode,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long leaveTypeId,
        String leaveTypeCode,
        String leaveTypeName,
        Long leaveTypeRuleId,
        Integer leaveTypeRuleVersionNo,
        String approvalRoleCode,
        String requestStatus,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal requestedUnits,
        String reason,
        String attachmentRef,
        LocalDateTime submittedAt,
        LocalDateTime approvedAt,
        String approvedByUsername,
        String approvalNote,
        LocalDateTime rejectedAt,
        String rejectedByUsername,
        String rejectionNote,
        LocalDateTime finalizedAt,
        String finalizedByUsername,
        String finalizeNote,
        LocalDateTime canceledAt,
        String canceledByUsername,
        String cancelNote,
        List<LeaveRequestHistoryResponse> histories
) {
}
