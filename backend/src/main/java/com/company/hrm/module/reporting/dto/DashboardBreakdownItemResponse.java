package com.company.hrm.module.reporting.dto;

public record DashboardBreakdownItemResponse(
        String key,
        String label,
        Long value
) {
}
