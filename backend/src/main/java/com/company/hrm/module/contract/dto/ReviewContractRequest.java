package com.company.hrm.module.contract.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ReviewContractRequest(
        boolean approved,

        UUID signedByCompanyUserId,

        @Size(max = 1000, message = "reason tối đa 1000 ký tự.")
        String reason
) {
}
