package com.company.hrm.security;

import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUserContext {

    private SecurityUserContext() {
    }

    public static Optional<UUID> getCurrentUserId() {
        return currentPrincipal().map(SecurityUserPrincipal::getUserId);
    }

    public static Optional<UUID> getCurrentSessionId() {
        return currentPrincipal().map(SecurityUserPrincipal::getSessionId);
    }

    public static Optional<String> getCurrentUsername() {
        return currentPrincipal().map(SecurityUserPrincipal::getUsername);
    }

    public static Optional<SecurityUserPrincipal> currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }
}
