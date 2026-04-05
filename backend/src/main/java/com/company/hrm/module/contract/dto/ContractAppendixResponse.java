package com.company.hrm.module.contract.dto;

import java.time.LocalDate;

public record ContractAppendixResponse(
        Long contractAppendixId,
        String appendixNumber,
        String appendixName,
        LocalDate effectiveDate,
        String changedFieldsJson,
        String status,
        String note
) {
}
