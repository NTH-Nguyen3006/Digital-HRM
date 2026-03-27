package com.company.hrm.module.orgunit.dto;

import java.util.ArrayList;
import java.util.List;

public record OrgUnitTreeNodeResponse(
        Long orgUnitId,
        Long parentOrgUnitId,
        String orgUnitCode,
        String orgUnitName,
        String orgUnitType,
        int hierarchyLevel,
        String status,
        Long managerEmployeeId,
        String managerEmployeeName,
        List<OrgUnitTreeNodeResponse> children
) {
    public OrgUnitTreeNodeResponse {
        children = children == null ? new ArrayList<>() : children;
    }
}
