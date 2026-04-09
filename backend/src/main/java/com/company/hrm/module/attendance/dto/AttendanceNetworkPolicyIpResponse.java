package com.company.hrm.module.attendance.dto;

import java.time.LocalDateTime;

public record AttendanceNetworkPolicyIpResponse(
        Long networkPolicyIpId,
        String cidrOrIp,
        String description,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
