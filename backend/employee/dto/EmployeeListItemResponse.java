package com.company.hrm.module.employee.dto;

public record EmployeeListItemResponse(
        Long employeeId,
        String employeeCode,
        String fullName,
        String workEmail,
        String workPhone,
        String genderCode,
        String employmentStatus,
        Long orgUnitId,
        String orgUnitCode,
        String orgUnitName,
        Long jobTitleId,
        String jobTitleCode,
        String jobTitleName,
        Long managerEmployeeId,
        String managerEmployeeCode,
        String managerEmployeeName
) {
}
