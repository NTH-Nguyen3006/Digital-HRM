package com.company.hrm.security;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.UserStatus;
import com.company.hrm.module.role.repository.SecRolePermissionRepository;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.entity.SecUserRole;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.module.user.repository.SecUserRoleRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final SecUserAccountRepository userAccountRepository;
    private final SecUserRoleRepository userRoleRepository;
    private final SecRolePermissionRepository rolePermissionRepository;

    public DatabaseUserDetailsService(
            SecUserAccountRepository userAccountRepository,
            SecUserRoleRepository userRoleRepository,
            SecRolePermissionRepository rolePermissionRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SecUserAccount user = userAccountRepository.findByUsernameIgnoreCaseAndDeletedFalse(username)
                .or(() -> userAccountRepository.findByEmailIgnoreCaseAndDeletedFalse(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        userRoleRepository.findActivePrimaryRole(user.getUserId(), LocalDateTime.now())
                .filter(userRole -> userRole.getStatus() == RecordStatus.ACTIVE)
                .ifPresent(userRole -> appendAuthorities(authorities, userRole));

        boolean locked = user.getStatus() == UserStatus.LOCKED;
        boolean disabled = user.getStatus() == UserStatus.DISABLED || user.getStatus() == UserStatus.PENDING_ACTIVATION || user.isDeleted();

        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountLocked(locked)
                .disabled(disabled)
                .build();
    }

    private void appendAuthorities(List<SimpleGrantedAuthority> authorities, SecUserRole userRole) {
        authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getRoleCode().name()));
        rolePermissionRepository.findAllowedPermissionCodes(userRole.getRole().getRoleId())
                .forEach(permissionCode -> authorities.add(new SimpleGrantedAuthority(permissionCode)));
    }
}
