package com.company.hrm.module.user.service;

import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.auth.service.MailService;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.constant.SessionStatus;
import com.company.hrm.common.constant.UserStatus;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.config.AppProperties;
import com.company.hrm.module.auth.entity.SecAuthSession;
import com.company.hrm.module.auth.entity.SecPasswordResetToken;
import com.company.hrm.module.role.entity.SecRole;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.entity.SecUserRole;
import com.company.hrm.module.user.dto.AssignPrimaryRoleRequest;
import com.company.hrm.module.user.dto.CreateUserRequest;
import com.company.hrm.module.user.dto.LockUnlockUserRequest;
import com.company.hrm.module.user.dto.UpdateUserRequest;
import com.company.hrm.module.user.dto.UserDetailResponse;
import com.company.hrm.module.user.dto.UserListItemResponse;
import com.company.hrm.module.user.dto.UserRoleHistoryResponse;
import com.company.hrm.module.auth.repository.SecAuthSessionRepository;
import com.company.hrm.module.auth.repository.SecPasswordResetTokenRepository;
import com.company.hrm.module.role.repository.SecRoleRepository;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.module.user.repository.SecUserRoleRepository;
import com.company.hrm.security.SecurityUserContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdminService {

    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;
    private final SecUserAccountRepository userAccountRepository;
    private final SecRoleRepository roleRepository;
    private final SecUserRoleRepository userRoleRepository;
    private final SecPasswordResetTokenRepository passwordResetTokenRepository;
    private final SecAuthSessionRepository authSessionRepository;
    private final AuditLogService auditLogService;
    private final MailService mailService;
    private final HrEmployeeRepository hrEmployeeRepository;

    public UserAdminService(
            AppProperties appProperties,
            PasswordEncoder passwordEncoder,
            SecUserAccountRepository userAccountRepository,
            SecRoleRepository roleRepository,
            SecUserRoleRepository userRoleRepository,
            SecPasswordResetTokenRepository passwordResetTokenRepository,
            SecAuthSessionRepository authSessionRepository,
            AuditLogService auditLogService,
            MailService mailService,
            HrEmployeeRepository hrEmployeeRepository
    ) {
        this.appProperties = appProperties;
        this.passwordEncoder = passwordEncoder;
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.authSessionRepository = authSessionRepository;
        this.auditLogService = auditLogService;
        this.mailService = mailService;
        this.hrEmployeeRepository = hrEmployeeRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserListItemResponse> listUsers(String keyword, UserStatus status, RoleCode roleCode, int page, int size) {
        Specification<SecUserAccount> specification = (root, query, builder) -> builder.isFalse(root.get("deleted"));

        if (keyword != null && !keyword.isBlank()) {
            String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("username")), likeKeyword),
                    builder.like(builder.lower(root.get("email")), likeKeyword),
                    builder.like(builder.lower(root.get("phoneNumber")), likeKeyword)
            ));
        }

        if (status != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }

        if (roleCode != null) {
            specification = specification.and((root, query, builder) -> {
                LocalDateTime now = LocalDateTime.now();
                var subquery = query.subquery(UUID.class);
                var userRoleRoot = subquery.from(SecUserRole.class);
                subquery.select(userRoleRoot.get("user").get("userId"));
                subquery.where(
                        builder.equal(userRoleRoot.get("user").get("userId"), root.get("userId")),
                        builder.equal(userRoleRoot.get("role").get("roleCode"), roleCode),
                        builder.equal(userRoleRoot.get("status"), RecordStatus.ACTIVE),
                        builder.isTrue(userRoleRoot.get("primaryRole")),
                        builder.lessThanOrEqualTo(userRoleRoot.get("effectiveFrom"), now),
                        builder.or(
                                builder.isNull(userRoleRoot.get("effectiveTo")),
                                builder.greaterThanOrEqualTo(userRoleRoot.get("effectiveTo"), now)
                        )
                );
                return builder.exists(subquery);
            });
        }

        Page<SecUserAccount> result = userAccountRepository.findAll(specification, PageRequest.of(page, size));

        List<UserListItemResponse> items = result.getContent().stream()
                .map(this::toUserListItemResponse)
                .toList();

        return new PageResponse<>(
                items,
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(UUID userId) {
        return toUserDetailResponse(getUser(userId));
    }

    @Transactional
    public UserDetailResponse createUser(CreateUserRequest request) {
        validateCreateRequest(request);

        if (userAccountRepository.existsByUsernameIgnoreCaseAndDeletedFalse(request.username().trim())) {
            throw new BusinessException("USER_USERNAME_DUPLICATE", "username đã tồn tại.", HttpStatus.CONFLICT);
        }

        if (request.email() != null && !request.email().isBlank()
                && userAccountRepository.existsByEmailIgnoreCaseAndDeletedFalse(request.email().trim())) {
            throw new BusinessException("USER_EMAIL_DUPLICATE", "email đã tồn tại.", HttpStatus.CONFLICT);
        }

        if (request.employeeId() != null && userAccountRepository.existsByEmployeeIdAndDeletedFalse(request.employeeId())) {
            throw new BusinessException("USER_EMPLOYEE_ID_DUPLICATE", "employeeId đã tồn tại.", HttpStatus.CONFLICT);
        }
        if (request.employeeId() != null && hrEmployeeRepository.findByEmployeeIdAndDeletedFalse(request.employeeId()).isEmpty()) {
            throw new BusinessException("USER_EMPLOYEE_ID_NOT_FOUND", "employeeId không tồn tại trong hồ sơ nhân sự.", HttpStatus.BAD_REQUEST);
        }

        SecRole role = getActiveRole(request.roleCode());

        String effectiveInitialPassword = request.initialPassword();
        if (request.sendSetupEmail() && (effectiveInitialPassword == null || effectiveInitialPassword.isBlank())) {
            effectiveInitialPassword = "Temp@" + UUID.randomUUID().toString().substring(0, 8);
        }

        SecUserAccount user = new SecUserAccount();
        user.setEmployeeId(request.employeeId());
        user.setUsername(request.username().trim());
        user.setEmail(blankToNull(request.email()));
        user.setPhoneNumber(blankToNull(request.phoneNumber()));
        user.setPasswordHash(passwordEncoder.encode(effectiveInitialPassword));
        user.setMustChangePassword(true);
        user.setMfaEnabled(false);
        user.setStatus(request.status() == null ? appProperties.getAuth().getDefaultNewUserStatus() : request.status());
        user.setFailedLoginCount(0);
        userAccountRepository.save(user);

        SecUserRole userRole = new SecUserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setPrimaryRole(true);
        userRole.setStatus(RecordStatus.ACTIVE);
        userRole.setEffectiveFrom(LocalDateTime.now());
        userRoleRepository.save(userRole);

        if (request.sendSetupEmail() && user.getEmail() != null) {
            String rawToken = PasswordResetHelper.issueSetupToken(user, passwordResetTokenRepository, appProperties);
            String resetLink = appProperties.getMail().getResetUrlBase() + "?token=" + rawToken;
            mailService.sendPasswordResetMail(user.getEmail(), user.getUsername(), resetLink);
        }

        UserDetailResponse response = toUserDetailResponse(user);
        auditLogService.logSuccess("CREATE", "USER", "sec_user_account", user.getUserId().toString(), null, response, "Tạo mới user.");
        return response;
    }

    @Transactional
    public UserDetailResponse updateUser(UUID userId, UpdateUserRequest request) {
        SecUserAccount user = getUser(userId);
        UserDetailResponse oldSnapshot = toUserDetailResponse(user);

        if (userAccountRepository.existsByUsernameIgnoreCaseAndDeletedFalseAndUserIdNot(request.username().trim(), userId)) {
            throw new BusinessException("USER_USERNAME_DUPLICATE", "username đã tồn tại.", HttpStatus.CONFLICT);
        }

        if (request.email() != null && !request.email().isBlank()
                && userAccountRepository.existsByEmailIgnoreCaseAndDeletedFalseAndUserIdNot(request.email().trim(), userId)) {
            throw new BusinessException("USER_EMAIL_DUPLICATE", "email đã tồn tại.", HttpStatus.CONFLICT);
        }

        if (request.employeeId() != null && userAccountRepository.existsByEmployeeIdAndDeletedFalseAndUserIdNot(request.employeeId(), userId)) {
            throw new BusinessException("USER_EMPLOYEE_ID_DUPLICATE", "employeeId đã tồn tại.", HttpStatus.CONFLICT);
        }
        if (request.employeeId() != null && hrEmployeeRepository.findByEmployeeIdAndDeletedFalse(request.employeeId()).isEmpty()) {
            throw new BusinessException("USER_EMPLOYEE_ID_NOT_FOUND", "employeeId không tồn tại trong hồ sơ nhân sự.", HttpStatus.BAD_REQUEST);
        }

        if (request.status() == UserStatus.LOCKED) {
            throw new BusinessException("USER_STATUS_INVALID", "Dùng API lock/unlock để khóa user.", HttpStatus.BAD_REQUEST);
        }

        user.setEmployeeId(request.employeeId());
        user.setUsername(request.username().trim());
        user.setEmail(blankToNull(request.email()));
        user.setPhoneNumber(blankToNull(request.phoneNumber()));
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        user.setMustChangePassword(request.mustChangePassword());
        userAccountRepository.save(user);

        UserDetailResponse newSnapshot = toUserDetailResponse(user);
        auditLogService.logSuccess("UPDATE", "USER", "sec_user_account", userId.toString(), oldSnapshot, newSnapshot, "Cập nhật user.");
        return newSnapshot;
    }

    @Transactional
    public UserDetailResponse lockOrUnlockUser(UUID userId, LockUnlockUserRequest request) {
        SecUserAccount user = getUser(userId);
        if (SecurityUserContext.getCurrentUserId().filter(userId::equals).isPresent() && request.locked()) {
            throw new BusinessException("USER_LOCK_SELF_FORBIDDEN", "Không được tự khóa chính tài khoản đang đăng nhập.", HttpStatus.CONFLICT);
        }

        UserDetailResponse oldSnapshot = toUserDetailResponse(user);

        if (request.locked()) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedUntil(request.lockedUntil());
            revokeAllActiveSessions(user.getUserId(), "LOCKED_BY_ADMIN");
        } else {
            user.setStatus(UserStatus.ACTIVE);
            user.setLockedUntil(null);
            user.setFailedLoginCount(0);
        }

        userAccountRepository.save(user);

        UserDetailResponse newSnapshot = toUserDetailResponse(user);
        String action = request.locked() ? "LOCK_USER" : "UNLOCK_USER";
        auditLogService.logSuccess(action, "USER", "sec_user_account", userId.toString(), oldSnapshot, newSnapshot, request.reason());
        return newSnapshot;
    }

    @Transactional
    public UserDetailResponse assignPrimaryRole(UUID userId, AssignPrimaryRoleRequest request) {
        SecUserAccount user = getUser(userId);
        SecRole role = getActiveRole(request.roleCode());

        UserDetailResponse oldSnapshot = toUserDetailResponse(user);
        LocalDateTime now = LocalDateTime.now();

        userRoleRepository.findActivePrimaryRole(userId, now).ifPresent(current -> {
            current.setPrimaryRole(false);
            current.setStatus(RecordStatus.INACTIVE);
            current.setEffectiveTo(now);
            userRoleRepository.save(current);
        });

        SecUserRole newRole = new SecUserRole();
        newRole.setUser(user);
        newRole.setRole(role);
        newRole.setPrimaryRole(true);
        newRole.setStatus(RecordStatus.ACTIVE);
        newRole.setEffectiveFrom(now);
        userRoleRepository.save(newRole);

        revokeAllActiveSessions(userId, "ROLE_CHANGED");

        UserDetailResponse newSnapshot = toUserDetailResponse(user);
        auditLogService.logSuccess("ASSIGN_ROLE", "USER", "sec_user_account", userId.toString(), oldSnapshot, newSnapshot, request.reason());
        return newSnapshot;
    }

    private void revokeAllActiveSessions(UUID userId, String reason) {
        List<SecAuthSession> sessions = authSessionRepository.findAllByUserUserIdAndStatus(userId, SessionStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        sessions.forEach(session -> {
            session.setStatus(SessionStatus.REVOKED);
            session.setRevokedAt(now);
            session.setRevokeReason(reason);
        });
        authSessionRepository.saveAll(sessions);
    }

    private void validateCreateRequest(CreateUserRequest request) {
        if (request.sendSetupEmail() && (request.email() == null || request.email().isBlank())) {
            throw new BusinessException("USER_EMAIL_REQUIRED", "Phải có email nếu chọn gửi link thiết lập mật khẩu.", HttpStatus.BAD_REQUEST);
        }
        if (!request.sendSetupEmail() && (request.initialPassword() == null || request.initialPassword().isBlank())) {
            throw new BusinessException("USER_INITIAL_PASSWORD_REQUIRED", "Phải cung cấp initialPassword nếu không gửi setup email.", HttpStatus.BAD_REQUEST);
        }
        if (request.initialPassword() != null && !request.initialPassword().isBlank()) {
            com.company.hrm.common.util.PasswordPolicyValidator.validate(request.initialPassword(), request.initialPassword());
        }
    }

    private UserListItemResponse toUserListItemResponse(SecUserAccount user) {
        SecUserRole activeRole = userRoleRepository.findActivePrimaryRole(user.getUserId(), LocalDateTime.now()).orElse(null);
        String employeeCode = user.getEmployeeId() == null
                ? null
                : hrEmployeeRepository.findByEmployeeIdAndDeletedFalse(user.getEmployeeId())
                        .map(employee -> employee.getEmployeeCode())
                        .orElse(null);
        return new UserListItemResponse(
                user.getUserId(),
                user.getEmployeeId(),
                employeeCode,
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus().name(),
                user.isMustChangePassword(),
                activeRole == null ? null : activeRole.getRole().getRoleCode().name(),
                activeRole == null ? null : activeRole.getRole().getRoleName(),
                user.getLastLoginAt(),
                user.getLastLoginIp()
        );
    }

    private UserDetailResponse toUserDetailResponse(SecUserAccount user) {
        String employeeCode = user.getEmployeeId() == null
                ? null
                : hrEmployeeRepository.findByEmployeeIdAndDeletedFalse(user.getEmployeeId())
                        .map(employee -> employee.getEmployeeCode())
                        .orElse(null);
        List<UserRoleHistoryResponse> roleHistory = userRoleRepository.findAllByUserUserIdOrderByEffectiveFromDesc(user.getUserId())
                .stream()
                .map(role -> new UserRoleHistoryResponse(
                        role.getUserRoleId(),
                        role.getRole().getRoleCode().name(),
                        role.getRole().getRoleName(),
                        role.isPrimaryRole(),
                        role.getStatus().name(),
                        role.getEffectiveFrom(),
                        role.getEffectiveTo()
                ))
                .toList();

        return new UserDetailResponse(
                user.getUserId(),
                user.getEmployeeId(),
                employeeCode,
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus().name(),
                user.isMustChangePassword(),
                user.isMfaEnabled(),
                user.getPasswordChangedAt(),
                user.getLastLoginAt(),
                user.getLastLoginIp(),
                user.getFailedLoginCount(),
                user.getLockedUntil(),
                roleHistory
        );
    }

    private SecRole getActiveRole(RoleCode roleCode) {
        SecRole role = roleRepository.findByRoleCodeAndDeletedFalse(roleCode)
                .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Không tìm thấy role."));
        if (role.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("ROLE_INACTIVE", "Role đang không hoạt động.", HttpStatus.CONFLICT);
        }
        return role;
    }

    private SecUserAccount getUser(UUID userId) {
        return userAccountRepository.findById(userId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Không tìm thấy user."));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    static final class PasswordResetHelper {
        private PasswordResetHelper() {
        }

        static String issueSetupToken(SecUserAccount user, SecPasswordResetTokenRepository repository, AppProperties appProperties) {
            List<SecPasswordResetToken> activeTokens = repository.findAllByUserUserIdAndStatus(user.getUserId(), com.company.hrm.common.constant.ResetTokenStatus.PENDING);
            activeTokens.forEach(token -> token.setStatus(com.company.hrm.common.constant.ResetTokenStatus.REVOKED));
            repository.saveAll(activeTokens);

            String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
            SecPasswordResetToken token = new SecPasswordResetToken();
            token.setUser(user);
            token.setTokenHash(com.company.hrm.common.util.HashUtils.sha256(rawToken));
            token.setChannel(com.company.hrm.common.constant.ResetChannel.EMAIL);
            token.setIssuedAt(LocalDateTime.now());
            token.setExpiresAt(LocalDateTime.now().plusMinutes(appProperties.getAuth().getPasswordResetMinutes()));
            token.setStatus(com.company.hrm.common.constant.ResetTokenStatus.PENDING);
            repository.save(token);
            return rawToken;
        }
    }
}
