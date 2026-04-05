package com.company.hrm.module.contract.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContractStatusHistoryResponse(
        Long contractStatusHistoryId,
        String fromStatus,
        String toStatus,
        LocalDateTime changedAt,
        UUID changedByUserId,
        String changedByUsername,
        String reason
) {
}
