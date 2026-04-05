package com.company.hrm.module.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OvertimeListItemResponse(
        Long overtimeRequestId,
        String requestCode,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long orgUnitId,
        String orgUnitName,
        LocalDate attendanceDate,
        LocalDateTime overtimeStartAt,
        LocalDateTime overtimeEndAt,
        Integer requestedMinutes,
        String reason,
        String evidenceFileKey,
        String requestStatus,
        LocalDateTime submittedAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt,
        String managerNote,
        String rejectionNote
) {
}
