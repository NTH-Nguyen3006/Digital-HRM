package com.company.hrm.module.reporting.controller;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.reporting.dto.TeamDashboardResponse;
import com.company.hrm.module.reporting.service.ReportingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/manager/reports")
public class ManagerReportingController {

    private final ReportingService reportingService;

    public ManagerReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/dashboard/team")
    @PreAuthorize("hasAuthority('report.dashboard.team.view')")
    public ApiResponse<TeamDashboardResponse> getTeamDashboard() {
        return ApiResponse.success("REPORT_TEAM_DASHBOARD_SUCCESS", "Lấy dashboard đội nhóm thành công.",
                reportingService.getTeamDashboard(), null, RequestTraceContext.getTraceId());
    }
}
