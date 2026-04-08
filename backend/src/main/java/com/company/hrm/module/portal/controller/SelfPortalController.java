package com.company.hrm.module.portal.controller;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.constant.PortalTaskStatus;
import com.company.hrm.module.attendance.dto.AttendanceAdjustmentDetailResponse;
import com.company.hrm.module.attendance.dto.AttendanceLogResponse;
import com.company.hrm.module.attendance.dto.CreateAttendanceAdjustmentRequest;
import com.company.hrm.module.attendance.dto.CreateAttendanceLogRequest;
import com.company.hrm.module.attendance.dto.CreateOvertimeRequest;
import com.company.hrm.module.attendance.dto.OvertimeListItemResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.employee.dto.ProfileChangeRequestResponse;
import com.company.hrm.module.employee.dto.SubmitProfileChangeRequest;
import com.company.hrm.module.leave.dto.CreateLeaveRequestRequest;
import com.company.hrm.module.leave.dto.LeaveRequestDetailResponse;
import com.company.hrm.module.offboarding.dto.CreateOffboardingRequest;
import com.company.hrm.module.offboarding.dto.OffboardingListItemResponse;
import com.company.hrm.module.offboarding.dto.OffboardingDetailResponse;
import com.company.hrm.module.payroll.dto.PayrollItemResponse;
import com.company.hrm.module.portal.dto.*;
import com.company.hrm.module.portal.service.PortalService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/portal")
public class SelfPortalController {

    private final PortalService portalService;

