package com.company.hrm.module.employee.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewProfileChangeRequest(
        @NotNull(message = "approved là bắt buộc.")
        Boolean approved,

        @Size(max = 1000, message = "reviewNote tối đa 1000 ký tự.")
        String reviewNote
) {
}
