package com.company.hrm.module.contract.dto;

public record ContractTypeDetailResponse(
        Long contractTypeId,
        String contractTypeCode,
        String contractTypeName,
        Integer maxTermMonths,
        boolean requiresEndDate,
        String status,
        String description
) {
}
