package com.company.hrm.module.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DailyAttendanceListItemResponse(
        Long dailyAttendanceId,
        Long employeeId,
        String employeeCode,
        String employeeName,
        Long orgUnitId,
        String orgUnitName,
        LocalDate attendanceDate,
        Long shiftId,
        String shiftCode,
        String shiftName,
        Long shiftVersionId,
        Long shiftAssignmentId,
        LocalDateTime plannedStartAt,
        LocalDateTime plannedEndAt,
        LocalDateTime actualCheckInAt,
        LocalDateTime actualCheckOutAt,
        Integer workedMinutes,
        Integer lateMinutes,
        Integer earlyLeaveMinutes,
        Integer approvedOtMinutes,
        boolean missingCheckIn,
        boolean missingCheckOut,
        Integer anomalyCount,
        List<String> anomalyCodes,
        String dailyStatus,
        boolean onLeave,
        Long attendancePeriodId,
        Long finalizedAdjustmentRequestId
) {
}
