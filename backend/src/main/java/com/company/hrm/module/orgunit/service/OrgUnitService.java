package com.company.hrm.module.orgunit.service;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.dto.ImportResultResponse;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.employee.service.EmployeeAccessScopeService;
import com.company.hrm.module.orgunit.dto.*;
import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import com.company.hrm.module.orgunit.repository.HrOrgUnitRepository;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrgUnitService {

    private final HrOrgUnitRepository orgUnitRepository;
    private final HrEmployeeRepository employeeRepository;
    private final EmployeeAccessScopeService employeeAccessScopeService;
    private final AuditLogService auditLogService;

    public OrgUnitService(
            HrOrgUnitRepository orgUnitRepository,
            HrEmployeeRepository employeeRepository,
            EmployeeAccessScopeService employeeAccessScopeService,
            AuditLogService auditLogService
    ) {
        this.orgUnitRepository = orgUnitRepository;
        this.employeeRepository = employeeRepository;
        this.employeeAccessScopeService = employeeAccessScopeService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrgUnitListItemResponse> list(String keyword, RecordStatus status, int page, int size) {
        Specification<HrOrgUnit> specification = (root, query, builder) -> builder.isFalse(root.get("deleted"));
        specification = employeeAccessScopeService.applyOrgUnitReadScope(specification);
        if (keyword != null && !keyword.isBlank()) {
            String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("orgUnitCode")), likeKeyword),
                    builder.like(builder.lower(root.get("orgUnitName")), likeKeyword)
            ));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }
        Page<HrOrgUnit> result = orgUnitRepository.findAll(specification, PageRequest.of(page, size, Sort.by("hierarchyLevel", "sortOrder", "orgUnitName")));
        List<OrgUnitListItemResponse> items = result.getContent().stream().map(this::toListItem).toList();
        return new PageResponse<>(items, page, size, result.getTotalElements(), result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Transactional(readOnly = true)
    public List<OrgUnitTreeNodeResponse> getTree() {
        List<HrOrgUnit> orgUnits = orgUnitRepository.findAll(employeeAccessScopeService.applyOrgUnitReadScope((root, query, builder) -> builder.isFalse(root.get("deleted"))), Sort.by("hierarchyLevel", "sortOrder", "orgUnitName"));
        Map<Long, OrgUnitTreeNodeResponse> nodes = new LinkedHashMap<>();
        for (HrOrgUnit entity : orgUnits) {
            nodes.put(entity.getOrgUnitId(), new OrgUnitTreeNodeResponse(
                    entity.getOrgUnitId(),
                    entity.getParentOrgUnit() == null ? null : entity.getParentOrgUnit().getOrgUnitId(),
                    entity.getOrgUnitCode(),
                    entity.getOrgUnitName(),
                    entity.getOrgUnitType().name(),
                    entity.getHierarchyLevel(),
                    entity.getStatus().name(),
                    entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getEmployeeId(),
                    entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getFullName(),
                    new ArrayList<>()
            ));
        }
        List<OrgUnitTreeNodeResponse> roots = new ArrayList<>();
        for (OrgUnitTreeNodeResponse node : nodes.values()) {
            if (node.parentOrgUnitId() == null || !nodes.containsKey(node.parentOrgUnitId())) {
                roots.add(node);
            } else {
                nodes.get(node.parentOrgUnitId()).children().add(node);
            }
        }
        return roots;
    }

    @Transactional(readOnly = true)
    public OrgUnitDetailResponse getDetail(Long orgUnitId) {
        HrOrgUnit orgUnit = getOrgUnit(orgUnitId);
        employeeAccessScopeService.assertCanReadOrgUnit(orgUnit);
        return toDetail(orgUnit);
    }

    @Transactional
    public OrgUnitDetailResponse create(CreateOrgUnitRequest request) {
        validateEffectiveRange(request.effectiveFrom(), request.effectiveTo());
        if (orgUnitRepository.existsByOrgUnitCodeIgnoreCaseAndDeletedFalse(request.orgUnitCode())) {
            throw new BusinessException("ORG_UNIT_CODE_DUPLICATE", "orgUnitCode đã tồn tại.", HttpStatus.CONFLICT);
        }
        HrOrgUnit parent = request.parentOrgUnitId() == null ? null : getOrgUnit(request.parentOrgUnitId());
        HrEmployee manager = request.managerEmployeeId() == null ? null : getEmployee(request.managerEmployeeId());

        HrOrgUnit entity = new HrOrgUnit();
        entity.setParentOrgUnit(parent);
        entity.setOrgUnitCode(normalizeCode(request.orgUnitCode()));
        entity.setOrgUnitName(request.orgUnitName().trim());
        entity.setOrgUnitType(request.orgUnitType());
        entity.setManagerEmployee(manager);
        entity.setHierarchyLevel(parent == null ? 1 : parent.getHierarchyLevel() + 1);
        entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entity.setStatus(RecordStatus.ACTIVE);
        entity.setEffectiveFrom(request.effectiveFrom());
        entity.setEffectiveTo(request.effectiveTo());
        entity = orgUnitRepository.save(entity);
        entity.setPathCode(buildPathCode(parent, entity.getOrgUnitCode()));
        entity = orgUnitRepository.save(entity);

        OrgUnitDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("CREATE", "ORG_UNIT", "hr_org_unit", entity.getOrgUnitId().toString(), null, response, "Tạo mới đơn vị tổ chức.");
        return response;
    }

    @Transactional
    public OrgUnitDetailResponse update(Long orgUnitId, UpdateOrgUnitRequest request) {
        validateEffectiveRange(request.effectiveFrom(), request.effectiveTo());
        HrOrgUnit entity = getOrgUnit(orgUnitId);
        OrgUnitDetailResponse oldSnapshot = toDetail(entity);

        if (orgUnitRepository.existsByOrgUnitCodeIgnoreCaseAndDeletedFalseAndOrgUnitIdNot(request.orgUnitCode(), orgUnitId)) {
            throw new BusinessException("ORG_UNIT_CODE_DUPLICATE", "orgUnitCode đã tồn tại.", HttpStatus.CONFLICT);
        }

        HrOrgUnit newParent = request.parentOrgUnitId() == null ? null : getOrgUnit(request.parentOrgUnitId());
        if (newParent != null && (newParent.getOrgUnitId().equals(orgUnitId) || (newParent.getPathCode() != null && entity.getPathCode() != null && newParent.getPathCode().startsWith(entity.getPathCode())))) {
            throw new BusinessException("ORG_UNIT_PARENT_INVALID", "Không thể đặt đơn vị cha là chính nó hoặc một đơn vị con.", HttpStatus.BAD_REQUEST);
        }

        String oldPath = entity.getPathCode();
        entity.setParentOrgUnit(newParent);
        entity.setOrgUnitCode(normalizeCode(request.orgUnitCode()));
        entity.setOrgUnitName(request.orgUnitName().trim());
        entity.setOrgUnitType(request.orgUnitType());
        entity.setHierarchyLevel(newParent == null ? 1 : newParent.getHierarchyLevel() + 1);
        entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entity.setEffectiveFrom(request.effectiveFrom());
        entity.setEffectiveTo(request.effectiveTo());
        entity.setPathCode(buildPathCode(newParent, entity.getOrgUnitCode()));
        entity = orgUnitRepository.save(entity);
        if (oldPath != null && !oldPath.equals(entity.getPathCode())) {
            rebuildDescendantPaths(oldPath, entity.getPathCode());
        }

        OrgUnitDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("UPDATE", "ORG_UNIT", "hr_org_unit", entity.getOrgUnitId().toString(), oldSnapshot, response, "Cập nhật đơn vị tổ chức.");
        return response;
    }

    @Transactional
    public OrgUnitDetailResponse changeStatus(Long orgUnitId, UpdateOrgUnitStatusRequest request) {
        HrOrgUnit entity = getOrgUnit(orgUnitId);
        OrgUnitDetailResponse oldSnapshot = toDetail(entity);

        if (request.status() == RecordStatus.INACTIVE) {
            boolean hasActiveChildren = orgUnitRepository.count((root, query, builder) -> builder.and(
                    builder.isFalse(root.get("deleted")),
                    builder.equal(root.get("parentOrgUnit").get("orgUnitId"), orgUnitId),
                    builder.equal(root.get("status"), RecordStatus.ACTIVE)
            )) > 0;
            if (hasActiveChildren) {
                throw new BusinessException("ORG_UNIT_HAS_ACTIVE_CHILDREN", "Không thể ngừng sử dụng đơn vị đang có đơn vị con hoạt động.", HttpStatus.CONFLICT);
            }
            boolean hasActiveEmployees = employeeRepository.count((root, query, builder) -> builder.and(
                    builder.isFalse(root.get("deleted")),
                    builder.equal(root.get("orgUnit").get("orgUnitId"), orgUnitId),
                    builder.equal(root.get("employmentStatus"), com.company.hrm.common.constant.EmploymentStatus.ACTIVE)
            )) > 0;
            if (hasActiveEmployees) {
                throw new BusinessException("ORG_UNIT_HAS_ACTIVE_EMPLOYEES", "Không thể ngừng sử dụng đơn vị đang còn nhân sự hoạt động.", HttpStatus.CONFLICT);
            }
        }

        entity.setStatus(request.status());
        entity.setEffectiveTo(request.effectiveTo());
        entity = orgUnitRepository.save(entity);
        OrgUnitDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("CHANGE_STATUS", "ORG_UNIT", "hr_org_unit", entity.getOrgUnitId().toString(), oldSnapshot, response, request.note());
        return response;
    }

    @Transactional
    public OrgUnitDetailResponse assignManager(Long orgUnitId, UpdateOrgUnitManagerRequest request) {
        HrOrgUnit entity = getOrgUnit(orgUnitId);
        OrgUnitDetailResponse oldSnapshot = toDetail(entity);
        entity.setManagerEmployee(request.managerEmployeeId() == null ? null : getEmployee(request.managerEmployeeId()));
        entity = orgUnitRepository.save(entity);
        OrgUnitDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("ASSIGN_MANAGER", "ORG_UNIT", "hr_org_unit", entity.getOrgUnitId().toString(), oldSnapshot, response, request.note());
        return response;
    }


    @Transactional(readOnly = true)
    public String exportCsv() {
        List<OrgUnitExportRowResponse> rows = orgUnitRepository.findAllByDeletedFalseOrderByHierarchyLevelAscSortOrderAscOrgUnitNameAsc()
                .stream()
                .map(entity -> new OrgUnitExportRowResponse(
                        entity.getOrgUnitCode(),
                        entity.getParentOrgUnit() == null ? null : entity.getParentOrgUnit().getOrgUnitCode(),
                        entity.getOrgUnitName(),
                        entity.getOrgUnitType().name(),
                        entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getEmployeeId(),
                        entity.getSortOrder(),
                        entity.getStatus().name(),
                        entity.getEffectiveFrom(),
                        entity.getEffectiveTo()
                ))
                .toList();
        String header = "orgUnitCode,parentOrgUnitCode,orgUnitName,orgUnitType,managerEmployeeId,sortOrder,status,effectiveFrom,effectiveTo";
        return header + "\n" + rows.stream()
                .map(row -> String.join(",",
                        csv(row.orgUnitCode()), csv(row.parentOrgUnitCode()), csv(row.orgUnitName()), csv(row.orgUnitType()),
                        csv(row.managerEmployeeId()), csv(row.sortOrder()), csv(row.status()), csv(row.effectiveFrom()), csv(row.effectiveTo())))
                .collect(Collectors.joining("\n"));
    }

    @Transactional
    public ImportResultResponse importRows(List<OrgUnitImportRowRequest> requests) {
        int created = 0;
        int updated = 0;
        int skipped = 0;
        List<String> messages = new ArrayList<>();

        for (OrgUnitImportRowRequest request : requests) {
            try {
                validateEffectiveRange(request.effectiveFrom(), request.effectiveTo());
                HrOrgUnit parent = request.parentOrgUnitCode() == null || request.parentOrgUnitCode().isBlank()
                        ? null
                        : orgUnitRepository.findByOrgUnitCodeIgnoreCaseAndDeletedFalse(request.parentOrgUnitCode().trim())
                        .orElseThrow(() -> new BusinessException("ORG_UNIT_PARENT_NOT_FOUND", "parentOrgUnitCode không tồn tại: " + request.parentOrgUnitCode(), HttpStatus.BAD_REQUEST));
                HrEmployee manager = request.managerEmployeeId() == null ? null : getEmployee(request.managerEmployeeId());
                HrOrgUnit entity = orgUnitRepository.findByOrgUnitCodeIgnoreCaseAndDeletedFalse(request.orgUnitCode().trim()).orElse(null);
                boolean isCreate = entity == null;
                if (isCreate) {
                    entity = new HrOrgUnit();
                    entity.setOrgUnitCode(normalizeCode(request.orgUnitCode()));
                }
                entity.setParentOrgUnit(parent);
                entity.setOrgUnitName(request.orgUnitName().trim());
                entity.setOrgUnitType(request.orgUnitType());
                entity.setManagerEmployee(manager);
                entity.setHierarchyLevel(parent == null ? 1 : parent.getHierarchyLevel() + 1);
                entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
                entity.setStatus(request.status());
                entity.setEffectiveFrom(request.effectiveFrom());
                entity.setEffectiveTo(request.effectiveTo());
                entity = orgUnitRepository.save(entity);
                entity.setPathCode(buildPathCode(parent, entity.getOrgUnitCode()));
                orgUnitRepository.save(entity);
                if (isCreate) created++; else updated++;
            } catch (RuntimeException ex) {
                skipped++;
                messages.add(request.orgUnitCode() + ": " + ex.getMessage());
            }
        }
        auditLogService.logSuccess("IMPORT", "ORG_UNIT", "hr_org_unit", null, null, Map.of("totalRows", requests.size(), "created", created, "updated", updated, "skipped", skipped), "Import cơ cấu tổ chức.");
        return new ImportResultResponse(requests.size(), created, updated, skipped, messages);
    }

    private void rebuildDescendantPaths(String oldPrefix, String newPrefix) {
        List<HrOrgUnit> descendants = orgUnitRepository.findAll((root, query, builder) -> builder.and(
                builder.isFalse(root.get("deleted")),
                builder.like(root.get("pathCode"), oldPrefix + "%"),
                builder.notEqual(root.get("pathCode"), oldPrefix)
        ));
        for (HrOrgUnit descendant : descendants) {
            descendant.setPathCode(descendant.getPathCode().replaceFirst(java.util.regex.Pattern.quote(oldPrefix), newPrefix));
            descendant.setHierarchyLevel(calculateLevel(descendant.getPathCode()));
        }
        orgUnitRepository.saveAll(descendants);
    }

    private int calculateLevel(String pathCode) {
        if (pathCode == null || pathCode.isBlank()) {
            return 1;
        }
        return (int) Arrays.stream(pathCode.split("/")).filter(value -> !value.isBlank()).count();
    }

    private String buildPathCode(HrOrgUnit parent, String orgUnitCode) {
        String normalized = normalizeCode(orgUnitCode);
        return parent == null ? "/" + normalized + "/" : parent.getPathCode() + normalized + "/";
    }

    private String csv(Object value) {
        if (value == null) return "";
        String raw = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + raw + "\"";
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private void validateEffectiveRange(LocalDate from, LocalDate to) {
        if (to != null && to.isBefore(from)) {
            throw new BusinessException("ORG_UNIT_EFFECTIVE_RANGE_INVALID", "effectiveTo không được nhỏ hơn effectiveFrom.", HttpStatus.BAD_REQUEST);
        }
    }

    private HrOrgUnit getOrgUnit(Long orgUnitId) {
        return orgUnitRepository.findByOrgUnitIdAndDeletedFalse(orgUnitId)
                .orElseThrow(() -> new NotFoundException("ORG_UNIT_NOT_FOUND", "Không tìm thấy đơn vị tổ chức."));
    }

    private HrEmployee getEmployee(Long employeeId) {
        return employeeRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new NotFoundException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân sự."));
    }

    private OrgUnitListItemResponse toListItem(HrOrgUnit entity) {
        return new OrgUnitListItemResponse(
                entity.getOrgUnitId(),
                entity.getParentOrgUnit() == null ? null : entity.getParentOrgUnit().getOrgUnitId(),
                entity.getOrgUnitCode(),
                entity.getOrgUnitName(),
                entity.getOrgUnitType().name(),
                entity.getHierarchyLevel(),
                entity.getPathCode(),
                entity.getSortOrder(),
                entity.getStatus().name(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getEmployeeId(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getEmployeeCode(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getFullName()
        );
    }

    private OrgUnitDetailResponse toDetail(HrOrgUnit entity) {
        return new OrgUnitDetailResponse(
                entity.getOrgUnitId(),
                entity.getParentOrgUnit() == null ? null : entity.getParentOrgUnit().getOrgUnitId(),
                entity.getParentOrgUnit() == null ? null : entity.getParentOrgUnit().getOrgUnitCode(),
                entity.getParentOrgUnit() == null ? null : entity.getParentOrgUnit().getOrgUnitName(),
                entity.getOrgUnitCode(),
                entity.getOrgUnitName(),
                entity.getOrgUnitType().name(),
                entity.getHierarchyLevel(),
                entity.getPathCode(),
                entity.getSortOrder(),
                entity.getStatus().name(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getEmployeeId(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getEmployeeCode(),
                entity.getManagerEmployee() == null ? null : entity.getManagerEmployee().getFullName()
        );
    }
}
