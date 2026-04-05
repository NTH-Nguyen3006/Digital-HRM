package com.company.hrm.module.reporting.controller;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.reporting.dto.HeadcountDashboardResponse;
import com.company.hrm.module.reporting.dto.ReportScheduleConfigResponse;
import com.company.hrm.module.reporting.dto.ReportScheduleConfigUpsertRequest;
import com.company.hrm.module.reporting.dto.ReportScheduleRunResponse;
import com.company.hrm.module.reporting.dto.SystemHealthDashboardResponse;
import com.company.hrm.module.reporting.service.ReportingService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reports")
public class AdminReportingController {

    private final ReportingService reportingService;

    public AdminReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/dashboard/headcount")
    @PreAuthorize("hasAuthority('report.dashboard.headcount.view')")
    public ApiResponse<HeadcountDashboardResponse> getHeadcountDashboard() {
        return ApiResponse.success("REPORT_HEADCOUNT_DASHBOARD_SUCCESS", "Lấy dashboard headcount thành công.",
                reportingService.getHeadcountDashboard(), null, RequestTraceContext.getTraceId());
    }

    @GetMapping(value = "/org-movement/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('report.org_movement.export')")
    public ResponseEntity<byte[]> exportOrgMovement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long orgUnitId
    ) {
        String csv = reportingService.exportOrgMovementCsv(fromDate, toDate, orgUnitId);
        return csvResponse("org-movement.csv", csv);
    }

    @GetMapping(value = "/contracts/expiry/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('report.contract_expiry.export')")
    public ResponseEntity<byte[]> exportContractExpiry(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long orgUnitId
    ) {
        String csv = reportingService.exportContractExpiryCsv(fromDate, toDate, orgUnitId);
        return csvResponse("contract-expiry.csv", csv);
    }

    @GetMapping(value = "/leave-balances/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('report.leave_balance.export')")
    public ResponseEntity<byte[]> exportLeaveBalances(
            @RequestParam(required = false) Integer leaveYear,
            @RequestParam(required = false) Long orgUnitId
    ) {
        return csvResponse("leave-balance.csv", reportingService.exportLeaveBalanceCsv(leaveYear, orgUnitId));
    }

    @GetMapping(value = "/attendance/anomaly-ot/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('report.attendance_anomaly.export')")
    public ResponseEntity<byte[]> exportAttendanceAnomalyOt(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long orgUnitId
    ) {
        return csvResponse("attendance-anomaly-ot.csv", reportingService.exportAttendanceAnomalyOtCsv(fromDate, toDate, orgUnitId));
    }

    @GetMapping(value = "/payroll/summary/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('report.payroll_summary.export')")
    public ResponseEntity<byte[]> exportPayrollSummary(@RequestParam Long payrollPeriodId) {
        return csvResponse("payroll-summary.csv", reportingService.exportPayrollSummaryCsv(payrollPeriodId));
    }

    @GetMapping(value = "/payroll/pit/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('report.pit.export')")
    public ResponseEntity<byte[]> exportPit(@RequestParam Long payrollPeriodId) {
        return csvResponse("payroll-pit.csv", reportingService.exportPitCsv(payrollPeriodId));
    }

    @GetMapping(value = "/onboarding-offboarding/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('report.onboarding_offboarding.export')")
    public ResponseEntity<byte[]> exportOnboardingOffboarding(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long orgUnitId
    ) {
        return csvResponse("onboarding-offboarding.csv", reportingService.exportOnboardingOffboardingCsv(fromDate, toDate, orgUnitId));
    }

    @GetMapping(value = "/audit/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('report.audit.export')")
    public ResponseEntity<byte[]> exportAudit(
            @RequestParam(required = false) String moduleCode,
            @RequestParam(required = false) String actionCode,
            @RequestParam(required = false) String resultCode,
            @RequestParam(required = false) String actorUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return csvResponse("audit-log.csv", reportingService.exportAuditCsv(moduleCode, actionCode, resultCode, actorUsername, from, to));
    }

    @GetMapping("/system-health")
    @PreAuthorize("hasAuthority('report.system_health.view')")
    public ApiResponse<SystemHealthDashboardResponse> getSystemHealthDashboard() {
        return ApiResponse.success("REPORT_SYSTEM_HEALTH_SUCCESS", "Lấy dashboard sức khỏe hệ thống thành công.",
                reportingService.getSystemHealthDashboard(), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/schedules")
    @PreAuthorize("hasAuthority('report.schedule.manage')")
    public ApiResponse<List<ReportScheduleConfigResponse>> listSchedules() {
        return ApiResponse.success("REPORT_SCHEDULE_LIST_SUCCESS", "Lấy danh sách lịch báo cáo thành công.",
                reportingService.listSchedules(), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/schedules")
    @PreAuthorize("hasAuthority('report.schedule.manage')")
    public ApiResponse<ReportScheduleConfigResponse> createSchedule(@Valid @RequestBody ReportScheduleConfigUpsertRequest request) {
        return ApiResponse.success("REPORT_SCHEDULE_CREATE_SUCCESS", "Tạo lịch báo cáo thành công.",
                reportingService.createSchedule(request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/schedules/{reportScheduleConfigId}")
    @PreAuthorize("hasAuthority('report.schedule.manage')")
    public ApiResponse<ReportScheduleConfigResponse> updateSchedule(@PathVariable Long reportScheduleConfigId,
                                                                    @Valid @RequestBody ReportScheduleConfigUpsertRequest request) {
        return ApiResponse.success("REPORT_SCHEDULE_UPDATE_SUCCESS", "Cập nhật lịch báo cáo thành công.",
                reportingService.updateSchedule(reportScheduleConfigId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/schedules/{reportScheduleConfigId}/runs")
    @PreAuthorize("hasAuthority('report.schedule.manage')")
    public ApiResponse<List<ReportScheduleRunResponse>> listScheduleRuns(@PathVariable Long reportScheduleConfigId) {
        return ApiResponse.success("REPORT_SCHEDULE_RUN_LIST_SUCCESS", "Lấy lịch sử chạy báo cáo thành công.",
                reportingService.listScheduleRuns(reportScheduleConfigId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/schedules/{reportScheduleConfigId}/run-now")
    @PreAuthorize("hasAuthority('report.schedule.manage')")
    public ApiResponse<ReportScheduleRunResponse> runScheduleNow(@PathVariable Long reportScheduleConfigId) {
        return ApiResponse.success("REPORT_SCHEDULE_RUN_NOW_SUCCESS", "Thực thi lịch báo cáo thành công.",
                reportingService.runScheduleNow(reportScheduleConfigId), null, RequestTraceContext.getTraceId());
    }

    private ResponseEntity<byte[]> csvResponse(String fileName, String csv) {
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }
}
