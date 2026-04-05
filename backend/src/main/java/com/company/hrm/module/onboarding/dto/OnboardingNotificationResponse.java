package com.company.hrm.module.onboarding.dto;

import java.time.LocalDateTime;

public record OnboardingNotificationResponse(
        Long onboardingNotificationLogId,
        String channelCode,
        String recipientName,
        String recipientEmail,
        String deliveryStatus,
        String subjectSnapshot,
        LocalDateTime sentAt,
        String note
) {
}