    public SelfPortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('portal.dashboard.view')")
    public ApiResponse<PortalDashboardResponse> getDashboard() {
        return ApiResponse.success("PORTAL_DASHBOARD_SUCCESS", "Lấy dashboard cá nhân thành công.",
                portalService.getDashboard(), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('portal.profile.view_self')")
    public ApiResponse<PortalProfileResponse> getMyProfile() {
        return ApiResponse.success("PORTAL_PROFILE_SUCCESS", "Lấy hồ sơ cá nhân thành công.",
                portalService.getMyProfile(), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/profile-change-requests")
    @PreAuthorize("hasAuthority('portal.profile.view_self')")
    public ApiResponse<List<ProfileChangeRequestResponse>> listMyProfileChangeRequests() {
        return ApiResponse.success("PORTAL_PROFILE_CHANGE_REQUEST_LIST_SUCCESS", "Lấy danh sách yêu cầu cập nhật hồ sơ cá nhân thành công.",
                portalService.listMyProfileChangeRequests(), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/profile-change-requests")
    @PreAuthorize("hasAuthority('portal.profile.view_self')")
    public ApiResponse<ProfileChangeRequestResponse> submitProfileChangeRequest(@Valid @RequestBody SubmitProfileChangeRequest request) {
        return ApiResponse.success("PORTAL_PROFILE_CHANGE_REQUEST_CREATE_SUCCESS", "Gửi yêu cầu cập nhật hồ sơ từ portal thành công.",
                portalService.submitProfileChangeRequestFromPortal(request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/leave")
    @PreAuthorize("hasAuthority('portal.leave.view_self')")
    public ApiResponse<PortalLeaveOverviewResponse> getLeaveOverview() {
        return ApiResponse.success("PORTAL_LEAVE_OVERVIEW_SUCCESS", "Lấy tổng quan nghỉ phép cá nhân thành công.",
                portalService.getLeaveOverview(), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/leave-requests")
    @PreAuthorize("hasAuthority('portal.leave.request_self')")
    public ApiResponse<LeaveRequestDetailResponse> createLeaveRequest(@Valid @RequestBody CreateLeaveRequestRequest request) {
        return ApiResponse.success("PORTAL_LEAVE_CREATE_SUCCESS", "Tạo đơn nghỉ từ portal thành công.",
                portalService.createLeaveFromPortal(request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/attendance")
    @PreAuthorize("hasAuthority('portal.attendance.view_self')")
    public ApiResponse<PortalAttendanceOverviewResponse> getAttendanceOverview(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate
    ) {
        return ApiResponse.success("PORTAL_ATTENDANCE_OVERVIEW_SUCCESS", "Lấy tổng quan chấm công cá nhân thành công.",
                portalService.getAttendanceOverview(fromDate, toDate), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/attendance-adjustment-requests")
    @PreAuthorize("hasAuthority('portal.attendance.adjust_self')")
    public ApiResponse<AttendanceAdjustmentDetailResponse> createAttendanceAdjustment(@Valid @RequestBody CreateAttendanceAdjustmentRequest request) {
        return ApiResponse.success("PORTAL_ATTENDANCE_ADJUST_CREATE_SUCCESS", "Tạo yêu cầu điều chỉnh công từ portal thành công.",
                portalService.createAttendanceAdjustmentFromPortal(request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/contracts")
    @PreAuthorize("hasAuthority('portal.contract.view_self')")
    public ApiResponse<PortalContractOverviewResponse> getContractOverview() {
        return ApiResponse.success("PORTAL_CONTRACT_OVERVIEW_SUCCESS", "Lấy danh sách hợp đồng cá nhân thành công.",
                portalService.getContractOverview(), null, RequestTraceContext.getTraceId());
    }

    @GetMapping(value = "/contracts/{laborContractId}/export", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasAuthority('portal.contract.view_self')")
    public ResponseEntity<String> exportContract(@PathVariable Long laborContractId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=my-contract-" + laborContractId + ".html")
                .contentType(MediaType.TEXT_HTML)
                .body(portalService.exportMyContract(laborContractId));
    }

    @GetMapping("/payroll")
    @PreAuthorize("hasAuthority('portal.payroll.view_self')")
    public ApiResponse<PortalPayrollOverviewResponse> getPayrollOverview() {
        return ApiResponse.success("PORTAL_PAYROLL_OVERVIEW_SUCCESS", "Lấy danh sách phiếu lương cá nhân thành công.",
                portalService.getPayrollOverview(), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/payroll/{payrollPeriodId}")
    @PreAuthorize("hasAuthority('portal.payroll.view_self')")
    public ApiResponse<PayrollItemResponse> getPayslipDetail(@PathVariable Long payrollPeriodId) {
        return ApiResponse.success("PORTAL_PAYROLL_DETAIL_SUCCESS", "Lấy chi tiết phiếu lương cá nhân thành công.",
                portalService.getPayslipDetail(payrollPeriodId), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/inbox")
    @PreAuthorize("hasAuthority('portal.inbox.view_self')")
    public ApiResponse<List<PortalInboxItemResponse>> listInbox() {
        return ApiResponse.success("PORTAL_INBOX_LIST_SUCCESS", "Lấy danh sách thông báo và task cá nhân thành công.",
                portalService.listInbox(), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/inbox/{portalInboxItemId}/read")
    @PreAuthorize("hasAuthority('portal.inbox.mark_read')")
    public ApiResponse<PortalInboxItemResponse> markRead(@PathVariable Long portalInboxItemId) {
        return ApiResponse.success("PORTAL_INBOX_MARK_READ_SUCCESS", "Đánh dấu đã đọc thành công.",
                portalService.markInboxRead(portalInboxItemId), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasAuthority('portal.tasks.view_self')")
    public ApiResponse<List<PortalInboxItemResponse>> listTasks() {
        return ApiResponse.success("PORTAL_TASK_LIST_SUCCESS", "Lấy danh sách nhiệm vụ cá nhân thành công.",
                portalService.listMyTasks(), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/tasks/{portalInboxItemId}/status")
    @PreAuthorize("hasAuthority('portal.tasks.update_self')")
    public ApiResponse<PortalInboxItemResponse> updateTaskStatus(@PathVariable Long portalInboxItemId, @RequestParam PortalTaskStatus status) {
        return ApiResponse.success("PORTAL_TASK_UPDATE_SUCCESS", "Cập nhật trạng thái nhiệm vụ thành công.",
                portalService.updateTaskStatus(portalInboxItemId, status), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/check-in/status")
    @PreAuthorize("hasAuthority('portal.attendance.view_self')")
    public ApiResponse<PortalCheckInStatusResponse> getCheckInStatus() {
        return ApiResponse.success("PORTAL_CHECKIN_STATUS_SUCCESS", "Lấy trạng thái chấm công hiện tại thành công.",
                portalService.getCheckInStatus(), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAuthority('portal.attendance.checkin_self')")
    public ApiResponse<AttendanceLogResponse> checkIn(@Valid @RequestBody CreateAttendanceLogRequest request) {
        return ApiResponse.success("PORTAL_CHECKIN_SUCCESS", "Check-in thành công.",
                portalService.checkIn(request), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAuthority('portal.attendance.checkin_self')")
    public ApiResponse<AttendanceLogResponse> checkOut(@Valid @RequestBody CreateAttendanceLogRequest request) {
        return ApiResponse.success("PORTAL_CHECKOUT_SUCCESS", "Check-out thành công.",
                portalService.checkOut(request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/overtime-requests")
    @PreAuthorize("hasAuthority('portal.attendance.view_self')")
    public ApiResponse<List<OvertimeListItemResponse>> listMyOvertimeRequests() {
        return ApiResponse.success("PORTAL_OT_LIST_SUCCESS", "Lấy danh sách yêu cầu OT cá nhân thành công.",
                portalService.listMyOvertimeRequests(), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/overtime-requests")
    @PreAuthorize("hasAuthority('portal.attendance.adjust_self')")
    public ApiResponse<OvertimeListItemResponse> submitOvertimeRequest(@Valid @RequestBody CreateOvertimeRequest request) {
        return ApiResponse.success("PORTAL_OT_CREATE_SUCCESS", "Gửi yêu cầu OT từ portal thành công.",
                portalService.submitOvertimeRequest(request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/resignation-requests")
    @PreAuthorize("hasAuthority('portal.offboarding.view_self')")
    public ApiResponse<List<OffboardingListItemResponse>> listMyResignationRequests() {
        return ApiResponse.success("PORTAL_RESIGNATION_LIST_SUCCESS", "Lấy danh sách yêu cầu nghỉ việc thành công.",
                portalService.listMyResignationRequests(), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/resignation-requests")
    @PreAuthorize("hasAuthority('portal.offboarding.request_self')")
    public ApiResponse<OffboardingDetailResponse> submitResignationRequest(@Valid @RequestBody CreateOffboardingRequest request) {
        return ApiResponse.success("PORTAL_RESIGNATION_CREATE_SUCCESS", "Gửi yêu cầu nghỉ việc thành công.",
                portalService.submitResignationRequest(request), null, RequestTraceContext.getTraceId());
    }
}
