package com.company.hrm.module.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AttendanceNetworkPolicyDetailResponse(
        Long networkPolicyId,
        String policyCode,
        String policyName,
        String scopeType,
        Long orgUnitId,
        String orgUnitCode,
        String orgUnitName,
        boolean allowCheckIn,
        boolean allowCheckOut,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String status,
        String description,
        List<AttendanceNetworkPolicyIpResponse> ipRanges,
        LocalDateTime createdAt,
        UUID createdBy,
        LocalDateTime updatedAt,
        UUID updatedBy
) {
}
