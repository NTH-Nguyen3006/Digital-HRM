package com.company.hrm.module.attendance.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public record UpsertShiftRequest(
        @NotBlank(message = "shiftCode là bắt buộc.")
        @Size(max = 30, message = "shiftCode tối đa 30 ký tự.")
        String shiftCode,

        @NotBlank(message = "shiftName là bắt buộc.")
        @Size(max = 200, message = "shiftName tối đa 200 ký tự.")
        String shiftName,

        @Size(max = 500, message = "description tối đa 500 ký tự.")
        String description,

        @NotNull(message = "effectiveFrom là bắt buộc.")
        LocalDate effectiveFrom,

        @NotNull(message = "startTime là bắt buộc.")
        LocalTime startTime,

        @NotNull(message = "endTime là bắt buộc.")
        LocalTime endTime,

        boolean crossesMidnight,

        @NotNull(message = "breakMinutes là bắt buộc.")
        @Min(value = 0, message = "breakMinutes không được âm.")
        @Max(value = 720, message = "breakMinutes tối đa 720 phút.")
        Integer breakMinutes,

        @NotNull(message = "lateGraceMinutes là bắt buộc.")
        @Min(value = 0, message = "lateGraceMinutes không được âm.")
        @Max(value = 240, message = "lateGraceMinutes tối đa 240 phút.")
        Integer lateGraceMinutes,

        @NotNull(message = "earlyLeaveGraceMinutes là bắt buộc.")
        @Min(value = 0, message = "earlyLeaveGraceMinutes không được âm.")
        @Max(value = 240, message = "earlyLeaveGraceMinutes tối đa 240 phút.")
        Integer earlyLeaveGraceMinutes,

        boolean otAllowed,
        boolean nightShift,

        @Min(value = 0, message = "minWorkMinutesForPresent không được âm.")
        Integer minWorkMinutesForPresent,

        @Min(value = 0, message = "sortOrder không được âm.")
        Integer sortOrder,

        @Size(max = 500, message = "note tối đa 500 ký tự.")
        String note
) {
}
