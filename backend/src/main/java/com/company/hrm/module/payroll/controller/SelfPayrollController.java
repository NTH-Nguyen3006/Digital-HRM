package com.company.hrm.module.payroll.controller;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.payroll.dto.PayrollItemResponse;
import com.company.hrm.module.payroll.dto.SelfPayslipListItemResponse;
import com.company.hrm.module.payroll.service.PayrollService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/payroll")
public class SelfPayrollController {

    private final PayrollService payrollService;

    public SelfPayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping("/payslips")
    @PreAuthorize("hasAuthority('payroll.payslip.view_self')")
    public ApiResponse<List<SelfPayslipListItemResponse>> listMyPayslips() {
        return ApiResponse.success("PAYROLL_SELF_PAYSLIP_LIST_SUCCESS", "Lấy danh sách phiếu lương cá nhân thành công.",
                payrollService.listMyPayslips(), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/payslips/{payrollPeriodId}")
    @PreAuthorize("hasAuthority('payroll.payslip.view_self')")
    public ApiResponse<PayrollItemResponse> getMyPayslip(@PathVariable Long payrollPeriodId) {
        return ApiResponse.success("PAYROLL_SELF_PAYSLIP_DETAIL_SUCCESS", "Lấy chi tiết phiếu lương cá nhân thành công.",
                payrollService.getMyPayslip(payrollPeriodId), null, RequestTraceContext.getTraceId());
    }
}
