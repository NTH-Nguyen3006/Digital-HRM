package com.company.hrm.module.auth.service;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.ResetChannel;
import com.company.hrm.common.constant.ResetTokenStatus;
import com.company.hrm.common.constant.SessionStatus;
import com.company.hrm.common.constant.UserStatus;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.UnauthorizedException;
import com.company.hrm.common.util.HashUtils;
import com.company.hrm.common.util.PasswordPolicyValidator;
import com.company.hrm.common.util.RequestContextUtils;
import com.company.hrm.config.AppProperties;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.auth.dto.AuthResult;
import com.company.hrm.module.auth.dto.ChangePasswordRequest;
import com.company.hrm.module.auth.dto.ForgotPasswordRequest;
import com.company.hrm.module.auth.dto.LoginRequest;
import com.company.hrm.module.auth.dto.LoginResponse;
import com.company.hrm.module.auth.dto.ResetPasswordRequest;
import com.company.hrm.module.auth.entity.SecAuthSession;
import com.company.hrm.module.auth.entity.SecPasswordResetToken;
import com.company.hrm.module.auth.repository.SecAuthSessionRepository;
import com.company.hrm.module.auth.repository.SecPasswordResetTokenRepository;
import com.company.hrm.module.role.repository.SecRolePermissionRepository;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.entity.SecUserRole;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.module.user.repository.SecUserRoleRepository;
import com.company.hrm.security.JwtTokenProvider;
import com.company.hrm.security.SecurityUserContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecUserAccountRepository userAccountRepository;
    private final SecUserRoleRepository userRoleRepository;
    private final SecRolePermissionRepository rolePermissionRepository;
    private final SecAuthSessionRepository authSessionRepository;
    private final SecPasswordResetTokenRepository passwordResetTokenRepository;
    private final AuditLogService auditLogService;
    private final MailService mailService;

    public AuthService(
            AppProperties appProperties,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            SecUserAccountRepository userAccountRepository,
            SecUserRoleRepository userRoleRepository,
            SecRolePermissionRepository rolePermissionRepository,
            SecAuthSessionRepository authSessionRepository,
            SecPasswordResetTokenRepository passwordResetTokenRepository,
            AuditLogService auditLogService,
            MailService mailService
    ) {
        this.appProperties = appProperties;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userAccountRepository = userAccountRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.auditLogService = auditLogService;
        this.mailService = mailService;
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        SecUserAccount user = findByLoginId(request.loginId());

        if (user == null || user.isDeleted()) {
            auditLogService.logSystemFailure(request.loginId(), "LOGIN_FAILED", "AUTH", "sec_user_account", null, null, null, "Không tìm thấy user.");
            throw new UnauthorizedException("AUTH_INVALID_CREDENTIALS", "Thông tin đăng nhập không chính xác.");
        }

        autoUnlockIfExpired(user);

        if (user.getStatus() == UserStatus.LOCKED || user.isLockedByPolicy()) {
            auditLogService.logSystemFailure(user.getUsername(), "LOGIN_FAILED", "AUTH", "sec_user_account", user.getUserId().toString(), null, null, "Tài khoản đang bị khóa.");
            throw new UnauthorizedException("AUTH_USER_LOCKED", "Tài khoản đang bị khóa.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            auditLogService.logSystemFailure(user.getUsername(), "LOGIN_FAILED", "AUTH", "sec_user_account", user.getUserId().toString(), null, null, "Tài khoản chưa active.");
            throw new UnauthorizedException("AUTH_USER_INACTIVE", "Tài khoản chưa sẵn sàng để đăng nhập.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedLogin(user);
            auditLogService.logSystemFailure(user.getUsername(), "LOGIN_FAILED", "AUTH", "sec_user_account", user.getUserId().toString(), null, null, "Sai mật khẩu.");
            throw new UnauthorizedException("AUTH_INVALID_CREDENTIALS", "Thông tin đăng nhập không chính xác.");
        }

        SecUserRole activeRole = userRoleRepository.findActivePrimaryRole(user.getUserId(), LocalDateTime.now())
                .orElseThrow(() -> new UnauthorizedException("AUTH_ROLE_NOT_FOUND", "Tài khoản chưa được gán role active."));

        if (activeRole.getRole().getStatus() != RecordStatus.ACTIVE) {
            throw new UnauthorizedException("AUTH_ROLE_INACTIVE", "Role chính của tài khoản đang không hoạt động.");
        }

        List<String> permissions = rolePermissionRepository.findAllowedPermissionCodes(activeRole.getRole().getRoleId());

        LocalDateTime now = LocalDateTime.now();
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        user.setLastLoginIp(RequestContextUtils.getClientIp());
        userAccountRepository.save(user);

        String rawRefreshToken = UUID.randomUUID().toString() + UUID.randomUUID();
        SecAuthSession authSession = new SecAuthSession();
        authSession.setUser(user);
        authSession.setRefreshTokenHash(HashUtils.sha256(rawRefreshToken));
        authSession.setDeviceName(request.deviceName());
        authSession.setDeviceOs(request.deviceOs());
        authSession.setBrowserName(request.browserName());
        authSession.setIpAddress(RequestContextUtils.getClientIp());
        authSession.setUserAgent(RequestContextUtils.getUserAgent());
        authSession.setLoginAt(now);
        authSession.setExpiresAt(now.plusDays(appProperties.getAuth().getRefreshTokenDays()));
        authSession.setStatus(SessionStatus.ACTIVE);
        authSessionRepository.save(authSession);

        AuthResult result = buildAuthResult(user, activeRole, permissions, authSession.getAuthSessionId(), rawRefreshToken);
        auditLogService.logSystemSuccess(user.getUsername(), "LOGIN", "AUTH", "sec_user_account", user.getUserId().toString(), null, result.response(), "Đăng nhập thành công.");
        return result;
    }

    @Transactional
    public AuthResult refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("AUTH_REFRESH_INVALID", "Refresh token không hợp lệ.");
        }

        SecAuthSession session = authSessionRepository.findByRefreshTokenHashAndStatus(
                        HashUtils.sha256(refreshToken),
                        SessionStatus.ACTIVE
                )
                .orElseThrow(() -> new UnauthorizedException("AUTH_REFRESH_INVALID", "Refresh token không hợp lệ."));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            authSessionRepository.save(session);
            throw new UnauthorizedException("AUTH_REFRESH_EXPIRED", "Refresh token đã hết hạn.");
        }

        SecUserAccount user = session.getUser();
        if (user.getStatus() != UserStatus.ACTIVE || user.isDeleted()) {
            throw new UnauthorizedException("AUTH_USER_INACTIVE", "Tài khoản không còn hiệu lực.");
        }

        SecUserRole activeRole = userRoleRepository.findActivePrimaryRole(user.getUserId(), LocalDateTime.now())
                .orElseThrow(() -> new UnauthorizedException("AUTH_ROLE_NOT_FOUND", "Tài khoản chưa được gán role active."));

        List<String> permissions = rolePermissionRepository.findAllowedPermissionCodes(activeRole.getRole().getRoleId());
        return buildAuthResult(user, activeRole, permissions, session.getAuthSessionId(), refreshToken);
    }

    @Transactional
    public void logoutCurrentSession() {
        UUID sessionId = SecurityUserContext.getCurrentSessionId()
                .orElseThrow(() -> new UnauthorizedException("AUTH_SESSION_NOT_FOUND", "Không tìm thấy phiên làm việc hiện tại."));

        SecAuthSession session = authSessionRepository.findByAuthSessionId(sessionId)
                .orElseThrow(() -> new UnauthorizedException("AUTH_SESSION_NOT_FOUND", "Không tìm thấy phiên làm việc hiện tại."));

        session.setStatus(SessionStatus.REVOKED);
        session.setRevokedAt(LocalDateTime.now());
        session.setRevokeReason("LOGOUT");
        authSessionRepository.save(session);

        auditLogService.logSuccess("LOGOUT", "AUTH", "sec_auth_session", sessionId.toString(), null, null, "Đăng xuất phiên hiện tại.");
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        SecUserAccount user = userAccountRepository.findByEmailIgnoreCaseAndDeletedFalse(request.email()).orElse(null);
        if (user == null) {
            return;
        }

        List<SecPasswordResetToken> activeTokens = passwordResetTokenRepository.findAllByUserUserIdAndStatus(
                user.getUserId(),
                ResetTokenStatus.PENDING
        );
        activeTokens.forEach(token -> token.setStatus(ResetTokenStatus.REVOKED));
        passwordResetTokenRepository.saveAll(activeTokens);

        String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
        SecPasswordResetToken token = new SecPasswordResetToken();
        token.setUser(user);
        token.setTokenHash(HashUtils.sha256(rawToken));
        token.setChannel(ResetChannel.EMAIL);
        token.setIssuedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(appProperties.getAuth().getPasswordResetMinutes()));
        token.setStatus(ResetTokenStatus.PENDING);
        passwordResetTokenRepository.save(token);

        String resetLink = appProperties.getMail().getResetUrlBase() + "?token=" + rawToken;
        mailService.sendPasswordResetMail(user.getEmail(), user.getUsername(), resetLink);
        auditLogService.logSystemSuccess(user.getUsername(), "FORGOT_PASSWORD", "AUTH", "sec_password_reset_token", token.getPasswordResetTokenId().toString(), null, null, "Phát hành reset token qua email.");
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordPolicyValidator.validate(request.newPassword(), request.confirmPassword());

        SecPasswordResetToken token = passwordResetTokenRepository.findByTokenHashAndStatus(HashUtils.sha256(request.token()), ResetTokenStatus.PENDING)
                .orElseThrow(() -> new BusinessException("RESET_TOKEN_INVALID", "Token reset không hợp lệ.", HttpStatus.BAD_REQUEST));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            token.setStatus(ResetTokenStatus.EXPIRED);
            passwordResetTokenRepository.save(token);
            throw new BusinessException("RESET_TOKEN_EXPIRED", "Token reset đã hết hạn.", HttpStatus.BAD_REQUEST);
        }

        SecUserAccount user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        if (user.getStatus() == UserStatus.LOCKED || user.getStatus() == UserStatus.PENDING_ACTIVATION) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userAccountRepository.save(user);

        token.setStatus(ResetTokenStatus.USED);
        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);

        revokeAllSessions(user.getUserId(), "PASSWORD_RESET");
        auditLogService.logSystemSuccess(user.getUsername(), "RESET_PASSWORD", "AUTH", "sec_user_account", user.getUserId().toString(), null, null, "Reset password thành công.");
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        PasswordPolicyValidator.validate(request.newPassword(), request.confirmPassword());

        UUID userId = SecurityUserContext.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("AUTH_USER_NOT_FOUND", "Không tìm thấy người dùng hiện tại."));

        SecUserAccount user = userAccountRepository.findById(userId)
                .filter(account -> !account.isDeleted())
                .orElseThrow(() -> new UnauthorizedException("AUTH_USER_NOT_FOUND", "Không tìm thấy người dùng hiện tại."));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("CURRENT_PASSWORD_INVALID", "Mật khẩu hiện tại không chính xác.", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        userAccountRepository.save(user);

        revokeAllSessions(userId, "PASSWORD_CHANGED");
        auditLogService.logSuccess("CHANGE_PASSWORD", "AUTH", "sec_user_account", userId.toString(), null, null, "Đổi mật khẩu thành công.");
    }

    private SecUserAccount findByLoginId(String loginId) {
        String normalized = loginId == null ? null : loginId.trim();
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        return userAccountRepository.findByUsernameIgnoreCaseAndDeletedFalse(normalized)
                .or(() -> userAccountRepository.findByEmailIgnoreCaseAndDeletedFalse(normalized))
                .orElse(null);
    }

    private void handleFailedLogin(SecUserAccount user) {
        int failed = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(failed);

        if (failed >= appProperties.getAuth().getMaxFailedLoginAttempts()) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(appProperties.getAuth().getAutoLockMinutes()));
        }

        userAccountRepository.save(user);
    }

    private void autoUnlockIfExpired(SecUserAccount user) {
        if (user.getStatus() == UserStatus.LOCKED
                && user.getLockedUntil() != null
                && user.getLockedUntil().isBefore(LocalDateTime.now())) {
            user.setStatus(UserStatus.ACTIVE);
            user.setLockedUntil(null);
            user.setFailedLoginCount(0);
            userAccountRepository.save(user);
        }
    }

    private void revokeAllSessions(UUID userId, String reason) {
        List<SecAuthSession> sessions = authSessionRepository.findAllByUserUserIdAndStatus(userId, SessionStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        sessions.forEach(session -> {
            session.setStatus(SessionStatus.REVOKED);
            session.setRevokedAt(now);
            session.setRevokeReason(reason);
        });
        authSessionRepository.saveAll(sessions);
    }

    private AuthResult buildAuthResult(
            SecUserAccount user,
            SecUserRole activeRole,
            List<String> permissions,
            UUID sessionId,
            String refreshToken
    ) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(),
                sessionId,
                user.getUsername(),
                user.getEmail(),
                activeRole.getRole().getRoleCode(),
                permissions
        );

        LoginResponse response = new LoginResponse(
                "COOKIE",
                appProperties.getAuth().getAccessTokenMinutes() * 60,
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                activeRole.getRole().getRoleCode().name(),
                permissions,
                user.isMustChangePassword(),
                resolveHomeRoute(activeRole.getRole().getRoleCode().name())
        );

        return new AuthResult(response, accessToken, refreshToken);
    }

    private String resolveHomeRoute(String roleCode) {
        return switch (roleCode) {
            case "ADMIN" -> "/admin";
            case "HR" -> "/hr";
            case "MANAGER" -> "/manager";
            default -> "/employee";
        };
    }
}
