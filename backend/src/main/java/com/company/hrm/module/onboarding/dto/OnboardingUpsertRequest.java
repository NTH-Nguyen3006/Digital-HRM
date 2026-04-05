package com.company.hrm.module.onboarding.dto;

import com.company.hrm.common.constant.GenderCode;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record OnboardingUpsertRequest(
        @NotBlank(message = "onboardingCode là bắt buộc.")
        @Size(max = 30, message = "onboardingCode tối đa 30 ký tự.")
        String onboardingCode,

        @NotBlank(message = "fullName là bắt buộc.")
        @Size(max = 200, message = "fullName tối đa 200 ký tự.")
        String fullName,

        @Email(message = "personalEmail không hợp lệ.")
        @Size(max = 150, message = "personalEmail tối đa 150 ký tự.")
        String personalEmail,

        @Size(max = 20, message = "personalPhone tối đa 20 ký tự.")
        String personalPhone,

        @NotNull(message = "genderCode là bắt buộc.")
        GenderCode genderCode,

        @NotNull(message = "dateOfBirth là bắt buộc.")
        LocalDate dateOfBirth,

        @NotNull(message = "plannedStartDate là bắt buộc.")
        LocalDate plannedStartDate,

        @Size(max = 30, message = "employeeCode tối đa 30 ký tự.")
        String employeeCode,

        @NotNull(message = "orgUnitId là bắt buộc.")
        Long orgUnitId,

        @NotNull(message = "jobTitleId là bắt buộc.")
        Long jobTitleId,

        Long managerEmployeeId,

        @Size(max = 200, message = "workLocation tối đa 200 ký tự.")
        String workLocation,

        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
