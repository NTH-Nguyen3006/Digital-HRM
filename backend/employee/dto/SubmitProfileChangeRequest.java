package com.company.hrm.module.employee.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitProfileChangeRequest(
        @NotNull(message = "payload là bắt buộc.")
        @Valid
        ProfileChangeRequestPayloadRequest payload,

        @Size(max = 500, message = "reason tối đa 500 ký tự.")
        String reason
) {
}
