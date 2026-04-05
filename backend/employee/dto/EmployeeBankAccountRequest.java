package com.company.hrm.module.employee.dto;

import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.*;

public record EmployeeBankAccountRequest(
        @NotBlank(message = "bankName không được để trống.")
        @Size(max = 200, message = "bankName tối đa 200 ký tự.")
        String bankName,
        @Size(max = 30, message = "bankCode tối đa 30 ký tự.")
        String bankCode,
        @NotBlank(message = "accountNumber không được để trống.")
        @Size(max = 50, message = "accountNumber tối đa 50 ký tự.")
        String accountNumber,
        @NotBlank(message = "accountHolderName không được để trống.")
        @Size(max = 200, message = "accountHolderName tối đa 200 ký tự.")
        String accountHolderName,
        @Size(max = 200, message = "branchName tối đa 200 ký tự.")
        String branchName,
        boolean primary,
        @NotNull(message = "status không được để trống.")
        RecordStatus status
) {
}
