package com.company.hrm.module.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateContractTypeRequest(
        @NotBlank(message = "contractTypeCode là bắt buộc.")
        @Size(max = 30, message = "contractTypeCode tối đa 30 ký tự.")
        String contractTypeCode,

        @NotBlank(message = "contractTypeName là bắt buộc.")
        @Size(max = 200, message = "contractTypeName tối đa 200 ký tự.")
        String contractTypeName,

        @PositiveOrZero(message = "maxTermMonths phải >= 0.")
        Integer maxTermMonths,

        boolean requiresEndDate,

        @Size(max = 500, message = "description tối đa 500 ký tự.")
        String description
) {
}
