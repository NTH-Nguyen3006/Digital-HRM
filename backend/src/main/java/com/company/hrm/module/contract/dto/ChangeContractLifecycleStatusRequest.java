package com.company.hrm.module.contract.dto;

import com.company.hrm.common.constant.ContractStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangeContractLifecycleStatusRequest(
        @NotNull(message = "targetStatus là bắt buộc.")
        ContractStatus targetStatus,

        @Size(max = 1000, message = "reason tối đa 1000 ký tự.")
        String reason
) {
}
