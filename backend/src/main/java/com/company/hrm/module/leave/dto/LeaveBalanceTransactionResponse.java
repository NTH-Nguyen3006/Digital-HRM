package com.company.hrm.module.leave.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeaveBalanceTransactionResponse(
        Long leaveBalanceTxnId,
        String transactionType,
        BigDecimal quantityDelta,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        LocalDateTime transactionDate,
        String referenceNo,
        String reason
) {
}
