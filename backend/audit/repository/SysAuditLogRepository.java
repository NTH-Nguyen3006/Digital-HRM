package com.company.hrm.module.audit.repository;

import com.company.hrm.module.audit.entity.SysAuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysAuditLogRepository extends JpaRepository<SysAuditLog, UUID>, JpaSpecificationExecutor<SysAuditLog> {
}
