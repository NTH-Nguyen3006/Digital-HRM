package com.company.hrm.module.reporting.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReportScheduleConfigResponse(
        Long reportScheduleConfigId,
        String scheduleCode,
        String scheduleName,
        String reportCode,
        String frequencyCode,
        Integer dayOfWeek,
        Integer dayOfMonth,
        Integer runAtHour,
        Integer runAtMinute,
        List<String> recipientEmails,
        String parameterJson,
        String status,
        LocalDateTime nextRunAt,
        LocalDateTime lastRunAt,
        String lastRunStatus,
        String lastRunMessage,
        String description
) {
}
