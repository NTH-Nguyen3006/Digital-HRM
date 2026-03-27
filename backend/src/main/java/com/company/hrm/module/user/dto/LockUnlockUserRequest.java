package com.company.hrm.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record LockUnlockUserRequest(
        boolean locked,

        @NotBlank(message = "reason là bắt buộc.")
        @Size(max = 255, message = "reason không được vượt quá 255 ký tự.")
        String reason,

        LocalDateTime lockedUntil
) {
}
