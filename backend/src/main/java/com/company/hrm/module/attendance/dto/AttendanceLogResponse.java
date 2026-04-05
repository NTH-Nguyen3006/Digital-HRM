package com.company.hrm.module.attendance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceLogResponse(
        Long attendanceLogId,
        Long employeeId,
        String employeeCode,
        String employeeName,
        LocalDate attendanceDate,
        String eventType,
        LocalDateTime eventTime,
        String sourceType,
        BigDecimal latitude,
        BigDecimal longitude,
        String deviceRef,
        String note,
        Long shiftAssignmentId,
        Long adjustmentRequestId
) {
}
