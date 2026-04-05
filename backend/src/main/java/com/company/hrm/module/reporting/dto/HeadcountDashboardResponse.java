package com.company.hrm.module.reporting.dto;

import java.util.List;

public record HeadcountDashboardResponse(
        Long totalEmployeeCount,
        Long activeEmployeeCount,
        Long probationEmployeeCount,
        Long suspendedEmployeeCount,
        Long resignedEmployeeCount,
        Long terminatedEmployeeCount,
        Long contractExpiring30DayCount,
        Long pendingOnboardingCount,
        Long openOffboardingCount,
        List<DashboardBreakdownItemResponse> employmentStatusBreakdown,
        List<DashboardBreakdownItemResponse> orgUnitHeadcountBreakdown,
        List<DashboardTrendPointResponse> monthlyMovement
) {
}
