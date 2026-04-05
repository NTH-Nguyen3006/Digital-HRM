package com.company.hrm.module.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelAttendanceAdjustmentRequest(
        @NotBlank(message = "cancelNote là bắt buộc.")
        @Size(max = 1000, message = "cancelNote tối đa 1000 ký tự.")
        String cancelNote
) {
}
