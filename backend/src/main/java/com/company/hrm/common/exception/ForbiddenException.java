package com.company.hrm.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {
    public ForbiddenException(String code, String message) {
        super(code, message, HttpStatus.FORBIDDEN);
    }
}
