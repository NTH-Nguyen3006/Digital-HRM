package com.company.hrm.module.jobtitle.dto;

import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateJobTitleStatusRequest(
        @NotNull(message = "status không được để trống.")
        RecordStatus status,
        String note
) {
}
