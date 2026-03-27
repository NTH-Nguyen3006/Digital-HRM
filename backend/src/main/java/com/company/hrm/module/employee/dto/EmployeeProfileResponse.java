package com.company.hrm.module.employee.dto;

public record EmployeeProfileResponse(
        Long employeeProfileId,
        Long employeeId,
        String firstName,
        String middleName,
        String lastName,
        String maritalStatus,
        String nationality,
        String placeOfBirth,
        String ethnicGroup,
        String religion,
        String educationLevel,
        String major,
        String emergencyNote
) {
}
