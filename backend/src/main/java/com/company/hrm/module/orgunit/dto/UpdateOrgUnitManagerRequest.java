package com.company.hrm.module.orgunit.dto;

public record UpdateOrgUnitManagerRequest(
        Long managerEmployeeId,
        String note
) {
}
