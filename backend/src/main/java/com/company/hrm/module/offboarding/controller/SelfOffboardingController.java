package com.company.hrm.module.offboarding.controller;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.offboarding.dto.CreateOffboardingRequest;
import com.company.hrm.module.offboarding.dto.OffboardingDetailResponse;
import com.company.hrm.module.offboarding.dto.OffboardingListItemResponse;
import com.company.hrm.module.offboarding.service.OffboardingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/offboarding")
public class SelfOffboardingController {

    private final OffboardingService offboardingService;

    public SelfOffboardingController(OffboardingService offboardingService) {
        this.offboardingService = offboardingService;
    }

    @GetMapping("/requests")
    @PreAuthorize("hasAuthority('offboarding.request.submit')")
    public ApiResponse<List<OffboardingListItemResponse>> listMyRequests() {
        return ApiResponse.success("OFFBOARDING_SELF_LIST_SUCCESS", "Lấy danh sách yêu cầu nghỉ việc cá nhân thành công.",
                offboardingService.listMyRequests(), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/requests")
    @PreAuthorize("hasAuthority('offboarding.request.submit')")
    public ApiResponse<OffboardingDetailResponse> create(@Valid @RequestBody CreateOffboardingRequest request) {
        return ApiResponse.success("OFFBOARDING_SELF_CREATE_SUCCESS", "Tạo yêu cầu nghỉ việc thành công.",
                offboardingService.createMyRequest(request), null, RequestTraceContext.getTraceId());
    }
}
