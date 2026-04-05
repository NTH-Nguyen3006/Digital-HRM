package com.company.hrm.module.portal.dto;

import java.time.LocalDateTime;

public record PortalInboxItemResponse(
        Long portalInboxItemId,
        String itemType,
        String categoryCode,
        String title,
        String message,
        String relatedModule,
        String relatedEntityId,
        LocalDateTime dueAt,
        LocalDateTime readAt,
        String taskStatus,
        LocalDateTime completedAt,
        String status,
        LocalDateTime createdAt
) {
}
