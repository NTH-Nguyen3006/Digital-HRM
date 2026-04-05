package com.company.hrm.module.onboarding.dto;

import com.company.hrm.common.constant.ContractWorkingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateInitialContractFromOnboardingRequest(
        @NotNull(message = "contractTypeId là bắt buộc.")
        Long contractTypeId,

        @NotBlank(message = "contractNumber là bắt buộc.")
        @Size(max = 50, message = "contractNumber tối đa 50 ký tự.")
        String contractNumber,

        @NotNull(message = "signDate là bắt buộc.")
        LocalDate signDate,

        @NotNull(message = "effectiveDate là bắt buộc.")
        LocalDate effectiveDate,

        LocalDate endDate,

        @NotNull(message = "baseSalary là bắt buộc.")
        @DecimalMin(value = "0.01", message = "baseSalary phải lớn hơn 0.")
        BigDecimal baseSalary,

        @NotNull(message = "workingType là bắt buộc.")
        ContractWorkingType workingType,

        @Size(max = 200, message = "workLocation tối đa 200 ký tự.")
        String workLocation,

        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
