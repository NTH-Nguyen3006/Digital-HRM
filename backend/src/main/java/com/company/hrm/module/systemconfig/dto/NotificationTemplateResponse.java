package com.company.hrm.module.systemconfig.dto;

public record NotificationTemplateResponse(
        Long notificationTemplateId,
        String templateCode,
        String templateName,
        String channelCode,
        String subjectTemplate,
        String bodyTemplate,
        String status,
        String description
) {
}
