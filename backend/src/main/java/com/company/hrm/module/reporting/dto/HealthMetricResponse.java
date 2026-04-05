package com.company.hrm.module.reporting.dto;

public record HealthMetricResponse(
        String metricCode,
        String metricName,
        Long metricValue,
        String severity
) {
}
