package com.company.hrm.config;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.constant.UserStatus;
import com.company.hrm.config.AppProperties;
import com.company.hrm.module.role.entity.SecRole;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.entity.SecUserRole;
import com.company.hrm.module.role.repository.SecRoleRepository;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.module.user.repository.SecUserRoleRepository;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class BootstrapDataInitializer implements ApplicationRunner {

    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;
    private final SecUserAccountRepository userAccountRepository;
    private final SecRoleRepository roleRepository;
    private final SecUserRoleRepository userRoleRepository;

    public BootstrapDataInitializer(
            AppProperties appProperties,
            PasswordEncoder passwordEncoder,
            SecUserAccountRepository userAccountRepository,
            SecRoleRepository roleRepository,
            SecUserRoleRepository userRoleRepository
    ) {
        this.appProperties = appProperties;
        this.passwordEncoder = passwordEncoder;
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!appProperties.getBootstrap().isSeedDefaultAdmin()) {
            return;
        }

        if (userAccountRepository.findByUsernameIgnoreCaseAndDeletedFalse(appProperties.getBootstrap().getAdminUsername()).isPresent()) {
            return;
        }

        SecRole adminRole = roleRepository.findByRoleCodeAndDeletedFalse(RoleCode.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Default ADMIN role is missing."));

        SecUserAccount adminUser = new SecUserAccount();
        adminUser.setUsername(appProperties.getBootstrap().getAdminUsername());
        adminUser.setEmail(appProperties.getBootstrap().getAdminEmail());
        adminUser.setPhoneNumber(appProperties.getBootstrap().getAdminPhoneNumber());
        adminUser.setPasswordHash(passwordEncoder.encode(appProperties.getBootstrap().getAdminPassword()));
        adminUser.setMustChangePassword(true);
        adminUser.setMfaEnabled(false);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setFailedLoginCount(0);
        adminUser.setPasswordChangedAt(LocalDateTime.now());
        userAccountRepository.save(adminUser);

        SecUserRole userRole = new SecUserRole();
        userRole.setUser(adminUser);
        userRole.setRole(adminRole);
        userRole.setPrimaryRole(true);
        userRole.setStatus(RecordStatus.ACTIVE);
        userRole.setEffectiveFrom(LocalDateTime.now());
        userRoleRepository.save(userRole);

        log.info("Bootstrapped default admin user username={}", adminUser.getUsername());
    }
}
