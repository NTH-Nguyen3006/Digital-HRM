package com.company.hrm.module.jobtitle.service;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.jobtitle.dto.*;
import com.company.hrm.module.jobtitle.entity.HrJobTitle;
import com.company.hrm.module.jobtitle.repository.HrJobTitleRepository;
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

    private HrJobTitle getJobTitle(Long jobTitleId) {
        return jobTitleRepository.findByJobTitleIdAndDeletedFalse(jobTitleId)
                .orElseThrow(() -> new NotFoundException("JOB_TITLE_NOT_FOUND", "Không tìm thấy chức danh."));
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
