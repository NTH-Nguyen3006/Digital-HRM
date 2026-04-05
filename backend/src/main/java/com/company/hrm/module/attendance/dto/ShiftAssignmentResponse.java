package com.company.hrm.module.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShiftAssignmentResponse(
        Long shiftAssignmentId,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long orgUnitId,
        String orgUnitName,
        Long shiftId,
        String shiftCode,
        String shiftName,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String assignmentNote,
        String assignmentBatchRef,
        LocalDateTime createdAt,
        java.util.UUID createdBy
) {
}
