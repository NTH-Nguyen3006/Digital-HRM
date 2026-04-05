package com.company.hrm.module.portal.dto;

import com.company.hrm.module.employee.dto.EmployeeDetailResponse;
import com.company.hrm.module.employee.dto.EmployeeProfileResponse;

public record PortalProfileResponse(
        EmployeeDetailResponse employee,
        EmployeeProfileResponse profile
) {
}
