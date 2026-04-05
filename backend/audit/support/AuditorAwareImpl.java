package com.company.hrm.module.audit.support;

import com.company.hrm.security.SecurityUserContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;

public class AuditorAwareImpl implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return SecurityUserContext.getCurrentUserId();
    }
}
