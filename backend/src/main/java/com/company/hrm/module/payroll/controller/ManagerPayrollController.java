package com.company.hrm.module.payroll.controller;

import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.payroll.dto.ManagerConfirmPayrollItemRequest;
import com.company.hrm.module.payroll.dto.PayrollItemResponse;
import com.company.hrm.module.payroll.service.PayrollService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manager/payroll")
public class ManagerPayrollController {

    private final PayrollService payrollService;

    public ManagerPayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping("/periods/{payrollPeriodId}/items")
    @PreAuthorize("hasAuthority('payroll.team.confirm')")
    public ApiResponse<List<PayrollItemResponse>> listPayrollItemsForManager(@PathVariable Long payrollPeriodId) {
        return ApiResponse.success("PAYROLL_MANAGER_ITEM_LIST_SUCCESS", "Lấy bảng lương team thành công.",
                payrollService.listPayrollItemsForManager(payrollPeriodId), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/items/{payrollItemId}/confirm")
    @PreAuthorize("hasAuthority('payroll.team.confirm')")
    public ApiResponse<PayrollItemResponse> confirmPayrollItem(@PathVariable Long payrollItemId,
                                                               @Valid @RequestBody ManagerConfirmPayrollItemRequest request) {
        return ApiResponse.success("PAYROLL_MANAGER_CONFIRM_SUCCESS", "Xác nhận bảng lương team thành công.",
                payrollService.confirmPayrollItemByManager(payrollItemId, request), null, RequestTraceContext.getTraceId());
    }
}
