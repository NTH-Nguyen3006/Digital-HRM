package com.company.hrm.module.employee.dto;

public record EmployeeBankAccountResponse(
        Long employeeBankAccountId,
        Long employeeId,
        String bankName,
        String bankCode,
        String accountNumber,
        String accountHolderName,
        String branchName,
        boolean primary,
        String status
) {
}
