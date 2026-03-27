package com.company.hrm.module.orgunit.dto;

import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateOrgUnitStatusRequest(
        @NotNull(message = "status không được để trống.")
        RecordStatus status,
        LocalDate effectiveTo,
        String note
) {
}
