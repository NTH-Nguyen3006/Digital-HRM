package com.company.hrm.module.employee.dto;

import com.company.hrm.common.constant.IdentificationDocumentType;
import com.company.hrm.common.constant.IdentificationStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record EmployeeIdentificationRequest(
        @NotNull(message = "documentType không được để trống.")
        IdentificationDocumentType documentType,
        @NotBlank(message = "documentNumber không được để trống.")
        @Size(max = 50, message = "documentNumber tối đa 50 ký tự.")
        String documentNumber,
        LocalDate issueDate,
        @Size(max = 200, message = "issuePlace tối đa 200 ký tự.")
        String issuePlace,
        LocalDate expiryDate,
        @Size(max = 100, message = "countryOfIssue tối đa 100 ký tự.")
        String countryOfIssue,
        boolean primary,
        @NotNull(message = "status không được để trống.")
        IdentificationStatus status
) {
}
