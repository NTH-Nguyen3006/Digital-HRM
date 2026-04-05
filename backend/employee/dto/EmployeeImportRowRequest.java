package com.company.hrm.module.employee.dto;

import com.company.hrm.common.constant.EmploymentStatus;
import com.company.hrm.common.constant.GenderCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EmployeeImportRowRequest(
        @NotBlank(message = "employeeCode là bắt buộc.")
        @Size(max = 30, message = "employeeCode tối đa 30 ký tự.")
        String employeeCode,

        @NotBlank(message = "fullName là bắt buộc.")
        @Size(max = 200, message = "fullName tối đa 200 ký tự.")
        String fullName,

        @NotBlank(message = "orgUnitCode là bắt buộc.")
        String orgUnitCode,

        @NotBlank(message = "jobTitleCode là bắt buộc.")
        String jobTitleCode,

        Long managerEmployeeId,

        @NotNull(message = "genderCode là bắt buộc.")
        GenderCode genderCode,

        @NotNull(message = "dateOfBirth là bắt buộc.")
        LocalDate dateOfBirth,

        @NotNull(message = "hireDate là bắt buộc.")
        LocalDate hireDate,

        @NotNull(message = "employmentStatus là bắt buộc.")
        EmploymentStatus employmentStatus,

        String workEmail,
        String workPhone,
        String workLocation,
        String taxCode,
        String personalEmail,
        String mobilePhone,
        String avatarUrl,
        String note,
        String nationality,
        String placeOfBirth,
        String ethnicGroup,
        String religion,
        String maritalStatus,
        String educationLevel,
        String major,
        String emergencyNote
) {
}
