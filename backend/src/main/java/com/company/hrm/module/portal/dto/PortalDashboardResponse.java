package com.company.hrm.module.portal.dto;

import java.math.BigDecimal;

public record PortalDashboardResponse(
        Long employeeId,
        String employeeCode,
        String fullName,
        String employmentStatus,
        String orgUnitName,
        String jobTitleName,
        long unreadInboxCount,
        long openPortalTaskCount,
        long leaveBalanceCount,
        long pendingLeaveRequestCount,
        long pendingAttendanceAdjustmentCount,
        long publishedPayslipCount,
        BigDecimal totalAvailableLeaveUnits
) {
}
