package com.company.hrm.module.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceAdjustmentListItemResponse(
        Long adjustmentRequestId,
        String requestCode,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long orgUnitId,
        String orgUnitName,
        LocalDate attendanceDate,
        String issueType,
        LocalDateTime proposedCheckInAt,
        LocalDateTime proposedCheckOutAt,
        String reason,
        String evidenceFileKey,
        String requestStatus,
        LocalDateTime submittedAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt,
        LocalDateTime finalizedAt,
        LocalDateTime canceledAt,
        String managerNote,
        String rejectionNote,
        String finalizeNote,
        String cancelNote,
        Long copiedFromAdjustmentRequestId
) {
}
