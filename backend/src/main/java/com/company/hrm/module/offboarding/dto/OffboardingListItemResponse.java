package com.company.hrm.module.offboarding.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OffboardingListItemResponse(
        Long offboardingCaseId,
        String offboardingCode,
        Long employeeId,
        String employeeCode,
        String employeeFullName,
        Long orgUnitId,
        String orgUnitName,
        Long managerEmployeeId,
        String managerEmployeeName,
        String status,
        LocalDate requestDate,
        LocalDate requestedLastWorkingDate,
        LocalDate effectiveLastWorkingDate,
        LocalDateTime hrFinalizedAt,
        LocalDateTime closedAt
) {
}
