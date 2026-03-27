package com.company.hrm.common.util;

import com.company.hrm.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class PasswordPolicyValidator {

    private PasswordPolicyValidator() {
    }

    public static void validate(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new BusinessException("PASSWORD_REQUIRED", "Mật khẩu mới là bắt buộc.", HttpStatus.BAD_REQUEST);
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException("PASSWORD_CONFIRM_NOT_MATCH", "Mật khẩu xác nhận không khớp.", HttpStatus.BAD_REQUEST);
        }

        if (newPassword.length() < 8) {
            throw new BusinessException("PASSWORD_TOO_SHORT", "Mật khẩu phải có ít nhất 8 ký tự.", HttpStatus.BAD_REQUEST);
        }

        if (!newPassword.matches(".*[A-Z].*")) {
            throw new BusinessException("PASSWORD_UPPERCASE_REQUIRED", "Mật khẩu phải có ít nhất 1 chữ in hoa.", HttpStatus.BAD_REQUEST);
        }

        if (!newPassword.matches(".*[a-z].*")) {
            throw new BusinessException("PASSWORD_LOWERCASE_REQUIRED", "Mật khẩu phải có ít nhất 1 chữ thường.", HttpStatus.BAD_REQUEST);
        }

        if (!newPassword.matches(".*\\d.*")) {
            throw new BusinessException("PASSWORD_DIGIT_REQUIRED", "Mật khẩu phải có ít nhất 1 chữ số.", HttpStatus.BAD_REQUEST);
        }

        if (!newPassword.matches(".*[^A-Za-z0-9].*")) {
            throw new BusinessException("PASSWORD_SPECIAL_CHAR_REQUIRED", "Mật khẩu phải có ít nhất 1 ký tự đặc biệt.", HttpStatus.BAD_REQUEST);
        }
    }
}
