package com.company.hrm.module.audit.controller;

import com.company.hrm.common.constant.AuditResultCode;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.dto.AuditLogListItemResponse;
import com.company.hrm.module.audit.service.AuditLogQueryService;
import com.company.hrm.module.audit.support.RequestTraceContext;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
public class AuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    public AuditLogController(AuditLogQueryService auditLogQueryService) {
        this.auditLogQueryService = auditLogQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('audit.view')")
    public ApiResponse<PageResponse<AuditLogListItemResponse>> search(
            @RequestParam(required = false) String moduleCode,
            @RequestParam(required = false) String actionCode,
            @RequestParam(required = false) AuditResultCode resultCode,
            @RequestParam(required = false) String actorUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<AuditLogListItemResponse> response = auditLogQueryService.search(
                moduleCode,
                actionCode,
                resultCode,
                actorUsername,
                from,
                to,
                page,
                size
        );
        return ApiResponse.success("AUDIT_LOG_LIST_SUCCESS", "Lấy danh sách audit log thành công.", response, null, RequestTraceContext.getTraceId());
    }
}
