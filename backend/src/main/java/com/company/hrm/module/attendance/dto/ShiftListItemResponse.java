package com.company.hrm.module.attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShiftListItemResponse(
        Long shiftId,
        String shiftCode,
        String shiftName,
        String description,
        Integer sortOrder,
        String status,
        Long currentShiftVersionId,
        Integer currentVersionNo,
        LocalDate currentEffectiveFrom,
        LocalDate currentEffectiveTo,
        LocalTime startTime,
        LocalTime endTime,
        boolean crossesMidnight,
        Integer breakMinutes,
        Integer lateGraceMinutes,
        Integer earlyLeaveGraceMinutes,
        boolean otAllowed,
        boolean nightShift
) {
}
