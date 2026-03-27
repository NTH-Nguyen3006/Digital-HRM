package com.company.hrm.module.orgunit.dto;

import java.time.LocalDate;

public record OrgUnitDetailResponse(
        Long orgUnitId,
        Long parentOrgUnitId,
        String parentOrgUnitCode,
        String parentOrgUnitName,
        String orgUnitCode,
        String orgUnitName,
        String orgUnitType,
        int hierarchyLevel,
        String pathCode,
        int sortOrder,
        String status,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Long managerEmployeeId,
        String managerEmployeeCode,
        String managerEmployeeName
) {
}
