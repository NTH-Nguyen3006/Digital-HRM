package com.company.hrm.module.role.dto;

import com.company.hrm.common.constant.DataScopeCode;
import com.company.hrm.common.constant.DataScopeTargetType;
import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record DataScopeRequest(
        @NotNull(message = "scopeCode là bắt buộc.")
        DataScopeCode scopeCode,

        DataScopeTargetType targetType,

        String targetRefId,

        boolean inclusive,

        Integer priorityOrder,

        LocalDateTime effectiveFrom,

        LocalDateTime effectiveTo,

        RecordStatus status
) {
}
