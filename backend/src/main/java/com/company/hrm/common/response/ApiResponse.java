package com.company.hrm.common.response;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        Object meta,
        String traceId,
        OffsetDateTime timestamp
) {
    public static <T> ApiResponse<T> success(String code, String message, T data, Object meta, String traceId) {
        return new ApiResponse<>(true, code, message, data, meta, traceId, OffsetDateTime.now());
    }

    public static ApiResponse<Void> success(String code, String message, String traceId) {
        return new ApiResponse<>(true, code, message, null, null, traceId, OffsetDateTime.now());
    }

    public static ApiResponse<Void> failure(String code, String message, Object meta, String traceId) {
        return new ApiResponse<>(false, code, message, null, meta, traceId, OffsetDateTime.now());
    }
}
