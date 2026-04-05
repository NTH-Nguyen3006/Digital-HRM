package com.company.hrm.module.payroll.dto;

import java.math.BigDecimal;

public record FormulaTaxBracketResponse(
        Long formulaTaxBracketId,
        Integer sequenceNo,
        BigDecimal incomeFrom,
        BigDecimal incomeTo,
        BigDecimal taxRate,
        BigDecimal quickDeduction
) {
}
