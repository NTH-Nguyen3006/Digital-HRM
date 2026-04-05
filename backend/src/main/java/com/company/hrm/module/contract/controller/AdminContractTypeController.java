package com.company.hrm.module.contract.controller;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.contract.dto.*;
import com.company.hrm.module.contract.service.ContractTypeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/contract-types")
public class AdminContractTypeController {

    private final ContractTypeService contractTypeService;

    public AdminContractTypeController(ContractTypeService contractTypeService) {
        this.contractTypeService = contractTypeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('contract.type.view')")
    public ApiResponse<PageResponse<ContractTypeListItemResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RecordStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(
                "CONTRACT_TYPE_LIST_SUCCESS",
                "Lấy danh sách loại hợp đồng thành công.",
                contractTypeService.list(keyword, status, page, size),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('contract.type.view')")
    public ApiResponse<List<ContractTypeOptionResponse>> listActiveOptions() {
        return ApiResponse.success(
                "CONTRACT_TYPE_OPTION_SUCCESS",
                "Lấy danh sách option loại hợp đồng thành công.",
                contractTypeService.listActiveOptions(),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @GetMapping("/{contractTypeId}")
    @PreAuthorize("hasAuthority('contract.type.view')")
    public ApiResponse<ContractTypeDetailResponse> getDetail(@PathVariable Long contractTypeId) {
        return ApiResponse.success(
                "CONTRACT_TYPE_DETAIL_SUCCESS",
                "Lấy chi tiết loại hợp đồng thành công.",
                contractTypeService.getDetail(contractTypeId),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('contract.type.create')")
    public ApiResponse<ContractTypeDetailResponse> create(@Valid @RequestBody CreateContractTypeRequest request) {
        return ApiResponse.success(
                "CONTRACT_TYPE_CREATE_SUCCESS",
                "Tạo loại hợp đồng thành công.",
                contractTypeService.create(request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PutMapping("/{contractTypeId}")
    @PreAuthorize("hasAuthority('contract.type.update')")
    public ApiResponse<ContractTypeDetailResponse> update(@PathVariable Long contractTypeId, @Valid @RequestBody UpdateContractTypeRequest request) {
        return ApiResponse.success(
                "CONTRACT_TYPE_UPDATE_SUCCESS",
                "Cập nhật loại hợp đồng thành công.",
                contractTypeService.update(contractTypeId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PatchMapping("/{contractTypeId}/status")
    @PreAuthorize("hasAuthority('contract.type.change_status')")
    public ApiResponse<ContractTypeDetailResponse> changeStatus(@PathVariable Long contractTypeId, @Valid @RequestBody UpdateContractTypeStatusRequest request) {
        return ApiResponse.success(
                "CONTRACT_TYPE_STATUS_SUCCESS",
                "Cập nhật trạng thái loại hợp đồng thành công.",
                contractTypeService.changeStatus(contractTypeId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }
}
