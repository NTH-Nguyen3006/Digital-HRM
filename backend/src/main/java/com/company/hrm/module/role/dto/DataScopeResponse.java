package com.company.hrm.module.role.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DataScopeResponse(
        UUID dataScopeAssignmentId,
        String subjectType,
        String scopeCode,
        String targetType,
        String targetRefId,
        boolean inclusive,
        int priorityOrder,
        String status,
        LocalDateTime effectiveFrom,
        LocalDateTime effectiveTo
) {
}
