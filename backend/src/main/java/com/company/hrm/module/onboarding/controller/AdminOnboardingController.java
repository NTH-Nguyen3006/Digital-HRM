package com.company.hrm.module.onboarding.controller;

import com.company.hrm.common.constant.OnboardingStatus;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.contract.dto.LaborContractDetailResponse;
import com.company.hrm.module.onboarding.dto.*;
import com.company.hrm.module.onboarding.service.OnboardingService;
import com.company.hrm.module.user.dto.UserDetailResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/onboarding")
public class AdminOnboardingController {

    private final OnboardingService onboardingService;

    public AdminOnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('onboarding.view')")
    public ApiResponse<PageResponse<OnboardingListItemResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) OnboardingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("ONBOARDING_LIST_SUCCESS", "Lấy danh sách onboarding thành công.", onboardingService.list(keyword, status, page, size), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/{onboardingId}")
    @PreAuthorize("hasAuthority('onboarding.view')")
    public ApiResponse<OnboardingDetailResponse> getDetail(@PathVariable Long onboardingId) {
        return ApiResponse.success("ONBOARDING_DETAIL_SUCCESS", "Lấy chi tiết onboarding thành công.", onboardingService.getDetail(onboardingId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('onboarding.create')")
    public ApiResponse<OnboardingDetailResponse> create(@Valid @RequestBody OnboardingUpsertRequest request) {
        return ApiResponse.success("ONBOARDING_CREATE_SUCCESS", "Tạo hồ sơ onboarding thành công.", onboardingService.create(request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{onboardingId}")
    @PreAuthorize("hasAuthority('onboarding.update')")
    public ApiResponse<OnboardingDetailResponse> update(@PathVariable Long onboardingId, @Valid @RequestBody OnboardingUpsertRequest request) {
        return ApiResponse.success("ONBOARDING_UPDATE_SUCCESS", "Cập nhật onboarding thành công.", onboardingService.update(onboardingId, request), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/{onboardingId}/checklist")
    @PreAuthorize("hasAuthority('onboarding.update')")
    public ApiResponse<OnboardingChecklistResponse> createChecklist(@PathVariable Long onboardingId, @Valid @RequestBody OnboardingChecklistRequest request) {
        return ApiResponse.success("ONBOARDING_CHECKLIST_CREATE_SUCCESS", "Cập nhật checklist onboarding thành công.", onboardingService.upsertChecklist(onboardingId, null, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{onboardingId}/checklist/{checklistId}")
    @PreAuthorize("hasAuthority('onboarding.update')")
    public ApiResponse<OnboardingChecklistResponse> updateChecklist(@PathVariable Long onboardingId, @PathVariable Long checklistId, @Valid @RequestBody OnboardingChecklistRequest request) {
        return ApiResponse.success("ONBOARDING_CHECKLIST_UPDATE_SUCCESS", "Cập nhật checklist onboarding thành công.", onboardingService.upsertChecklist(onboardingId, checklistId, request), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/{onboardingId}/documents")
    @PreAuthorize("hasAuthority('onboarding.update')")
    public ApiResponse<OnboardingDocumentResponse> createDocument(@PathVariable Long onboardingId, @Valid @RequestBody OnboardingDocumentRequest request) {
        return ApiResponse.success("ONBOARDING_DOCUMENT_CREATE_SUCCESS", "Cập nhật hồ sơ đầu vào thành công.", onboardingService.upsertDocument(onboardingId, null, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{onboardingId}/documents/{documentId}")
    @PreAuthorize("hasAuthority('onboarding.update')")
    public ApiResponse<OnboardingDocumentResponse> updateDocument(@PathVariable Long onboardingId, @PathVariable Long documentId, @Valid @RequestBody OnboardingDocumentRequest request) {
        return ApiResponse.success("ONBOARDING_DOCUMENT_UPDATE_SUCCESS", "Cập nhật hồ sơ đầu vào thành công.", onboardingService.upsertDocument(onboardingId, documentId, request), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/{onboardingId}/assets")
    @PreAuthorize("hasAuthority('onboarding.update')")
    public ApiResponse<OnboardingAssetResponse> createAsset(@PathVariable Long onboardingId, @Valid @RequestBody OnboardingAssetRequest request) {
        return ApiResponse.success("ONBOARDING_ASSET_CREATE_SUCCESS", "Cập nhật phân bổ trang thiết bị thành công.", onboardingService.upsertAsset(onboardingId, null, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{onboardingId}/assets/{assetId}")
    @PreAuthorize("hasAuthority('onboarding.update')")
    public ApiResponse<OnboardingAssetResponse> updateAsset(@PathVariable Long onboardingId, @PathVariable Long assetId, @Valid @RequestBody OnboardingAssetRequest request) {
        return ApiResponse.success("ONBOARDING_ASSET_UPDATE_SUCCESS", "Cập nhật phân bổ trang thiết bị thành công.", onboardingService.upsertAsset(onboardingId, assetId, request), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/{onboardingId}/create-user")
    @PreAuthorize("hasAuthority('onboarding.create_user')")
    public ApiResponse<UserDetailResponse> createUser(@PathVariable Long onboardingId, @Valid @RequestBody CreateOnboardingUserRequest request) {
        return ApiResponse.success("ONBOARDING_CREATE_USER_SUCCESS", "Tạo user từ onboarding thành công.", onboardingService.createUserFromOnboarding(onboardingId, request), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/{onboardingId}/initial-contract")
    @PreAuthorize("hasAuthority('onboarding.link_contract')")
    public ApiResponse<LaborContractDetailResponse> createInitialContract(@PathVariable Long onboardingId, @Valid @RequestBody CreateInitialContractFromOnboardingRequest request) {
        return ApiResponse.success("ONBOARDING_CREATE_CONTRACT_SUCCESS", "Tạo hợp đồng đầu tiên từ onboarding thành công.", onboardingService.createInitialContract(onboardingId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{onboardingId}/complete")
    @PreAuthorize("hasAuthority('onboarding.complete')")
    public ApiResponse<OnboardingDetailResponse> complete(@PathVariable Long onboardingId, @Valid @RequestBody CompleteOnboardingRequest request) {
        return ApiResponse.success("ONBOARDING_COMPLETE_SUCCESS", "Chốt hoàn tất onboarding thành công.", onboardingService.completeOnboarding(onboardingId, request), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/{onboardingId}/notify")
    @PreAuthorize("hasAuthority('onboarding.notify')")
    public ApiResponse<List<OnboardingNotificationResponse>> notify(@PathVariable Long onboardingId, @Valid @RequestBody SendOnboardingNotificationRequest request) {
        return ApiResponse.success("ONBOARDING_NOTIFY_SUCCESS", "Gửi thông báo onboarding thành công.", onboardingService.sendOnboardingNotifications(onboardingId, request), null, RequestTraceContext.getTraceId());
    }
}
