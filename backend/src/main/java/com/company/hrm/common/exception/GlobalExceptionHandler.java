package com.company.hrm.common.exception;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.ValidationError;
import com.company.hrm.module.audit.support.RequestTraceContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.failure(ex.getCode(), ex.getMessage(), ex.getMeta(), RequestTraceContext.getTraceId()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ValidationError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toValidationError)
                .collect(Collectors.toList());

        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("VALIDATION_ERROR", "Dữ liệu đầu vào không hợp lệ.", errors, RequestTraceContext.getTraceId()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Giá trị tham số không hợp lệ: " + ex.getName();
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("REQUEST_PARAM_INVALID", message, ex.getValue(), RequestTraceContext.getTraceId()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("REQUEST_BODY_INVALID", "Body request không hợp lệ hoặc không parse được.", null, RequestTraceContext.getTraceId()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure("ACCESS_DENIED", "Bạn không có quyền thực hiện thao tác này.", null, RequestTraceContext.getTraceId()));
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure("AUTH_INVALID_CREDENTIALS", "Thông tin đăng nhập không chính xác.", null, RequestTraceContext.getTraceId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(
                        "INTERNAL_SERVER_ERROR",
                        "Đã xảy ra lỗi nội bộ. Vui lòng kiểm tra log hệ thống.",
                        request.getRequestURI(),
                        RequestTraceContext.getTraceId()
                ));
    }

    private ValidationError toValidationError(FieldError fieldError) {
        return new ValidationError(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
