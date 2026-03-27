package com.company.hrm.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus status;
    private final Object meta;

    public BusinessException(String code, String message, HttpStatus status) {
        this(code, message, status, null);
    }

    public BusinessException(String code, String message, HttpStatus status, Object meta) {
        super(message);
        this.code = code;
        this.status = status;
        this.meta = meta;
    }
}
