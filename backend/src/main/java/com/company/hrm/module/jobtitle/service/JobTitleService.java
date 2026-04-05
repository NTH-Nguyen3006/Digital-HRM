package com.company.hrm.module.jobtitle.service;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.dto.ImportResultResponse;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.jobtitle.dto.*;
import com.company.hrm.module.jobtitle.entity.HrJobTitle;
import com.company.hrm.module.jobtitle.repository.HrJobTitleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobTitleService {

    private final HrJobTitleRepository jobTitleRepository;
    private final HrEmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    public JobTitleService(HrJobTitleRepository jobTitleRepository, HrEmployeeRepository employeeRepository, AuditLogService auditLogService) {
        this.jobTitleRepository = jobTitleRepository;
        this.employeeRepository = employeeRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<JobTitleListItemResponse> list(String keyword, RecordStatus status, int page, int size) {
        Specification<HrJobTitle> specification = (root, query, builder) -> builder.isFalse(root.get("deleted"));
        if (keyword != null && !keyword.isBlank()) {
            String likeValue = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("jobTitleCode")), likeValue),
                    builder.like(builder.lower(root.get("jobTitleName")), likeValue)
            ));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }
        Page<HrJobTitle> result = jobTitleRepository.findAll(specification, PageRequest.of(page, size, Sort.by("sortOrder", "jobTitleName")));
        return new PageResponse<>(result.getContent().stream().map(this::toListItem).toList(), page, size, result.getTotalElements(), result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Transactional(readOnly = true)
    public JobTitleDetailResponse getDetail(Long jobTitleId) {
        return toDetail(getJobTitle(jobTitleId));
    }

    @Transactional
    public JobTitleDetailResponse create(CreateJobTitleRequest request) {
        if (jobTitleRepository.existsByJobTitleCodeIgnoreCaseAndDeletedFalse(request.jobTitleCode())) {
            throw new BusinessException("JOB_TITLE_CODE_DUPLICATE", "jobTitleCode đã tồn tại.", HttpStatus.CONFLICT);
        }
        HrJobTitle entity = new HrJobTitle();
        entity.setJobTitleCode(normalizeCode(request.jobTitleCode()));
        entity.setJobTitleName(request.jobTitleName().trim());
        entity.setJobLevelCode(blankToNull(request.jobLevelCode()));
        entity.setDescription(blankToNull(request.description()));
        entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entity.setStatus(RecordStatus.ACTIVE);
        entity = jobTitleRepository.save(entity);
        JobTitleDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("CREATE", "JOB_TITLE", "hr_job_title", entity.getJobTitleId().toString(), null, response, "Tạo mới chức danh.");
        return response;
    }

    @Transactional
    public JobTitleDetailResponse update(Long jobTitleId, UpdateJobTitleRequest request) {
        HrJobTitle entity = getJobTitle(jobTitleId);
        JobTitleDetailResponse oldSnapshot = toDetail(entity);
        if (jobTitleRepository.existsByJobTitleCodeIgnoreCaseAndDeletedFalseAndJobTitleIdNot(request.jobTitleCode(), jobTitleId)) {
            throw new BusinessException("JOB_TITLE_CODE_DUPLICATE", "jobTitleCode đã tồn tại.", HttpStatus.CONFLICT);
        }
        entity.setJobTitleCode(normalizeCode(request.jobTitleCode()));
        entity.setJobTitleName(request.jobTitleName().trim());
        entity.setJobLevelCode(blankToNull(request.jobLevelCode()));
        entity.setDescription(blankToNull(request.description()));
        entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entity = jobTitleRepository.save(entity);
        JobTitleDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("UPDATE", "JOB_TITLE", "hr_job_title", entity.getJobTitleId().toString(), oldSnapshot, response, "Cập nhật chức danh.");
        return response;
    }

    @Transactional
    public JobTitleDetailResponse changeStatus(Long jobTitleId, UpdateJobTitleStatusRequest request) {
        HrJobTitle entity = getJobTitle(jobTitleId);
        JobTitleDetailResponse oldSnapshot = toDetail(entity);
        if (request.status() == RecordStatus.INACTIVE) {
            boolean usedByActiveEmployee = employeeRepository.count((root, query, builder) -> builder.and(
                    builder.isFalse(root.get("deleted")),
                    builder.equal(root.get("jobTitle").get("jobTitleId"), jobTitleId),
                    builder.notEqual(root.get("employmentStatus"), com.company.hrm.common.constant.EmploymentStatus.TERMINATED),
                    builder.notEqual(root.get("employmentStatus"), com.company.hrm.common.constant.EmploymentStatus.RESIGNED)
            )) > 0;
            if (usedByActiveEmployee) {
                throw new BusinessException("JOB_TITLE_IN_USE", "Không thể ngừng sử dụng chức danh đang có nhân sự sử dụng.", HttpStatus.CONFLICT);
            }
        }
        entity.setStatus(request.status());
        entity = jobTitleRepository.save(entity);
        JobTitleDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("CHANGE_STATUS", "JOB_TITLE", "hr_job_title", entity.getJobTitleId().toString(), oldSnapshot, response, request.note());
        return response;
    }


    @Transactional(readOnly = true)
    public String exportCsv() {
        List<JobTitleExportRowResponse> rows = jobTitleRepository.findAll((root, query, builder) -> builder.isFalse(root.get("deleted")), Sort.by("sortOrder", "jobTitleName"))
                .stream()
                .map(entity -> new JobTitleExportRowResponse(entity.getJobTitleCode(), entity.getJobTitleName(), entity.getJobLevelCode(), entity.getDescription(), entity.getSortOrder(), entity.getStatus().name()))
                .toList();
        String header = "jobTitleCode,jobTitleName,jobLevelCode,description,sortOrder,status";
        return header + "\n" + rows.stream().map(row -> String.join(",", csv(row.jobTitleCode()), csv(row.jobTitleName()), csv(row.jobLevelCode()), csv(row.description()), csv(row.sortOrder()), csv(row.status()))).collect(Collectors.joining("\n"));
    }

    @Transactional
    public ImportResultResponse importRows(List<JobTitleImportRowRequest> requests) {
        int created = 0;
        int updated = 0;
        int skipped = 0;
        List<String> messages = new ArrayList<>();
        for (JobTitleImportRowRequest request : requests) {
            try {
                HrJobTitle entity = jobTitleRepository.findByJobTitleCodeIgnoreCaseAndDeletedFalse(request.jobTitleCode().trim()).orElse(null);
                boolean isCreate = entity == null;
                if (isCreate) {
                    entity = new HrJobTitle();
                    entity.setJobTitleCode(normalizeCode(request.jobTitleCode()));
                }
                entity.setJobTitleName(request.jobTitleName().trim());
                entity.setJobLevelCode(blankToNull(request.jobLevelCode()));
                entity.setDescription(blankToNull(request.description()));
                entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
                entity.setStatus(request.status());
                jobTitleRepository.save(entity);
                if (isCreate) created++; else updated++;
            } catch (RuntimeException ex) {
                skipped++;
                messages.add(request.jobTitleCode() + ": " + ex.getMessage());
            }
        }
        auditLogService.logSuccess("IMPORT", "JOB_TITLE", "hr_job_title", null, null, Map.of("totalRows", requests.size(), "created", created, "updated", updated, "skipped", skipped), "Import chức danh.");
        return new ImportResultResponse(requests.size(), created, updated, skipped, messages);
    }

    private HrJobTitle getJobTitle(Long jobTitleId) {
        return jobTitleRepository.findByJobTitleIdAndDeletedFalse(jobTitleId)
                .orElseThrow(() -> new NotFoundException("JOB_TITLE_NOT_FOUND", "Không tìm thấy chức danh."));
    }

    private String csv(Object value) {
        if (value == null) return "";
        String raw = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + raw + "\"";
    }

    private String normalizeCode(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private JobTitleListItemResponse toListItem(HrJobTitle entity) {
        return new JobTitleListItemResponse(entity.getJobTitleId(), entity.getJobTitleCode(), entity.getJobTitleName(), entity.getJobLevelCode(), entity.getDescription(), entity.getSortOrder(), entity.getStatus().name());
    }

    private JobTitleDetailResponse toDetail(HrJobTitle entity) {
        return new JobTitleDetailResponse(entity.getJobTitleId(), entity.getJobTitleCode(), entity.getJobTitleName(), entity.getJobLevelCode(), entity.getDescription(), entity.getSortOrder(), entity.getStatus().name());
    }
}
