package com.company.hrm.module.orgunit.controller;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.orgunit.dto.*;
import com.company.hrm.module.orgunit.service.OrgUnitService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/org-units")
public class AdminOrgUnitController {

    private final OrgUnitService orgUnitService;

    public AdminOrgUnitController(OrgUnitService orgUnitService) {
        this.orgUnitService = orgUnitService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('orgunit.view')")
    public ApiResponse<PageResponse<OrgUnitListItemResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RecordStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("ORG_UNIT_LIST_SUCCESS", "Lấy danh sách đơn vị tổ chức thành công.", orgUnitService.list(keyword, status, page, size), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('orgunit.view')")
    public ApiResponse<List<OrgUnitTreeNodeResponse>> getTree() {
        return ApiResponse.success("ORG_UNIT_TREE_SUCCESS", "Lấy cây cơ cấu tổ chức thành công.", orgUnitService.getTree(), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/{orgUnitId}")
    @PreAuthorize("hasAuthority('orgunit.view')")
    public ApiResponse<OrgUnitDetailResponse> getDetail(@PathVariable Long orgUnitId) {
        return ApiResponse.success("ORG_UNIT_DETAIL_SUCCESS", "Lấy chi tiết đơn vị tổ chức thành công.", orgUnitService.getDetail(orgUnitId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('orgunit.create')")
    public ApiResponse<OrgUnitDetailResponse> create(@Valid @RequestBody CreateOrgUnitRequest request) {
        return ApiResponse.success("ORG_UNIT_CREATE_SUCCESS", "Tạo mới đơn vị tổ chức thành công.", orgUnitService.create(request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{orgUnitId}")
    @PreAuthorize("hasAuthority('orgunit.update')")
    public ApiResponse<OrgUnitDetailResponse> update(@PathVariable Long orgUnitId, @Valid @RequestBody UpdateOrgUnitRequest request) {
        return ApiResponse.success("ORG_UNIT_UPDATE_SUCCESS", "Cập nhật đơn vị tổ chức thành công.", orgUnitService.update(orgUnitId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{orgUnitId}/status")
    @PreAuthorize("hasAuthority('orgunit.change_status')")
    public ApiResponse<OrgUnitDetailResponse> changeStatus(@PathVariable Long orgUnitId, @Valid @RequestBody UpdateOrgUnitStatusRequest request) {
        return ApiResponse.success("ORG_UNIT_STATUS_SUCCESS", "Cập nhật trạng thái đơn vị tổ chức thành công.", orgUnitService.changeStatus(orgUnitId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{orgUnitId}/manager")
    @PreAuthorize("hasAuthority('orgunit.assign_manager')")
    public ApiResponse<OrgUnitDetailResponse> assignManager(@PathVariable Long orgUnitId, @Valid @RequestBody UpdateOrgUnitManagerRequest request) {
        return ApiResponse.success("ORG_UNIT_ASSIGN_MANAGER_SUCCESS", "Gán quản lý đơn vị thành công.", orgUnitService.assignManager(orgUnitId, request), null, RequestTraceContext.getTraceId());
    }
}
