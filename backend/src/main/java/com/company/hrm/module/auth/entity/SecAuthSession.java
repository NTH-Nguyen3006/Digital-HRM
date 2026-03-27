package com.company.hrm.module.auth.entity;

import com.company.hrm.common.constant.SessionStatus;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sec_auth_session")
public class SecAuthSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "auth_session_id", nullable = false, updatable = false)
    private UUID authSessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private SecUserAccount user;

    @Column(name = "refresh_token_hash", nullable = false, length = 255)
    private String refreshTokenHash;

    @Column(name = "device_name", length = 150)
    private String deviceName;

    @Column(name = "device_os", length = 100)
    private String deviceOs;

    @Column(name = "browser_name", length = 100)
    private String browserName;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoke_reason", length = 255)
    private String revokeReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;
}
