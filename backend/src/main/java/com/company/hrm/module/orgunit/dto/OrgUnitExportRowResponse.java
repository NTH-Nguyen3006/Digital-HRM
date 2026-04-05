package com.company.hrm.module.orgunit.dto;

import java.time.LocalDate;

public record OrgUnitExportRowResponse(
        String orgUnitCode,
        String parentOrgUnitCode,
        String orgUnitName,
        String orgUnitType,
        Long managerEmployeeId,
        Integer sortOrder,
        String status,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
