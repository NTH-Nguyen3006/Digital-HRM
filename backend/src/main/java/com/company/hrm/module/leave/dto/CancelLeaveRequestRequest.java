package com.company.hrm.module.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelLeaveRequestRequest(
        @NotBlank(message = "cancelNote là bắt buộc.")
        @Size(max = 1000, message = "cancelNote tối đa 1000 ký tự.")
        String cancelNote
) {
}
