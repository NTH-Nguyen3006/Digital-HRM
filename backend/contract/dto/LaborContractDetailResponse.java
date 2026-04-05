package com.company.hrm.module.contract.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LaborContractDetailResponse(
        Long laborContractId,
        Long employeeId,
        String employeeCode,
        String employeeFullName,
        Long contractTypeId,
        String contractTypeCode,
        String contractTypeName,
        String contractNumber,
        LocalDate signDate,
        LocalDate effectiveDate,
        LocalDate endDate,
        Long jobTitleId,
        String jobTitleCode,
        String jobTitleName,
        Long orgUnitId,
        String orgUnitCode,
        String orgUnitName,
        String workLocation,
        BigDecimal baseSalary,
        String salaryCurrency,
        String workingType,
        String contractStatus,
        UUID signedByCompanyUserId,
        String signedByCompanyUsername,
        String note,
        List<ContractAppendixResponse> appendices,
        List<ContractAttachmentResponse> attachments
) {
}
