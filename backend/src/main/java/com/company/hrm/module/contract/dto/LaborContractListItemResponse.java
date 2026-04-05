package com.company.hrm.module.contract.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LaborContractListItemResponse(
        Long laborContractId,
        String contractNumber,
        Long employeeId,
        String employeeCode,
        String employeeFullName,
        Long contractTypeId,
        String contractTypeCode,
        String contractTypeName,
        LocalDate signDate,
        LocalDate effectiveDate,
        LocalDate endDate,
        Long jobTitleId,
        String jobTitleCode,
        String jobTitleName,
        Long orgUnitId,
        String orgUnitCode,
        String orgUnitName,
        String workingType,
        BigDecimal baseSalary,
        String salaryCurrency,
        String contractStatus,
        Integer expiringInDays
) {
}
