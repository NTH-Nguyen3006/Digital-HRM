package com.company.hrm.module.employee.dto;

import com.company.hrm.common.constant.EmploymentStatus;
import com.company.hrm.common.constant.GenderCode;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateEmployeeRequest(
        @NotBlank(message = "employeeCode không được để trống.")
        @Size(max = 30, message = "employeeCode tối đa 30 ký tự.")
        String employeeCode,
        @NotNull(message = "orgUnitId không được để trống.")
        Long orgUnitId,
        @NotNull(message = "jobTitleId không được để trống.")
        Long jobTitleId,
        Long managerEmployeeId,
        @NotBlank(message = "fullName không được để trống.")
        @Size(max = 200, message = "fullName tối đa 200 ký tự.")
        String fullName,
        @Email(message = "workEmail không đúng định dạng.")
        @Size(max = 150, message = "workEmail tối đa 150 ký tự.")
        String workEmail,
        @Size(max = 20, message = "workPhone tối đa 20 ký tự.")
        String workPhone,
        @NotNull(message = "genderCode không được để trống.")
        GenderCode genderCode,
        @NotNull(message = "dateOfBirth không được để trống.")
        LocalDate dateOfBirth,
        @NotNull(message = "hireDate không được để trống.")
        LocalDate hireDate,
        @NotNull(message = "employmentStatus không được để trống.")
        EmploymentStatus employmentStatus,
        @Size(max = 200, message = "workLocation tối đa 200 ký tự.")
        String workLocation,
        @Size(max = 30, message = "taxCode tối đa 30 ký tự.")
        String taxCode,
        @Email(message = "personalEmail không đúng định dạng.")
        @Size(max = 150, message = "personalEmail tối đa 150 ký tự.")
        String personalEmail,
        @Size(max = 20, message = "mobilePhone tối đa 20 ký tự.")
        String mobilePhone,
        @Size(max = 500, message = "avatarUrl tối đa 500 ký tự.")
        String avatarUrl,
        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
