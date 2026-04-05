package com.company.hrm.module.reporting.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SystemHealthDashboardResponse(
        LocalDateTime capturedAt,
        Long activeUserCount,
        Long lockedUserCount,
        Long activeSessionCount,
        Long storageFileCount,
        Long missingStorageBinaryCount,
        Long auditFailureLast24hCount,
        Long pendingWorkflowCount,
        Long overdueChecklistCount,
        Long scheduledReportActiveCount,
        Long scheduledReportFailedLast7dCount,
        List<HealthMetricResponse> pendingModuleMetrics
) {
}
