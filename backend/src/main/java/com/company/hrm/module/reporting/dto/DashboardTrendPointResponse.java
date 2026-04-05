package com.company.hrm.module.reporting.dto;

public record DashboardTrendPointResponse(
        String periodLabel,
        Long joinerCount,
        Long leaverCount
) {
}
