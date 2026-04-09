package com.company.hrm.module.attendance.dto;

import com.company.hrm.common.constant.AttendanceNetworkPolicyScopeType;
import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpsertAttendanceNetworkPolicyRequest(
        @NotBlank(message = "policyCode là bắt buộc.")
        @Size(max = 30, message = "policyCode tối đa 30 ký tự.")
        String policyCode,

        @NotBlank(message = "policyName là bắt buộc.")
        @Size(max = 200, message = "policyName tối đa 200 ký tự.")
        String policyName,

        @NotNull(message = "scopeType là bắt buộc.")
        AttendanceNetworkPolicyScopeType scopeType,

        Long orgUnitId,

        Boolean allowCheckIn,
        Boolean allowCheckOut,

        LocalDate effectiveFrom,
        LocalDate effectiveTo,

        @Size(max = 500, message = "description tối đa 500 ký tự.")
        String description,

        RecordStatus status
) {
}
