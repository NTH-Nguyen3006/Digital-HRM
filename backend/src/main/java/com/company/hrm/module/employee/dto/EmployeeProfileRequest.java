package com.company.hrm.module.employee.dto;

import com.company.hrm.common.constant.MaritalStatus;
import jakarta.validation.constraints.Size;

public record EmployeeProfileRequest(
        @Size(max = 100, message = "firstName tối đa 100 ký tự.")
        String firstName,
        @Size(max = 100, message = "middleName tối đa 100 ký tự.")
        String middleName,
        @Size(max = 100, message = "lastName tối đa 100 ký tự.")
        String lastName,
        MaritalStatus maritalStatus,
        @Size(max = 100, message = "nationality tối đa 100 ký tự.")
        String nationality,
        @Size(max = 200, message = "placeOfBirth tối đa 200 ký tự.")
        String placeOfBirth,
        @Size(max = 100, message = "ethnicGroup tối đa 100 ký tự.")
        String ethnicGroup,
        @Size(max = 100, message = "religion tối đa 100 ký tự.")
        String religion,
        @Size(max = 200, message = "educationLevel tối đa 200 ký tự.")
        String educationLevel,
        @Size(max = 200, message = "major tối đa 200 ký tự.")
        String major,
        @Size(max = 500, message = "emergencyNote tối đa 500 ký tự.")
        String emergencyNote
) {
}
