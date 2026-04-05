package com.company.hrm.module.onboarding.controller;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.onboarding.dto.ConfirmOrientationRequest;
import com.company.hrm.module.onboarding.dto.OnboardingDetailResponse;
import com.company.hrm.module.onboarding.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manager/onboarding")
public class ManagerOnboardingController {

    private final OnboardingService onboardingService;

    public ManagerOnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PatchMapping("/{onboardingId}/orientation-confirm")
    @PreAuthorize("hasAuthority('onboarding.orientation.confirm')")
    public ApiResponse<OnboardingDetailResponse> confirmOrientation(@PathVariable Long onboardingId, @Valid @RequestBody ConfirmOrientationRequest request) {
        return ApiResponse.success("ONBOARDING_ORIENTATION_CONFIRM_SUCCESS", "Xác nhận hoàn tất hướng dẫn nhập môn thành công.", onboardingService.confirmOrientation(onboardingId, request), null, RequestTraceContext.getTraceId());
    }
}
