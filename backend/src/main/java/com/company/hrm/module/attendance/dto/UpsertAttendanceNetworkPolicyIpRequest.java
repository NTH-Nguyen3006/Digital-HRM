package com.company.hrm.module.attendance.dto;

import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertAttendanceNetworkPolicyIpRequest(
        @NotBlank(message = "cidrOrIp là bắt buộc.")
        @Size(max = 64, message = "cidrOrIp tối đa 64 ký tự.")
        String cidrOrIp,

        @Size(max = 250, message = "description tối đa 250 ký tự.")
        String description,

        RecordStatus status
) {
}
