package com.company.hrm.module.offboarding.controller;

import com.company.hrm.common.constant.OffboardingStatus;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.offboarding.dto.*;
import com.company.hrm.module.offboarding.service.OffboardingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/offboarding")
public class AdminOffboardingController {

    private final OffboardingService offboardingService;

    public AdminOffboardingController(OffboardingService offboardingService) {
        this.offboardingService = offboardingService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('offboarding.case.view')")
    public ApiResponse<PageResponse<OffboardingListItemResponse>> listCases(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) OffboardingStatus status,
            @RequestParam(required = false) Long orgUnitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("OFFBOARDING_CASE_LIST_SUCCESS", "Lấy danh sách offboarding thành công.",
                offboardingService.listCases(keyword, status, orgUnitId, page, size), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/{offboardingCaseId}")
    @PreAuthorize("hasAuthority('offboarding.case.view')")
    public ApiResponse<OffboardingDetailResponse> getDetail(@PathVariable Long offboardingCaseId) {
        return ApiResponse.success("OFFBOARDING_CASE_DETAIL_SUCCESS", "Lấy chi tiết offboarding thành công.",
                offboardingService.getDetail(offboardingCaseId), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{offboardingCaseId}/finalize")
    @PreAuthorize("hasAuthority('offboarding.finalize')")
    public ApiResponse<OffboardingDetailResponse> finalizeByHr(@PathVariable Long offboardingCaseId,
                                                               @Valid @RequestBody FinalizeOffboardingRequest request) {
        return ApiResponse.success("OFFBOARDING_FINALIZE_SUCCESS", "HR chốt offboarding thành công.",
                offboardingService.finalizeByHr(offboardingCaseId, request), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/{offboardingCaseId}/asset-returns")
    @PreAuthorize("hasAuthority('offboarding.asset.manage')")
    public ApiResponse<OffboardingAssetReturnResponse> createAssetReturn(@PathVariable Long offboardingCaseId,
                                                                         @Valid @RequestBody UpsertOffboardingAssetReturnRequest request) {
        return ApiResponse.success("OFFBOARDING_ASSET_CREATE_SUCCESS", "Tạo dòng thu hồi tài sản thành công.",
                offboardingService.createAssetReturn(offboardingCaseId, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/asset-returns/{assetReturnId}")
    @PreAuthorize("hasAuthority('offboarding.asset.manage')")
    public ApiResponse<OffboardingAssetReturnResponse> updateAssetReturn(@PathVariable Long assetReturnId,
                                                                         @Valid @RequestBody UpsertOffboardingAssetReturnRequest request) {
        return ApiResponse.success("OFFBOARDING_ASSET_UPDATE_SUCCESS", "Cập nhật dòng thu hồi tài sản thành công.",
                offboardingService.updateAssetReturn(assetReturnId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{offboardingCaseId}/settlement")
    @PreAuthorize("hasAuthority('offboarding.settlement.prepare')")
    public ApiResponse<OffboardingDetailResponse> prepareSettlement(@PathVariable Long offboardingCaseId,
                                                                    @Valid @RequestBody PrepareOffboardingSettlementRequest request) {
        return ApiResponse.success("OFFBOARDING_SETTLEMENT_SUCCESS", "Chuẩn bị settlement offboarding thành công.",
                offboardingService.prepareSettlement(offboardingCaseId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{offboardingCaseId}/close")
    @PreAuthorize("hasAuthority('offboarding.close')")
    public ApiResponse<OffboardingDetailResponse> close(@PathVariable Long offboardingCaseId,
                                                        @Valid @RequestBody CloseOffboardingRequest request) {
        return ApiResponse.success("OFFBOARDING_CLOSE_SUCCESS", "Đóng hồ sơ offboarding thành công.",
                offboardingService.closeOffboarding(offboardingCaseId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{offboardingCaseId}/revoke-access")
    @PreAuthorize("hasAuthority('offboarding.access.revoke')")
    public ApiResponse<OffboardingDetailResponse> revokeAccess(@PathVariable Long offboardingCaseId,
                                                               @Valid @RequestBody RevokeOffboardingAccessRequest request) {
        return ApiResponse.success("OFFBOARDING_REVOKE_ACCESS_SUCCESS", "Thu hồi quyền truy cập thành công.",
                offboardingService.revokeAccess(offboardingCaseId, request), null, RequestTraceContext.getTraceId());
    }
}
