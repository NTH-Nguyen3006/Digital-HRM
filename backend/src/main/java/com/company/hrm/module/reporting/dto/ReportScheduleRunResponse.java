package com.company.hrm.module.reporting.dto;

import java.time.LocalDateTime;

public record ReportScheduleRunResponse(
        Long reportScheduleRunId,
        Long reportScheduleConfigId,
        String scheduleCode,
        String triggerType,
        String runStatus,
        String outputFileKey,
        String outputFileName,
        String outputDownloadUrl,
        Integer outputRowCount,
        String runMessage,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
