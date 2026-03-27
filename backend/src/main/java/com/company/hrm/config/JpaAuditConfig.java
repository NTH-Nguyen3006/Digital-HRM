package com.company.hrm.config;

import com.company.hrm.module.audit.support.AuditorAwareImpl;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditConfig {

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return new AuditorAwareImpl();
    }
}
