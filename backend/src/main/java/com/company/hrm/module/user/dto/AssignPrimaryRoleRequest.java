package com.company.hrm.module.user.dto;

import com.company.hrm.common.constant.RoleCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssignPrimaryRoleRequest(
        @NotNull(message = "roleCode là bắt buộc.")
        RoleCode roleCode,

        @NotBlank(message = "reason là bắt buộc.")
        @Size(max = 255, message = "reason không được vượt quá 255 ký tự.")
        String reason
) {
}
