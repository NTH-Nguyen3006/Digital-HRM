package com.company.hrm.module.offboarding.controller;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.offboarding.dto.*;
import com.company.hrm.module.offboarding.service.OffboardingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manager/offboarding")
public class ManagerOffboardingController {

    private final OffboardingService offboardingService;

    public ManagerOffboardingController(OffboardingService offboardingService) {
        this.offboardingService = offboardingService;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('offboarding.review')")
    public ApiResponse<List<OffboardingListItemResponse>> listPending() {
        return ApiResponse.success("OFFBOARDING_MANAGER_PENDING_SUCCESS", "Lấy danh sách offboarding chờ duyệt thành công.",
                offboardingService.listManagerPendingCases(), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{offboardingCaseId}/review")
    @PreAuthorize("hasAuthority('offboarding.review')")
    public ApiResponse<OffboardingDetailResponse> review(@PathVariable Long offboardingCaseId,
                                                         @Valid @RequestBody ReviewOffboardingRequest request) {
        return ApiResponse.success("OFFBOARDING_REVIEW_SUCCESS", "Xử lý duyệt/từ chối offboarding thành công.",
                offboardingService.reviewByManager(offboardingCaseId, request), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/{offboardingCaseId}/checklist-items")
    @PreAuthorize("hasAuthority('offboarding.checklist.manage')")
    public ApiResponse<OffboardingChecklistItemResponse> createChecklist(@PathVariable Long offboardingCaseId,
                                                                         @Valid @RequestBody UpsertOffboardingChecklistItemRequest request) {
        return ApiResponse.success("OFFBOARDING_CHECKLIST_CREATE_SUCCESS", "Tạo checklist bàn giao thành công.",
                offboardingService.createChecklistItem(offboardingCaseId, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/checklist-items/{checklistItemId}")
    @PreAuthorize("hasAuthority('offboarding.checklist.manage')")
    public ApiResponse<OffboardingChecklistItemResponse> updateChecklist(@PathVariable Long checklistItemId,
                                                                         @Valid @RequestBody UpsertOffboardingChecklistItemRequest request) {
        return ApiResponse.success("OFFBOARDING_CHECKLIST_UPDATE_SUCCESS", "Cập nhật checklist bàn giao thành công.",
                offboardingService.updateChecklistItem(checklistItemId, request), null, RequestTraceContext.getTraceId());
    }
}
