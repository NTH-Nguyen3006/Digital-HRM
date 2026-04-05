package com.company.hrm.module.audit.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogListItemResponse(
        UUID auditLogId,
        UUID actorUserId,
        String actorUsername,
        String actionCode,
        String moduleCode,
        String entityName,
        String entityId,
        String requestId,
        String ipAddress,
        LocalDateTime actionAt,
        String resultCode,
        String message
) {
}
