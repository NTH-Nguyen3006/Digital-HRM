package com.company.hrm.module.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SelfPayslipListItemResponse(
        Long payrollPeriodId,
        String periodCode,
        Integer periodYear,
        Integer periodMonth,
        String itemStatus,
        BigDecimal grossIncome,
        BigDecimal pitAmount,
        BigDecimal netPay,
        LocalDateTime publishedAt
) {
}
