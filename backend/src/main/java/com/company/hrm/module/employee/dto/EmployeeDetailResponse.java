package com.company.hrm.module.employee.dto;

import java.time.LocalDate;

public record EmployeeDetailResponse(
        Long employeeId,
        String employeeCode,
        String fullName,
        String workEmail,
        String workPhone,
        String genderCode,
        LocalDate dateOfBirth,
        LocalDate hireDate,
        String employmentStatus,
        String workLocation,
        String taxCode,
        String personalEmail,
        String mobilePhone,
        String avatarUrl,
        String note,
        Long orgUnitId,
        String orgUnitCode,
        String orgUnitName,
        String orgUnitType,
        Long jobTitleId,
        String jobTitleCode,
        String jobTitleName,
        String jobLevelCode,
        Long managerEmployeeId,
        String managerEmployeeCode,
        String managerEmployeeName
) {
}
