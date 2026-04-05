package com.company.hrm.module.role.dto;

import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRoleStatusRequest(
        @NotNull(message = "status là bắt buộc.")
        RecordStatus status,

        @Size(max = 500, message = "reason tối đa 500 ký tự.")
        String reason
) {
}
