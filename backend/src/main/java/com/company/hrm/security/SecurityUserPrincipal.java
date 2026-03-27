package com.company.hrm.security;

import com.company.hrm.common.constant.RoleCode;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class SecurityUserPrincipal implements UserDetails {

    private final UUID userId;
    private final UUID sessionId;
    private final String username;
    private final String email;
    private final RoleCode roleCode;
    private final List<String> permissions;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUserPrincipal(
            UUID userId,
            UUID sessionId,
            String username,
            String email,
            RoleCode roleCode,
            List<String> permissions,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.username = username;
        this.email = email;
        this.roleCode = roleCode;
        this.permissions = permissions;
        this.authorities = authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
