package com.company.hrm.module.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateOvertimeRequest(
        @NotNull(message = "attendanceDate là bắt buộc.")
        LocalDate attendanceDate,

        @NotNull(message = "overtimeStartAt là bắt buộc.")
        LocalDateTime overtimeStartAt,

        @NotNull(message = "overtimeEndAt là bắt buộc.")
        LocalDateTime overtimeEndAt,

        @NotBlank(message = "reason là bắt buộc.")
        @Size(max = 1000, message = "reason tối đa 1000 ký tự.")
        String reason,

        @Size(max = 120, message = "evidenceFileKey tối đa 120 ký tự.")
        String evidenceFileKey
) {
}
