package com.company.hrm.module.attendance.dto;

import jakarta.validation.constraints.Size;

public record ReviewAttendanceAdjustmentRequest(
        boolean approved,

        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
