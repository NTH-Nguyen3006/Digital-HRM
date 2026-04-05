package com.company.hrm.module.employee.dto;

public record EmergencyContactResponse(
        Long emergencyContactId,
        Long employeeId,
        String contactName,
        String relationshipCode,
        String phoneNumber,
        String email,
        String addressLine,
        boolean primary,
        String note
) {
}
