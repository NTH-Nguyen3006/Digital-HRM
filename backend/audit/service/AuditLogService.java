package com.company.hrm.module.audit.service;

import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.common.constant.AuditResultCode;
import com.company.hrm.common.util.RequestContextUtils;
import com.company.hrm.module.audit.entity.SysAuditLog;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.module.audit.repository.SysAuditLogRepository;
import com.company.hrm.security.SecurityUserContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final SysAuditLogRepository auditLogRepository;
    private final SecUserAccountRepository userAccountRepository;
    private final ObjectMapper objectMapper;

    public AuditLogService(
            SysAuditLogRepository auditLogRepository,
            SecUserAccountRepository userAccountRepository,
            ObjectMapper objectMapper
    ) {
        this.auditLogRepository = auditLogRepository;
        this.userAccountRepository = userAccountRepository;
        this.objectMapper = objectMapper;
    }

    public void logSuccess(String actionCode, String moduleCode, String entityName, String entityId, Object oldData, Object newData, String message) {
        persist(actionCode, moduleCode, entityName, entityId, oldData, newData, AuditResultCode.SUCCESS, message);
    }

    public void logFailure(String actionCode, String moduleCode, String entityName, String entityId, Object oldData, Object newData, String message) {
        persist(actionCode, moduleCode, entityName, entityId, oldData, newData, AuditResultCode.FAILED, message);
    }

    public void logSystemSuccess(String actorUsername, String actionCode, String moduleCode, String entityName, String entityId, Object oldData, Object newData, String message) {
        persistSystem(actorUsername, actionCode, moduleCode, entityName, entityId, oldData, newData, AuditResultCode.SUCCESS, message);
    }

    public void logSystemFailure(String actorUsername, String actionCode, String moduleCode, String entityName, String entityId, Object oldData, Object newData, String message) {
        persistSystem(actorUsername, actionCode, moduleCode, entityName, entityId, oldData, newData, AuditResultCode.FAILED, message);
    }

    private void persist(String actionCode, String moduleCode, String entityName, String entityId, Object oldData, Object newData, AuditResultCode resultCode, String message) {
        SysAuditLog auditLog = new SysAuditLog();
        Optional<UUID> currentUserId = SecurityUserContext.getCurrentUserId();
        currentUserId.flatMap(userAccountRepository::findById).ifPresent(auditLog::setActorUser);
        auditLog.setActorUsername(SecurityUserContext.getCurrentUsername().orElse(null));
        fillCommonFields(auditLog, actionCode, moduleCode, entityName, entityId, oldData, newData, resultCode, message);
        auditLogRepository.save(auditLog);
    }

    private void persistSystem(String actorUsername, String actionCode, String moduleCode, String entityName, String entityId, Object oldData, Object newData, AuditResultCode resultCode, String message) {
        SysAuditLog auditLog = new SysAuditLog();
        auditLog.setActorUsername(actorUsername);
        fillCommonFields(auditLog, actionCode, moduleCode, entityName, entityId, oldData, newData, resultCode, message);
        auditLogRepository.save(auditLog);
    }

    private void fillCommonFields(
            SysAuditLog auditLog,
            String actionCode,
            String moduleCode,
            String entityName,
            String entityId,
            Object oldData,
            Object newData,
            AuditResultCode resultCode,
            String message
    ) {
        auditLog.setActionCode(actionCode);
        auditLog.setModuleCode(moduleCode);
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setRequestId(RequestTraceContext.getTraceId());
        auditLog.setOldDataJson(toJson(oldData));
        auditLog.setNewDataJson(toJson(newData));
        auditLog.setIpAddress(RequestContextUtils.getClientIp());
        auditLog.setUserAgent(RequestContextUtils.getUserAgent());
        auditLog.setActionAt(LocalDateTime.now());
        auditLog.setResultCode(resultCode);
        auditLog.setMessage(message);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{\"serializationError\":true}";
        }
    }
}
