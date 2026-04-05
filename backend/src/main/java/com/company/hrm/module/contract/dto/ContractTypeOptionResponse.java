package com.company.hrm.module.contract.dto;

public record ContractTypeOptionResponse(
        Long contractTypeId,
        String contractTypeCode,
        String contractTypeName,
        boolean requiresEndDate,
        Integer maxTermMonths
) {
}
