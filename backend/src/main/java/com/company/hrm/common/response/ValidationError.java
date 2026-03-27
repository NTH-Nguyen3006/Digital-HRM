package com.company.hrm.common.response;

public record ValidationError(
        String field,
        String message
) {
}
