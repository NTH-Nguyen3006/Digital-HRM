package com.company.hrm.module.audit.service;

import com.company.hrm.common.constant.AuditResultCode;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.dto.AuditLogListItemResponse;
import com.company.hrm.module.audit.entity.SysAuditLog;
import com.company.hrm.module.audit.repository.SysAuditLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogQueryService {

    private final SysAuditLogRepository auditLogRepository;

    public AuditLogQueryService(SysAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogListItemResponse> search(
            String moduleCode,
            String actionCode,
            AuditResultCode resultCode,
            String actorUsername,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {
        Specification<SysAuditLog> specification = Specification.where(null);

        if (moduleCode != null && !moduleCode.isBlank()) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("moduleCode"), moduleCode.trim().toUpperCase()));
        }
        if (actionCode != null && !actionCode.isBlank()) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("actionCode"), actionCode.trim().toUpperCase()));
        }
        if (resultCode != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("resultCode"), resultCode));
        }
        if (actorUsername != null && !actorUsername.isBlank()) {
            String likeValue = "%" + actorUsername.trim().toLowerCase() + "%";
            specification = specification.and((root, query, builder) -> builder.like(builder.lower(root.get("actorUsername")), likeValue));
        }
        if (from != null) {
            specification = specification.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("actionAt"), from));
        }
        if (to != null) {
            specification = specification.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("actionAt"), to));
        }

        Page<SysAuditLog> result = auditLogRepository.findAll(
                specification,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "actionAt"))
        );

        List<AuditLogListItemResponse> items = result.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                items,
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    private AuditLogListItemResponse toResponse(SysAuditLog entity) {
        return new AuditLogListItemResponse(
                entity.getAuditLogId(),
                entity.getActorUser() == null ? null : entity.getActorUser().getUserId(),
                entity.getActorUsername(),
                entity.getActionCode(),
                entity.getModuleCode(),
                entity.getEntityName(),
                entity.getEntityId(),
                entity.getRequestId(),
                entity.getIpAddress(),
                entity.getActionAt(),
                entity.getResultCode().name(),
                entity.getMessage()
        );
    }
}
