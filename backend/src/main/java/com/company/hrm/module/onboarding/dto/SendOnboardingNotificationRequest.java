package com.company.hrm.module.onboarding.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public record SendOnboardingNotificationRequest(
        @Size(max = 50, message = "templateCode tối đa 50 ký tự.")
        String templateCode,
        boolean notifyNewHire,
        boolean notifyManager,
        boolean notifyLinkedUser,
        List<@Size(max = 150, message = "Email tùy chọn tối đa 150 ký tự.") String> customRecipientEmails,
        @Size(max = 500, message = "note tối đa 500 ký tự.")
        String note
) {
}
