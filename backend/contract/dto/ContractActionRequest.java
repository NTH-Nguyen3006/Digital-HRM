package com.company.hrm.module.contract.dto;

import jakarta.validation.constraints.Size;

public record ContractActionRequest(
        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
