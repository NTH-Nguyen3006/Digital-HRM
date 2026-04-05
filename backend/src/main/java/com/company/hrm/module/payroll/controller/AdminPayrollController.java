package com.company.hrm.module.payroll.controller;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.payroll.dto.*;
import com.company.hrm.module.payroll.service.PayrollService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/payroll")
public class AdminPayrollController {

    private final PayrollService payrollService;

    public AdminPayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping("/components")
    @PreAuthorize("hasAuthority('payroll.component.view')")
    public ApiResponse<PageResponse<SalaryComponentResponse>> listComponents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RecordStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("PAYROLL_COMPONENT_LIST_SUCCESS", "Lấy danh sách thành phần lương thành công.",
                payrollService.listComponents(keyword, status, page, size), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/components/{salaryComponentId}")
    @PreAuthorize("hasAuthority('payroll.component.view')")
    public ApiResponse<SalaryComponentResponse> getComponent(@PathVariable Long salaryComponentId) {
        return ApiResponse.success("PAYROLL_COMPONENT_DETAIL_SUCCESS", "Lấy chi tiết thành phần lương thành công.",
                payrollService.getComponent(salaryComponentId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/components")
    @PreAuthorize("hasAuthority('payroll.component.manage')")
    public ApiResponse<SalaryComponentResponse> createComponent(@Valid @RequestBody SalaryComponentUpsertRequest request) {
        return ApiResponse.success("PAYROLL_COMPONENT_CREATE_SUCCESS", "Tạo thành phần lương thành công.",
                payrollService.upsertComponent(null, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/components/{salaryComponentId}")
    @PreAuthorize("hasAuthority('payroll.component.manage')")
    public ApiResponse<SalaryComponentResponse> updateComponent(@PathVariable Long salaryComponentId,
                                                                @Valid @RequestBody SalaryComponentUpsertRequest request) {
        return ApiResponse.success("PAYROLL_COMPONENT_UPDATE_SUCCESS", "Cập nhật thành phần lương thành công.",
                payrollService.upsertComponent(salaryComponentId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/formulas")
    @PreAuthorize("hasAuthority('payroll.formula.manage')")
    public ApiResponse<List<FormulaVersionResponse>> listFormulaVersions() {
        return ApiResponse.success("PAYROLL_FORMULA_LIST_SUCCESS", "Lấy danh sách cấu hình công thức lương thành công.",
                payrollService.listFormulaVersions(), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/formulas/{formulaVersionId}")
    @PreAuthorize("hasAuthority('payroll.formula.manage')")
    public ApiResponse<FormulaVersionResponse> getFormulaVersion(@PathVariable Long formulaVersionId) {
        return ApiResponse.success("PAYROLL_FORMULA_DETAIL_SUCCESS", "Lấy chi tiết cấu hình công thức lương thành công.",
                payrollService.getFormulaVersion(formulaVersionId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/formulas")
    @PreAuthorize("hasAuthority('payroll.formula.manage')")
    public ApiResponse<FormulaVersionResponse> createFormulaVersion(@Valid @RequestBody FormulaVersionUpsertRequest request) {
        return ApiResponse.success("PAYROLL_FORMULA_CREATE_SUCCESS", "Tạo cấu hình công thức lương thành công.",
                payrollService.upsertFormulaVersion(null, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/formulas/{formulaVersionId}")
    @PreAuthorize("hasAuthority('payroll.formula.manage')")
    public ApiResponse<FormulaVersionResponse> updateFormulaVersion(@PathVariable Long formulaVersionId,
                                                                    @Valid @RequestBody FormulaVersionUpsertRequest request) {
        return ApiResponse.success("PAYROLL_FORMULA_UPDATE_SUCCESS", "Cập nhật cấu hình công thức lương thành công.",
                payrollService.upsertFormulaVersion(formulaVersionId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/compensations")
    @PreAuthorize("hasAuthority('payroll.compensation.manage')")
    public ApiResponse<PageResponse<EmployeeCompensationListItemResponse>> listCompensations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long orgUnitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("PAYROLL_COMPENSATION_LIST_SUCCESS", "Lấy danh sách cấu hình lương nhân viên thành công.",
                payrollService.listCompensations(keyword, orgUnitId, page, size), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/compensations/{employeeId}")
    @PreAuthorize("hasAuthority('payroll.compensation.manage')")
    public ApiResponse<EmployeeCompensationResponse> getEmployeeCompensation(@PathVariable Long employeeId) {
        return ApiResponse.success("PAYROLL_COMPENSATION_DETAIL_SUCCESS", "Lấy cấu hình lương nhân viên thành công.",
                payrollService.getEmployeeCompensation(employeeId), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/compensations/{employeeId}")
    @PreAuthorize("hasAuthority('payroll.compensation.manage')")
    public ApiResponse<EmployeeCompensationResponse> upsertEmployeeCompensation(@PathVariable Long employeeId,
                                                                                @Valid @RequestBody EmployeeCompensationUpsertRequest request) {
        return ApiResponse.success("PAYROLL_COMPENSATION_UPSERT_SUCCESS", "Thiết lập lương cho nhân viên thành công.",
                payrollService.upsertEmployeeCompensation(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/tax-profiles/{employeeId}")
    @PreAuthorize("hasAuthority('payroll.tax_profile.manage')")
    public ApiResponse<PersonalTaxProfileResponse> getPersonalTaxProfile(@PathVariable Long employeeId) {
        return ApiResponse.success("PAYROLL_TAX_PROFILE_DETAIL_SUCCESS", "Lấy hồ sơ thuế cá nhân thành công.",
                payrollService.getPersonalTaxProfile(employeeId), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/tax-profiles/{employeeId}")
    @PreAuthorize("hasAuthority('payroll.tax_profile.manage')")
    public ApiResponse<PersonalTaxProfileResponse> upsertPersonalTaxProfile(@PathVariable Long employeeId,
                                                                            @Valid @RequestBody PersonalTaxProfileUpsertRequest request) {
        return ApiResponse.success("PAYROLL_TAX_PROFILE_UPSERT_SUCCESS", "Thiết lập hồ sơ thuế cá nhân thành công.",
                payrollService.upsertPersonalTaxProfile(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/tax-profiles/{employeeId}/dependents")
    @PreAuthorize("hasAuthority('payroll.tax_profile.manage')")
    public ApiResponse<TaxDependentResponse> addDependent(@PathVariable Long employeeId,
                                                          @Valid @RequestBody TaxDependentUpsertRequest request) {
        return ApiResponse.success("PAYROLL_DEPENDENT_CREATE_SUCCESS", "Thêm người phụ thuộc thành công.",
                payrollService.addDependent(employeeId, request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/dependents/{taxDependentId}")
    @PreAuthorize("hasAuthority('payroll.tax_profile.manage')")
    public ApiResponse<TaxDependentResponse> updateDependent(@PathVariable Long taxDependentId,
                                                             @Valid @RequestBody TaxDependentUpsertRequest request) {
        return ApiResponse.success("PAYROLL_DEPENDENT_UPDATE_SUCCESS", "Cập nhật người phụ thuộc thành công.",
                payrollService.updateDependent(taxDependentId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/dependents/{taxDependentId}/deactivate")
    @PreAuthorize("hasAuthority('payroll.tax_profile.manage')")
    public ApiResponse<TaxDependentResponse> deactivateDependent(@PathVariable Long taxDependentId,
                                                                 @Valid @RequestBody DeactivateTaxDependentRequest request) {
        return ApiResponse.success("PAYROLL_DEPENDENT_DEACTIVATE_SUCCESS", "Ngừng hiệu lực người phụ thuộc thành công.",
                payrollService.deactivateDependent(taxDependentId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/periods")
    @PreAuthorize("hasAuthority('payroll.period.view')")
    public ApiResponse<List<PayrollPeriodResponse>> listPayrollPeriods() {
        return ApiResponse.success("PAYROLL_PERIOD_LIST_SUCCESS", "Lấy danh sách kỳ lương thành công.",
                payrollService.listPayrollPeriods(), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/periods")
    @PreAuthorize("hasAuthority('payroll.period.create')")
    public ApiResponse<PayrollPeriodResponse> createPayrollPeriod(@Valid @RequestBody PayrollPeriodCreateRequest request) {
        return ApiResponse.success("PAYROLL_PERIOD_CREATE_SUCCESS", "Tạo kỳ lương thành công.",
                payrollService.createPayrollPeriod(request), null, RequestTraceContext.getTraceId());
    }

    @PostMapping("/periods/{payrollPeriodId}/generate-draft")
    @PreAuthorize("hasAuthority('payroll.period.generate')")
    public ApiResponse<PayrollPeriodResponse> generatePayrollDraft(@PathVariable Long payrollPeriodId,
                                                                   @Valid @RequestBody GeneratePayrollDraftRequest request) {
        return ApiResponse.success("PAYROLL_PERIOD_GENERATE_SUCCESS", "Tạo bảng lương nháp thành công.",
                payrollService.generatePayrollDraft(payrollPeriodId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/periods/{payrollPeriodId}/items")
    @PreAuthorize("hasAuthority('payroll.period.view')")
    public ApiResponse<PageResponse<PayrollItemResponse>> listPayrollItemsForHr(
            @PathVariable Long payrollPeriodId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long orgUnitId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("PAYROLL_ITEM_LIST_SUCCESS", "Lấy danh sách dòng lương thành công.",
                payrollService.listPayrollItemsForHr(payrollPeriodId, keyword, orgUnitId, employeeId, page, size), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/items/{payrollItemId}")
    @PreAuthorize("hasAuthority('payroll.period.view')")
    public ApiResponse<PayrollItemResponse> getPayrollItem(@PathVariable Long payrollItemId) {
        return ApiResponse.success("PAYROLL_ITEM_DETAIL_SUCCESS", "Lấy chi tiết dòng lương thành công.",
                payrollService.getPayrollItem(payrollItemId), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/items/{payrollItemId}/adjust")
    @PreAuthorize("hasAuthority('payroll.draft.adjust')")
    public ApiResponse<PayrollItemResponse> adjustPayrollItem(@PathVariable Long payrollItemId,
                                                              @Valid @RequestBody AdjustPayrollItemRequest request) {
        return ApiResponse.success("PAYROLL_ITEM_ADJUST_SUCCESS", "Điều chỉnh dòng lương thành công.",
                payrollService.adjustPayrollItem(payrollItemId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/periods/{payrollPeriodId}/approve")
    @PreAuthorize("hasAuthority('payroll.period.approve')")
    public ApiResponse<PayrollPeriodResponse> approvePayrollPeriod(@PathVariable Long payrollPeriodId,
                                                                   @Valid @RequestBody ApprovePayrollPeriodRequest request) {
        return ApiResponse.success("PAYROLL_PERIOD_APPROVE_SUCCESS", "Phê duyệt bảng lương thành công.",
                payrollService.approvePayrollPeriod(payrollPeriodId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/periods/{payrollPeriodId}/publish")
    @PreAuthorize("hasAuthority('payroll.payslip.publish')")
    public ApiResponse<PayrollPeriodResponse> publishPayrollPeriod(@PathVariable Long payrollPeriodId,
                                                                   @Valid @RequestBody PublishPayrollPeriodRequest request) {
        return ApiResponse.success("PAYROLL_PERIOD_PUBLISH_SUCCESS", "Phát hành phiếu lương thành công.",
                payrollService.publishPayrollPeriod(payrollPeriodId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping(value = "/reports/bank-transfer/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('payroll.bank.export')")
    public ResponseEntity<String> exportBankTransfer(@RequestParam Long payrollPeriodId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll-bank-transfer.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(payrollService.exportBankTransferCsv(payrollPeriodId));
    }

    @GetMapping(value = "/reports/pit/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('payroll.tax.export')")
    public ResponseEntity<String> exportPitReport(@RequestParam Long payrollPeriodId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll-pit-report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(payrollService.exportPitReportCsv(payrollPeriodId));
    }
}
