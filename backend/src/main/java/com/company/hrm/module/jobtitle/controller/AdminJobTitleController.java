package com.company.hrm.module.jobtitle.controller;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.jobtitle.dto.*;
import com.company.hrm.module.jobtitle.service.JobTitleService;
import com.company.hrm.common.dto.ImportResultResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/job-titles")
public class AdminJobTitleController {

    private final JobTitleService jobTitleService;

    public AdminJobTitleController(JobTitleService jobTitleService) {
        this.jobTitleService = jobTitleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('jobtitle.view')")
    public ApiResponse<PageResponse<JobTitleListItemResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RecordStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("JOB_TITLE_LIST_SUCCESS", "Lấy danh sách chức danh thành công.", jobTitleService.list(keyword, status, page, size), null, RequestTraceContext.getTraceId());
    }

    @GetMapping("/{jobTitleId}")
    @PreAuthorize("hasAuthority('jobtitle.view')")
    public ApiResponse<JobTitleDetailResponse> getDetail(@PathVariable Long jobTitleId) {
        return ApiResponse.success("JOB_TITLE_DETAIL_SUCCESS", "Lấy chi tiết chức danh thành công.", jobTitleService.getDetail(jobTitleId), null, RequestTraceContext.getTraceId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('jobtitle.create')")
    public ApiResponse<JobTitleDetailResponse> create(@Valid @RequestBody CreateJobTitleRequest request) {
        return ApiResponse.success("JOB_TITLE_CREATE_SUCCESS", "Tạo chức danh thành công.", jobTitleService.create(request), null, RequestTraceContext.getTraceId());
    }

    @PutMapping("/{jobTitleId}")
    @PreAuthorize("hasAuthority('jobtitle.update')")
    public ApiResponse<JobTitleDetailResponse> update(@PathVariable Long jobTitleId, @Valid @RequestBody UpdateJobTitleRequest request) {
        return ApiResponse.success("JOB_TITLE_UPDATE_SUCCESS", "Cập nhật chức danh thành công.", jobTitleService.update(jobTitleId, request), null, RequestTraceContext.getTraceId());
    }

    @PatchMapping("/{jobTitleId}/status")
    @PreAuthorize("hasAuthority('jobtitle.change_status')")
    public ApiResponse<JobTitleDetailResponse> changeStatus(@PathVariable Long jobTitleId, @Valid @RequestBody UpdateJobTitleStatusRequest request) {
        return ApiResponse.success("JOB_TITLE_STATUS_SUCCESS", "Cập nhật trạng thái chức danh thành công.", jobTitleService.changeStatus(jobTitleId, request), null, RequestTraceContext.getTraceId());
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('orgunit.import_export')")
    public ResponseEntity<String> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=job-titles.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(jobTitleService.exportCsv());
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('orgunit.import_export')")
    public ApiResponse<ImportResultResponse> importRows(@Valid @RequestBody List<@Valid JobTitleImportRowRequest> requests) {
        return ApiResponse.success("JOB_TITLE_IMPORT_SUCCESS", "Import chức danh thành công.", jobTitleService.importRows(requests), null, RequestTraceContext.getTraceId());
    }
}
