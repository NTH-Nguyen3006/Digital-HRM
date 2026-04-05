package com.company.hrm.module.leave.dto;

import com.company.hrm.common.constant.LeaveUnitType;
import com.company.hrm.common.constant.RoleCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LeaveTypeUpsertRequest(
        @NotBlank(message = "leaveTypeCode là bắt buộc.")
        @Size(max = 30, message = "leaveTypeCode tối đa 30 ký tự.")
        String leaveTypeCode,

        @NotBlank(message = "leaveTypeName là bắt buộc.")
        @Size(max = 200, message = "leaveTypeName tối đa 200 ký tự.")
        String leaveTypeName,

        @NotNull(message = "effectiveFrom là bắt buộc.")
        LocalDate effectiveFrom,

        @NotNull(message = "unitType là bắt buộc.")
        LeaveUnitType unitType,

        @NotNull(message = "defaultEntitlementUnits là bắt buộc.")
        @DecimalMin(value = "0.00", inclusive = true, message = "defaultEntitlementUnits không được âm.")
        BigDecimal defaultEntitlementUnits,

        @NotNull(message = "carryForwardMaxUnits là bắt buộc.")
        @DecimalMin(value = "0.00", inclusive = true, message = "carryForwardMaxUnits không được âm.")
        BigDecimal carryForwardMaxUnits,

        boolean paid,
        boolean requiresBalanceCheck,
        boolean requiresAttachment,

        @NotNull(message = "approvalRoleCode là bắt buộc.")
        RoleCode approvalRoleCode,

        boolean allowNegativeBalance,

        @NotNull(message = "minNoticeDays là bắt buộc.")
        @Min(value = 0, message = "minNoticeDays không được âm.")
        Integer minNoticeDays,

        @DecimalMin(value = "0.00", inclusive = false, message = "maxConsecutiveUnits phải lớn hơn 0 nếu có.")
        BigDecimal maxConsecutiveUnits,

        @Size(max = 500, message = "description tối đa 500 ký tự.")
        String description,

        @Size(max = 500, message = "ruleNote tối đa 500 ký tự.")
        String ruleNote
) {
}
