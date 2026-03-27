package com.company.hrm.module.orgunit.dto;

import com.company.hrm.common.constant.OrgUnitType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record UpdateOrgUnitRequest(
        Long parentOrgUnitId,
        @NotBlank(message = "orgUnitCode không được để trống.")
        @Size(max = 30, message = "orgUnitCode tối đa 30 ký tự.")
        String orgUnitCode,
        @NotBlank(message = "orgUnitName không được để trống.")
        @Size(max = 200, message = "orgUnitName tối đa 200 ký tự.")
        String orgUnitName,
        @NotNull(message = "orgUnitType không được để trống.")
        OrgUnitType orgUnitType,
        @Min(value = 0, message = "sortOrder phải lớn hơn hoặc bằng 0.")
        Integer sortOrder,
        @NotNull(message = "effectiveFrom không được để trống.")
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
