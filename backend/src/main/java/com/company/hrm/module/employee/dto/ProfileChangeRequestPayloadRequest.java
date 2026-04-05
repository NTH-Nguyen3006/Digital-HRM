package com.company.hrm.module.employee.dto;

import jakarta.validation.constraints.Size;

public record ProfileChangeRequestPayloadRequest(
        @Size(max = 150, message = "personalEmail tối đa 150 ký tự.")
        String personalEmail,

        @Size(max = 20, message = "mobilePhone tối đa 20 ký tự.")
        String mobilePhone,

        @Size(max = 500, message = "avatarUrl tối đa 500 ký tự.")
        String avatarUrl,

        @Size(max = 30, message = "taxCode tối đa 30 ký tự.")
        String taxCode,

        @Size(max = 100, message = "nationality tối đa 100 ký tự.")
        String nationality,

        @Size(max = 200, message = "placeOfBirth tối đa 200 ký tự.")
        String placeOfBirth,

        @Size(max = 100, message = "ethnicGroup tối đa 100 ký tự.")
        String ethnicGroup,

        @Size(max = 100, message = "religion tối đa 100 ký tự.")
        String religion,

        String maritalStatus,

        @Size(max = 200, message = "educationLevel tối đa 200 ký tự.")
        String educationLevel,

        @Size(max = 200, message = "major tối đa 200 ký tự.")
        String major,

        @Size(max = 500, message = "emergencyNote tối đa 500 ký tự.")
        String emergencyNote
) {
}
