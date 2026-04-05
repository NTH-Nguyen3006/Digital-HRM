package com.company.hrm.module.contract.dto;

import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateContractTypeStatusRequest(
        @NotNull(message = "status là bắt buộc.")
        RecordStatus status,

        @Size(max = 500, message = "note tối đa 500 ký tự.")
        String note
) {
}
