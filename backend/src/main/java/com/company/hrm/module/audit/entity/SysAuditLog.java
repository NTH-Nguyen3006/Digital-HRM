package com.company.hrm.module.audit.entity;

import com.company.hrm.common.constant.AuditResultCode;
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
@Table(name = "sys_audit_log")
public class SysAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_log_id", nullable = false, updatable = false)
    private UUID auditLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private SecUserAccount actorUser;

    @Column(name = "actor_username", length = 50)
    private String actorUsername;

    @Column(name = "action_code", nullable = false, length = 50)
    private String actionCode;

    @Column(name = "module_code", nullable = false, length = 50)
    private String moduleCode;

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id", length = 50)
    private String entityId;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "old_data_json", columnDefinition = "NVARCHAR(MAX)")
    private String oldDataJson;

    @Column(name = "new_data_json", columnDefinition = "NVARCHAR(MAX)")
    private String newDataJson;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "action_at", nullable = false)
    private LocalDateTime actionAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_code", nullable = false, length = 20)
    private AuditResultCode resultCode;

    @Column(name = "message", length = 1000)
    private String message;
}
