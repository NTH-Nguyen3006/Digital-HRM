package com.company.hrm.module.attendance.dto;

import jakarta.validation.constraints.Size;

public record FinalizeAttendanceAdjustmentRequest(
        boolean approved,

        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
