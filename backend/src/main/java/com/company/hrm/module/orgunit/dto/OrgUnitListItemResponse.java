package com.company.hrm.module.orgunit.dto;

public record OrgUnitListItemResponse(
        Long orgUnitId,
        Long parentOrgUnitId,
        String orgUnitCode,
        String orgUnitName,
        String orgUnitType,
        int hierarchyLevel,
        String pathCode,
        int sortOrder,
        String status,
        Long managerEmployeeId,
        String managerEmployeeCode,
        String managerEmployeeName
) {
}
