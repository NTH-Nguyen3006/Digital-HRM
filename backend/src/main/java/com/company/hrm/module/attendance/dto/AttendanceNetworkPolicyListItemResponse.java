package com.company.hrm.module.attendance.dto;

import java.time.LocalDate;

public record AttendanceNetworkPolicyListItemResponse(
        Long networkPolicyId,
        String policyCode,
        String policyName,
        String scopeType,
        Long orgUnitId,
        String orgUnitName,
        boolean allowCheckIn,
        boolean allowCheckOut,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String status,
        int ipRangeCount,
        String description
) {
}
