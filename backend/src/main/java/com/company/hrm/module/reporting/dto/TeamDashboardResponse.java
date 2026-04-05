package com.company.hrm.module.reporting.dto;

import java.util.List;

public record TeamDashboardResponse(
        Long managerEmployeeId,
        String managerEmployeeCode,
        String managerEmployeeName,
        Long teamHeadcount,
        Long presentTodayCount,
        Long onLeaveTodayCount,
        Long absentTodayCount,
        Long pendingLeaveApprovalCount,
        Long pendingAttendanceAdjustmentCount,
        Long pendingOvertimeCount,
        Long contractExpiring30DayCount,
        Long openOffboardingCount,
        List<DashboardBreakdownItemResponse> teamOrgBreakdown,
        List<DashboardBreakdownItemResponse> todayStatusBreakdown
) {
}
