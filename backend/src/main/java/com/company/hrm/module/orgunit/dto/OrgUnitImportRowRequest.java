package com.company.hrm.module.orgunit.dto;

import com.company.hrm.common.constant.OrgUnitType;
import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record OrgUnitImportRowRequest(
        @NotBlank(message = "orgUnitCode là bắt buộc.")
        @Size(max = 30, message = "orgUnitCode tối đa 30 ký tự.")
        String orgUnitCode,

        @Size(max = 30, message = "parentOrgUnitCode tối đa 30 ký tự.")
        String parentOrgUnitCode,

        @NotBlank(message = "orgUnitName là bắt buộc.")
        @Size(max = 200, message = "orgUnitName tối đa 200 ký tự.")
        String orgUnitName,

        @NotNull(message = "orgUnitType là bắt buộc.")
        OrgUnitType orgUnitType,

        Long managerEmployeeId,
        Integer sortOrder,
        @NotNull(message = "status là bắt buộc.")
        RecordStatus status,
        @NotNull(message = "effectiveFrom là bắt buộc.")
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
