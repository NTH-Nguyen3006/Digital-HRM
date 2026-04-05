package com.company.hrm.module.employee.controller;

import com.company.hrm.common.constant.EmploymentStatus;
import com.company.hrm.common.constant.ProfileChangeRequestStatus;
import com.company.hrm.common.dto.ImportResultResponse;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.employee.dto.*;
import com.company.hrm.module.employee.service.EmployeeProfileWorkflowService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AdminEmployeeProfileWorkflowController {

    private final EmployeeProfileWorkflowService workflowService;

    public AdminEmployeeProfileWorkflowController(EmployeeProfileWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/me/profile-change-requests")
    @PreAuthorize("hasAuthority('employee.profile.request.submit')")
    public ApiResponse<ProfileChangeRequestResponse> submitMyProfileChangeRequest(@Valid @RequestBody SubmitProfileChangeRequest request) {
        return ApiResponse.success("EMPLOYEE_PROFILE_REQUEST_SUBMIT_SUCCESS", "Gửi yêu cầu cập nhật hồ sơ thành công.", workflowService.submitMyProfileChangeRequest(request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/me/profile-change-requests")
    @PreAuthorize("hasAuthority('employee.profile.request.submit')")
    public ApiResponse<List<ProfileChangeRequestResponse>> listMyProfileChangeRequests() {
        return ApiResponse.success("EMPLOYEE_PROFILE_REQUEST_ME_LIST_SUCCESS", "Lấy danh sách yêu cầu cá nhân thành công.", workflowService.listMyProfileChangeRequests(), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/admin/profile-change-requests")
    @PreAuthorize("hasAuthority('employee.profile.request.review')")
    public ApiResponse<PageResponse<ProfileChangeRequestResponse>> listRequests(
            @RequestParam(required = false) ProfileChangeRequestStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("EMPLOYEE_PROFILE_REQUEST_LIST_SUCCESS", "Lấy danh sách yêu cầu cập nhật hồ sơ thành công.", workflowService.listRequests(status, employeeId, page, size), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/admin/profile-change-requests/{requestId}/review")
    @PreAuthorize("hasAuthority('employee.profile.request.review')")
    public ApiResponse<ProfileChangeRequestResponse> reviewRequest(@PathVariable Long requestId, @Valid @RequestBody ReviewProfileChangeRequest request) {
        return ApiResponse.success("EMPLOYEE_PROFILE_REQUEST_REVIEW_SUCCESS", "Xử lý yêu cầu cập nhật hồ sơ thành công.", workflowService.reviewRequest(requestId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/admin/employees/{employeeId}/profile-lock")
    @PreAuthorize("hasAuthority('employee.profile.lock_restore')")
    public ApiResponse<EmployeeProfileResponse> lockProfile(@PathVariable Long employeeId, @Valid @RequestBody LockEmployeeProfileRequest request) {
        return ApiResponse.success("EMPLOYEE_PROFILE_LOCK_SUCCESS", "Khóa hồ sơ nhân viên thành công.", workflowService.lockProfile(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/admin/employees/{employeeId}/profile-restore")
    @PreAuthorize("hasAuthority('employee.profile.lock_restore')")
    public ApiResponse<EmployeeProfileResponse> restoreProfile(@PathVariable Long employeeId, @Valid @RequestBody RestoreEmployeeProfileRequest request) {
        return ApiResponse.success("EMPLOYEE_PROFILE_RESTORE_SUCCESS", "Khôi phục hồ sơ nhân viên thành công.", workflowService.restoreProfile(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/admin/employees/{employeeId}/profile-timeline")
    @PreAuthorize("hasAuthority('employee.profile.timeline.view')")
    public ApiResponse<List<EmployeeProfileTimelineResponse>> listTimeline(@PathVariable Long employeeId) {
        return ApiResponse.success("EMPLOYEE_PROFILE_TIMELINE_SUCCESS", "Lấy timeline hồ sơ nhân sự thành công.", workflowService.listTimeline(employeeId), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/admin/employees/managed")
    @PreAuthorize("hasAuthority('employee.view')")
    public ApiResponse<PageResponse<EmployeeListItemResponse>> listManagedProfiles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmploymentStatus employmentStatus,
            @RequestParam(required = false) Long orgUnitId,
            @RequestParam(required = false) Long jobTitleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("EMPLOYEE_MANAGED_LIST_SUCCESS", "Lấy danh sách hồ sơ nhân viên theo phạm vi quản lý thành công.", workflowService.listManagedProfiles(keyword, employmentStatus, orgUnitId, jobTitleId, page, size), null, RequestTraceContext.getTraceId());
    }

    @GetMapping(value = "/admin/employees/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('employee.profile.import_export')")
    public ResponseEntity<String> exportEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmploymentStatus employmentStatus,
            @RequestParam(required = false) Long orgUnitId,
            @RequestParam(required = false) Long jobTitleId
    ) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(workflowService.exportEmployeeCsv(keyword, employmentStatus, orgUnitId, jobTitleId));
    }

    @PostMapping("/admin/employees/import")
    @PreAuthorize("hasAuthority('employee.profile.import_export')")
    public ApiResponse<ImportResultResponse> importEmployees(@Valid @RequestBody List<@Valid EmployeeImportRowRequest> requests) {
        return ApiResponse.success("EMPLOYEE_IMPORT_SUCCESS", "Import hồ sơ nhân sự thành công.", workflowService.importEmployees(requests), null, RequestTraceContext.getTraceId());
    }
}
