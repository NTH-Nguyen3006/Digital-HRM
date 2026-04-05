package com.company.hrm.module.employee.dto;

public record EmployeeAddressResponse(
        Long employeeAddressId,
        Long employeeId,
        String addressType,
        String countryName,
        String provinceName,
        String districtName,
        String wardName,
        String addressLine,
        String postalCode,
        boolean primary
) {
}
