package com.company.hrm.module.auth.controller;

import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.auth.dto.AuthResult;
import com.company.hrm.module.auth.dto.ChangePasswordRequest;
import com.company.hrm.module.auth.dto.ForgotPasswordRequest;
import com.company.hrm.module.auth.dto.LoginRequest;
import com.company.hrm.module.auth.dto.LoginResponse;
import com.company.hrm.module.auth.dto.RefreshTokenRequest;
import com.company.hrm.module.auth.dto.ResetPasswordRequest;
import com.company.hrm.module.auth.service.AuthService;
import com.company.hrm.module.auth.support.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

    public AuthController(AuthService authService, AuthCookieService authCookieService) {
        this.authService = authService;
        this.authCookieService = authCookieService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("AUTH_LOGIN_SUCCESS", "Đăng nhập thành công.", response, null,
                RequestTraceContext.getTraceId());
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refresh(request);
        return ApiResponse.success("AUTH_REFRESH_SUCCESS", "Làm mới access token thành công.", response, null,
                RequestTraceContext.getTraceId());
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('auth.logout')")
    public ApiResponse<Void> logout(HttpServletResponse httpServletResponse) {
        authService.logoutCurrentSession();
        authCookieService.clearAuthenticationCookies(httpServletResponse);
        return ApiResponse.success("AUTH_LOGOUT_SUCCESS", "Đăng xuất thành công.", RequestTraceContext.getTraceId());
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.success("AUTH_FORGOT_PASSWORD_ACCEPTED",
                "Nếu email hợp lệ, hệ thống sẽ gửi hướng dẫn đặt lại mật khẩu.", RequestTraceContext.getTraceId());
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("AUTH_RESET_PASSWORD_SUCCESS", "Đặt lại mật khẩu thành công.",
                RequestTraceContext.getTraceId());
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasAuthority('auth.change_password')")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletResponse httpServletResponse) {
        authService.changePassword(request);
        return ApiResponse.success("AUTH_CHANGE_PASSWORD_SUCCESS", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.",
                RequestTraceContext.getTraceId());
    }
}
