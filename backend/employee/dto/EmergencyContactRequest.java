package com.company.hrm.module.employee.dto;

import com.company.hrm.common.constant.RelationshipCode;
import jakarta.validation.constraints.*;

public record EmergencyContactRequest(
        @NotBlank(message = "contactName không được để trống.")
        @Size(max = 200, message = "contactName tối đa 200 ký tự.")
        String contactName,
        @NotNull(message = "relationshipCode không được để trống.")
        RelationshipCode relationshipCode,
        @NotBlank(message = "phoneNumber không được để trống.")
        @Size(max = 20, message = "phoneNumber tối đa 20 ký tự.")
        String phoneNumber,
        @Email(message = "email không đúng định dạng.")
        @Size(max = 150, message = "email tối đa 150 ký tự.")
        String email,
        @Size(max = 300, message = "addressLine tối đa 300 ký tự.")
        String addressLine,
        boolean primary,
        @Size(max = 500, message = "note tối đa 500 ký tự.")
        String note
) {
}
