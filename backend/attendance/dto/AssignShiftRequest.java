package com.company.hrm.module.attendance.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record AssignShiftRequest(
        @NotNull(message = "shiftId là bắt buộc.")
        Long shiftId,

        @NotEmpty(message = "employeeIds là bắt buộc.")
        List<Long> employeeIds,

        @NotNull(message = "effectiveFrom là bắt buộc.")
        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        @Size(max = 500, message = "assignmentNote tối đa 500 ký tự.")
        String assignmentNote,

        @Size(max = 50, message = "assignmentBatchRef tối đa 50 ký tự.")
        String assignmentBatchRef
) {
}
