package com.company.hrm.module.offboarding.dto;

import com.company.hrm.common.constant.OffboardingChecklistItemType;
import com.company.hrm.common.constant.OffboardingChecklistStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpsertOffboardingChecklistItemRequest(
        @NotNull(message = "itemType là bắt buộc.")
        OffboardingChecklistItemType itemType,

        @NotBlank(message = "itemName là bắt buộc.")
        @Size(max = 255, message = "itemName tối đa 255 ký tự.")
        String itemName,

        @Size(max = 30, message = "ownerRoleCode tối đa 30 ký tự.")
        String ownerRoleCode,

        LocalDate dueDate,

        OffboardingChecklistStatus status,

        @Size(max = 1000, message = "note tối đa 1000 ký tự.")
        String note
) {
}
