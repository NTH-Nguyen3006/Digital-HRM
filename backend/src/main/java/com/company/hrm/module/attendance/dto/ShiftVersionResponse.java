package com.company.hrm.module.attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShiftVersionResponse(
        Long shiftVersionId,
        Integer versionNo,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        LocalTime startTime,
        LocalTime endTime,
        boolean crossesMidnight,
        Integer breakMinutes,
        Integer lateGraceMinutes,
        Integer earlyLeaveGraceMinutes,
        boolean otAllowed,
        boolean nightShift,
        Integer minWorkMinutesForPresent,
        String status,
        String note
) {
}
