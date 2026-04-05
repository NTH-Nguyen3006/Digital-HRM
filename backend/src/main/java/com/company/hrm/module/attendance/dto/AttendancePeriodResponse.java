package com.company.hrm.module.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AttendancePeriodResponse(
        Long attendancePeriodId,
        String periodCode,
        Integer periodYear,
        Integer periodMonth,
        LocalDate periodStartDate,
        LocalDate periodEndDate,
        String periodStatus,
        String note,
        LocalDateTime closedAt,
        UUID closedBy,
        LocalDateTime reopenedAt,
        UUID reopenedBy,
        String reopenReason,
        boolean reopenedFlag,
        Integer totalEmployeeCount,
        Integer totalAnomalyDayCount
) {
}
