package com.company.hrm.module.employee.dto;

import java.time.LocalDate;

public record EmployeeIdentificationResponse(
        Long employeeIdentificationId,
        Long employeeId,
        String documentType,
        String documentNumber,
        LocalDate issueDate,
        String issuePlace,
        LocalDate expiryDate,
        String countryOfIssue,
        boolean primary,
        String status
) {
}
