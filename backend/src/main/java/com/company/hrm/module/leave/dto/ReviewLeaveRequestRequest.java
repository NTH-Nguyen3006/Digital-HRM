package com.company.hrm.module.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewLeaveRequestRequest(
        @NotNull(message = "approved là bắt buộc.")
        Boolean approved,

        @NotBlank(message = "note là bắt buộc.")
        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
