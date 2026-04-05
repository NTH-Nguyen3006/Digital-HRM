package com.company.hrm.module.employee.dto;

import java.time.LocalDateTime;
import java.util.UUID;

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
        String emergencyNote,
        String profileStatus,
        String lockedReason,
        LocalDateTime lockedAt,
        UUID lockedBy,
        LocalDateTime restoredAt,
        UUID restoredBy
) {
}
