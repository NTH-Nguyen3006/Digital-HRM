package com.company.hrm.module.contract.controller;

import com.company.hrm.common.constant.ContractStatus;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.contract.dto.*;
import com.company.hrm.module.contract.service.LaborContractService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/contracts")
public class AdminLaborContractController {

    private final LaborContractService laborContractService;

    public AdminLaborContractController(LaborContractService laborContractService) {
        this.laborContractService = laborContractService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('contract.view')")
    public ApiResponse<PageResponse<LaborContractListItemResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long contractTypeId,
            @RequestParam(required = false) ContractStatus contractStatus,
            @RequestParam(required = false) Integer expiringWithinDays,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(
                "LABOR_CONTRACT_LIST_SUCCESS",
                "Lấy danh sách hợp đồng thành công.",
                laborContractService.list(keyword, employeeId, contractTypeId, contractStatus, expiringWithinDays, page, size),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAuthority('contract.view')")
    public ApiResponse<List<LaborContractListItemResponse>> listExpiring(@RequestParam(defaultValue = "30") int days) {
        return ApiResponse.success(
                "LABOR_CONTRACT_EXPIRING_SUCCESS",
                "Lấy danh sách hợp đồng sắp hết hạn thành công.",
                laborContractService.listExpiringContracts(days),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @GetMapping("/{laborContractId}")
    @PreAuthorize("hasAuthority('contract.view')")
    public ApiResponse<LaborContractDetailResponse> getDetail(@PathVariable Long laborContractId) {
        return ApiResponse.success(
                "LABOR_CONTRACT_DETAIL_SUCCESS",
                "Lấy chi tiết hợp đồng thành công.",
                laborContractService.getDetail(laborContractId),
                null,
                RequestTraceContext.getTraceId()
        );
    }


    @GetMapping(value = "/{laborContractId}/export", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasAuthority('contract.export')")
    public ResponseEntity<String> exportContract(@PathVariable Long laborContractId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contract-" + laborContractId + ".html")
                .contentType(MediaType.TEXT_HTML)
                .body(laborContractService.exportContractHtml(laborContractId));
    }

    @GetMapping(value = "/{laborContractId}/appendices/{appendixId}/export", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasAuthority('contract.export')")
    public ResponseEntity<String> exportAppendix(@PathVariable Long laborContractId, @PathVariable Long appendixId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contract-appendix-" + appendixId + ".html")
                .contentType(MediaType.TEXT_HTML)
                .body(laborContractService.exportAppendixHtml(laborContractId, appendixId));
    }

    @GetMapping("/{laborContractId}/history")
    @PreAuthorize("hasAuthority('contract.history.view')")
    public ApiResponse<List<ContractStatusHistoryResponse>> getHistory(@PathVariable Long laborContractId) {
        return ApiResponse.success(
                "LABOR_CONTRACT_HISTORY_SUCCESS",
                "Lấy lịch sử thay đổi trạng thái hợp đồng thành công.",
                laborContractService.getStatusHistory(laborContractId),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('contract.create')")
    public ApiResponse<LaborContractDetailResponse> createDraft(@Valid @RequestBody LaborContractUpsertRequest request) {
        return ApiResponse.success(
                "LABOR_CONTRACT_CREATE_DRAFT_SUCCESS",
                "Tạo bản nháp hợp đồng thành công.",
                laborContractService.createDraft(request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PutMapping("/{laborContractId}")
    @PreAuthorize("hasAuthority('contract.update')")
    public ApiResponse<LaborContractDetailResponse> updateDraft(@PathVariable Long laborContractId, @Valid @RequestBody LaborContractUpsertRequest request) {
        return ApiResponse.success(
                "LABOR_CONTRACT_UPDATE_DRAFT_SUCCESS",
                "Cập nhật bản nháp hợp đồng thành công.",
                laborContractService.updateDraft(laborContractId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @DeleteMapping("/{laborContractId}")
    @PreAuthorize("hasAuthority('contract.cancel_draft')")
    public ApiResponse<Void> cancelDraft(@PathVariable Long laborContractId, @Valid @RequestBody(required = false) ContractActionRequest request) {
        laborContractService.cancelDraft(laborContractId, request);
        return ApiResponse.success(
                "LABOR_CONTRACT_CANCEL_DRAFT_SUCCESS",
                "Hủy bản nháp hợp đồng thành công.",
                RequestTraceContext.getTraceId()
        );
    }

    @PatchMapping("/{laborContractId}/submit")
    @PreAuthorize("hasAuthority('contract.submit')")
    public ApiResponse<LaborContractDetailResponse> submit(@PathVariable Long laborContractId, @Valid @RequestBody(required = false) ContractActionRequest request) {
        return ApiResponse.success(
                "LABOR_CONTRACT_SUBMIT_SUCCESS",
                "Gửi hợp đồng sang bước chờ ký thành công.",
                laborContractService.submitForSigning(laborContractId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PatchMapping("/{laborContractId}/review")
    @PreAuthorize("hasAuthority('contract.review')")
    public ApiResponse<LaborContractDetailResponse> review(@PathVariable Long laborContractId, @Valid @RequestBody ReviewContractRequest request) {
        return ApiResponse.success(
                "LABOR_CONTRACT_REVIEW_SUCCESS",
                "Xử lý duyệt/từ chối hợp đồng thành công.",
                laborContractService.review(laborContractId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PatchMapping("/{laborContractId}/activate")
    @PreAuthorize("hasAuthority('contract.activate')")
    public ApiResponse<LaborContractDetailResponse> activate(@PathVariable Long laborContractId, @Valid @RequestBody(required = false) ContractActionRequest request) {
        return ApiResponse.success(
                "LABOR_CONTRACT_ACTIVATE_SUCCESS",
                "Chốt hiệu lực hợp đồng thành công.",
                laborContractService.activate(laborContractId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PatchMapping("/{laborContractId}/lifecycle-status")
    @PreAuthorize("hasAuthority('contract.change_status')")
    public ApiResponse<LaborContractDetailResponse> changeLifecycleStatus(
            @PathVariable Long laborContractId,
            @Valid @RequestBody ChangeContractLifecycleStatusRequest request
    ) {
        return ApiResponse.success(
                "LABOR_CONTRACT_STATUS_SUCCESS",
                "Cập nhật trạng thái vòng đời hợp đồng thành công.",
                laborContractService.changeLifecycleStatus(laborContractId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PostMapping("/{laborContractId}/renewal-draft")
    @PreAuthorize("hasAuthority('contract.create')")
    public ApiResponse<LaborContractDetailResponse> createRenewalDraft(
            @PathVariable Long laborContractId,
            @Valid @RequestBody RenewLaborContractRequest request
    ) {
        return ApiResponse.success(
                "LABOR_CONTRACT_RENEWAL_DRAFT_SUCCESS",
                "Tạo bản nháp hợp đồng kế nhiệm thành công.",
                laborContractService.createRenewalDraft(laborContractId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @GetMapping("/{laborContractId}/appendices")
    @PreAuthorize("hasAuthority('contract.appendix.manage')")
    public ApiResponse<List<ContractAppendixResponse>> listAppendices(@PathVariable Long laborContractId) {
        return ApiResponse.success(
                "CONTRACT_APPENDIX_LIST_SUCCESS",
                "Lấy danh sách phụ lục hợp đồng thành công.",
                laborContractService.listAppendices(laborContractId),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PostMapping("/{laborContractId}/appendices")
    @PreAuthorize("hasAuthority('contract.appendix.manage')")
    public ApiResponse<ContractAppendixResponse> createAppendix(
            @PathVariable Long laborContractId,
            @Valid @RequestBody ContractAppendixRequest request
    ) {
        return ApiResponse.success(
                "CONTRACT_APPENDIX_CREATE_SUCCESS",
                "Tạo phụ lục hợp đồng thành công.",
                laborContractService.createAppendix(laborContractId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PutMapping("/{laborContractId}/appendices/{appendixId}")
    @PreAuthorize("hasAuthority('contract.appendix.manage')")
    public ApiResponse<ContractAppendixResponse> updateAppendix(
            @PathVariable Long laborContractId,
            @PathVariable Long appendixId,
            @Valid @RequestBody ContractAppendixRequest request
    ) {
        return ApiResponse.success(
                "CONTRACT_APPENDIX_UPDATE_SUCCESS",
                "Cập nhật phụ lục hợp đồng thành công.",
                laborContractService.updateAppendix(laborContractId, appendixId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PatchMapping("/{laborContractId}/appendices/{appendixId}/activate")
    @PreAuthorize("hasAuthority('contract.appendix.manage')")
    public ApiResponse<ContractAppendixResponse> activateAppendix(
            @PathVariable Long laborContractId,
            @PathVariable Long appendixId,
            @Valid @RequestBody(required = false) ContractActionRequest request
    ) {
        return ApiResponse.success(
                "CONTRACT_APPENDIX_ACTIVATE_SUCCESS",
                "Kích hoạt phụ lục hợp đồng thành công.",
                laborContractService.activateAppendix(laborContractId, appendixId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PatchMapping("/{laborContractId}/appendices/{appendixId}/cancel")
    @PreAuthorize("hasAuthority('contract.appendix.manage')")
    public ApiResponse<ContractAppendixResponse> cancelAppendix(
            @PathVariable Long laborContractId,
            @PathVariable Long appendixId,
            @Valid @RequestBody(required = false) ContractActionRequest request
    ) {
        return ApiResponse.success(
                "CONTRACT_APPENDIX_CANCEL_SUCCESS",
                "Hủy phụ lục hợp đồng thành công.",
                laborContractService.cancelAppendix(laborContractId, appendixId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @GetMapping("/{laborContractId}/attachments")
    @PreAuthorize("hasAuthority('contract.attachment.manage')")
    public ApiResponse<List<ContractAttachmentResponse>> listAttachments(@PathVariable Long laborContractId) {
        return ApiResponse.success(
                "CONTRACT_ATTACHMENT_LIST_SUCCESS",
                "Lấy danh sách file hợp đồng thành công.",
                laborContractService.listAttachments(laborContractId),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PostMapping("/{laborContractId}/attachments")
    @PreAuthorize("hasAuthority('contract.attachment.manage')")
    public ApiResponse<ContractAttachmentResponse> createAttachment(
            @PathVariable Long laborContractId,
            @Valid @RequestBody ContractAttachmentRequest request
    ) {
        return ApiResponse.success(
                "CONTRACT_ATTACHMENT_CREATE_SUCCESS",
                "Tạo metadata file hợp đồng thành công.",
                laborContractService.createAttachment(laborContractId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PutMapping("/{laborContractId}/attachments/{attachmentId}")
    @PreAuthorize("hasAuthority('contract.attachment.manage')")
    public ApiResponse<ContractAttachmentResponse> updateAttachment(
            @PathVariable Long laborContractId,
            @PathVariable Long attachmentId,
            @Valid @RequestBody ContractAttachmentRequest request
    ) {
        return ApiResponse.success(
                "CONTRACT_ATTACHMENT_UPDATE_SUCCESS",
                "Cập nhật metadata file hợp đồng thành công.",
                laborContractService.updateAttachment(laborContractId, attachmentId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @PatchMapping("/{laborContractId}/attachments/{attachmentId}/archive")
    @PreAuthorize("hasAuthority('contract.attachment.manage')")
    public ApiResponse<ContractAttachmentResponse> archiveAttachment(
            @PathVariable Long laborContractId,
            @PathVariable Long attachmentId,
            @Valid @RequestBody(required = false) ContractActionRequest request
    ) {
        return ApiResponse.success(
                "CONTRACT_ATTACHMENT_ARCHIVE_SUCCESS",
                "Lưu trữ file hợp đồng thành công.",
                laborContractService.archiveAttachment(laborContractId, attachmentId, request),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @DeleteMapping("/{laborContractId}/attachments/{attachmentId}")
    @PreAuthorize("hasAuthority('contract.attachment.manage')")
    public ApiResponse<Void> deleteAttachment(@PathVariable Long laborContractId, @PathVariable Long attachmentId) {
        laborContractService.deleteAttachment(laborContractId, attachmentId);
        return ApiResponse.success(
                "CONTRACT_ATTACHMENT_DELETE_SUCCESS",
                "Xóa mềm metadata file hợp đồng thành công.",
                RequestTraceContext.getTraceId()
        );
    }
}
