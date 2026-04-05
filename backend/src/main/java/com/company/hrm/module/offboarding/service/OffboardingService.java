package com.company.hrm.module.offboarding.service;

import com.company.hrm.common.constant.*;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.ForbiddenException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.attendance.entity.AttAttendancePeriod;
import com.company.hrm.module.attendance.repository.AttAttendancePeriodRepository;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.contract.entity.CtLaborContract;
import com.company.hrm.module.contract.repository.CtLaborContractRepository;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.leave.entity.LeaLeaveBalance;
import com.company.hrm.module.leave.repository.LeaLeaveBalanceRepository;
import com.company.hrm.module.offboarding.dto.*;
import com.company.hrm.module.offboarding.entity.*;
import com.company.hrm.module.offboarding.repository.*;
import com.company.hrm.module.payroll.dto.GeneratePayrollDraftRequest;
import com.company.hrm.module.payroll.dto.PayrollPeriodCreateRequest;
import com.company.hrm.module.payroll.entity.PayEmployeeCompensation;
import com.company.hrm.module.payroll.entity.PayPayrollItem;
import com.company.hrm.module.payroll.entity.PayPayrollPeriod;
import com.company.hrm.module.payroll.repository.PayEmployeeCompensationRepository;
import com.company.hrm.module.payroll.repository.PayPayrollItemRepository;
import com.company.hrm.module.payroll.repository.PayPayrollPeriodRepository;
import com.company.hrm.module.payroll.service.PayrollService;
import com.company.hrm.module.portal.entity.PorPortalInboxItem;
import com.company.hrm.module.portal.repository.PorPortalInboxItemRepository;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.module.auth.entity.SecAuthSession;
import com.company.hrm.module.auth.repository.SecAuthSessionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OffboardingService {

    private static final int SCALE_MONEY = 2;
    private static final BigDecimal STANDARD_WORKING_DAYS = BigDecimal.valueOf(26);

    private final OffOffboardingCaseRepository offboardingCaseRepository;
    private final OffOffboardingChecklistItemRepository checklistItemRepository;
    private final OffOffboardingAssetReturnRepository assetReturnRepository;
    private final OffOffboardingHistoryRepository historyRepository;
    private final HrEmployeeRepository employeeRepository;
    private final SecUserAccountRepository userAccountRepository;
    private final SecAuthSessionRepository authSessionRepository;
    private final OffboardingAccessScopeService accessScopeService;
    private final AuditLogService auditLogService;
    private final AttAttendancePeriodRepository attendancePeriodRepository;
    private final LeaLeaveBalanceRepository leaveBalanceRepository;
    private final CtLaborContractRepository laborContractRepository;
    private final PayEmployeeCompensationRepository compensationRepository;
    private final PayPayrollPeriodRepository payrollPeriodRepository;
    private final PayPayrollItemRepository payrollItemRepository;
    private final PayrollService payrollService;
    private final PorPortalInboxItemRepository portalInboxItemRepository;

    public OffboardingService(
            OffOffboardingCaseRepository offboardingCaseRepository,
            OffOffboardingChecklistItemRepository checklistItemRepository,
            OffOffboardingAssetReturnRepository assetReturnRepository,
            OffOffboardingHistoryRepository historyRepository,
            HrEmployeeRepository employeeRepository,
            SecUserAccountRepository userAccountRepository,
            SecAuthSessionRepository authSessionRepository,
            OffboardingAccessScopeService accessScopeService,
            AuditLogService auditLogService,
            AttAttendancePeriodRepository attendancePeriodRepository,
            LeaLeaveBalanceRepository leaveBalanceRepository,
            CtLaborContractRepository laborContractRepository,
            PayEmployeeCompensationRepository compensationRepository,
            PayPayrollPeriodRepository payrollPeriodRepository,
            PayPayrollItemRepository payrollItemRepository,
            PayrollService payrollService,
            PorPortalInboxItemRepository portalInboxItemRepository
    ) {
        this.offboardingCaseRepository = offboardingCaseRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.assetReturnRepository = assetReturnRepository;
        this.historyRepository = historyRepository;
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
        this.authSessionRepository = authSessionRepository;
        this.accessScopeService = accessScopeService;
        this.auditLogService = auditLogService;
        this.attendancePeriodRepository = attendancePeriodRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.laborContractRepository = laborContractRepository;
        this.compensationRepository = compensationRepository;
        this.payrollPeriodRepository = payrollPeriodRepository;
        this.payrollItemRepository = payrollItemRepository;
        this.payrollService = payrollService;
        this.portalInboxItemRepository = portalInboxItemRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<OffboardingListItemResponse> listCases(String keyword, OffboardingStatus status, Long orgUnitId, int page, int size) {
        Specification<OffOffboardingCase> specification = (root, query, builder) -> builder.isFalse(root.get("deleted"));
        if (keyword != null && !keyword.isBlank()) {
            String likeValue = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("offboardingCode")), likeValue),
                    builder.like(builder.lower(root.join("employee").get("employeeCode")), likeValue),
                    builder.like(builder.lower(root.join("employee").get("fullName")), likeValue)
            ));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }
        if (orgUnitId != null) {
            HrEmployee any = employeeRepository.findAll((r, q, b) -> b.and(
                    b.isFalse(r.get("deleted")),
                    b.equal(r.join("orgUnit").get("orgUnitId"), orgUnitId)
            ), PageRequest.of(0, 1)).stream().findFirst().orElse(null);
            if (any == null || any.getOrgUnit() == null) {
                return new PageResponse<>(List.of(), page, size, 0, 0, false, false);
            }
            String prefix = any.getOrgUnit().getPathCode();
            specification = specification.and((root, query, builder) ->
                    builder.like(root.join("employee").join("orgUnit").get("pathCode"), prefix + "%"));
        }
        Page<OffOffboardingCase> result = offboardingCaseRepository.findAll(specification,
                PageRequest.of(page, size, Sort.by("createdAt").descending().and(Sort.by("offboardingCaseId").descending())));
        List<OffboardingListItemResponse> items = result.getContent().stream().map(this::toListItem).toList();
        return new PageResponse<>(items, page, size, result.getTotalElements(), result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Transactional(readOnly = true)
    public List<OffboardingListItemResponse> listMyRequests() {
        HrEmployee currentEmployee = accessScopeService.getCurrentEmployeeRequired();
        return offboardingCaseRepository.findAllByEmployeeEmployeeIdAndDeletedFalseOrderByCreatedAtDescOffboardingCaseIdDesc(currentEmployee.getEmployeeId())
                .stream()
                .map(this::toListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OffboardingListItemResponse> listManagerPendingCases() {
        String prefix = accessScopeService.getManagerOrgPathPrefix()
                .orElseThrow(() -> new ForbiddenException("MANAGER_SCOPE_REQUIRED", "Tài khoản hiện tại không thuộc phạm vi quản lý."));
        return offboardingCaseRepository.findAll((root, query, builder) -> builder.and(
                        builder.isFalse(root.get("deleted")),
                        builder.equal(root.get("status"), OffboardingStatus.REQUESTED),
                        builder.like(root.join("employee").join("orgUnit").get("pathCode"), prefix + "%")
                ), Sort.by("requestDate").ascending().and(Sort.by("offboardingCaseId").ascending()))
                .stream().map(this::toListItem).toList();
    }

    @Transactional(readOnly = true)
    public OffboardingDetailResponse getDetail(Long offboardingCaseId) {
        OffOffboardingCase entity = getCase(offboardingCaseId);
        return toDetail(entity);
    }

    @Transactional
    public OffboardingDetailResponse createMyRequest(CreateOffboardingRequest request) {
        HrEmployee currentEmployee = accessScopeService.getCurrentEmployeeRequired();
        ensureNoOpenCase(currentEmployee.getEmployeeId());

        OffOffboardingCase entity = new OffOffboardingCase();
        entity.setOffboardingCode(generateCode());
        entity.setEmployee(currentEmployee);
        entity.setRequestedByUser(accessScopeService.getCurrentUserRequired());
        entity.setRequestDate(LocalDate.now());
        entity.setRequestedLastWorkingDate(request.requestedLastWorkingDate());
        entity.setRequestReason(request.requestReason().trim());
        entity.setStatus(OffboardingStatus.REQUESTED);
        entity = offboardingCaseRepository.save(entity);

        appendHistory(entity, null, OffboardingStatus.REQUESTED, "REQUEST_SUBMIT", request.requestReason());
        createPortalItemForEmployee(currentEmployee, PortalInboxItemType.NOTIFICATION, "OFFBOARDING",
                "Đã gửi yêu cầu nghỉ việc", "Yêu cầu nghỉ việc của bạn đã được gửi và đang chờ quản lý xử lý.",
                "OFFBOARDING", entity.getOffboardingCaseId().toString(), null);

        OffboardingDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("CREATE", "OFFBOARDING_CASE", "off_offboarding_case",
                entity.getOffboardingCaseId().toString(), null, response, "Tạo yêu cầu nghỉ việc.");
        return response;
    }

    @Transactional
    public OffboardingDetailResponse reviewByManager(Long offboardingCaseId, ReviewOffboardingRequest request) {
        OffOffboardingCase entity = getCase(offboardingCaseId);
        accessScopeService.assertManagerCanAccessEmployee(entity.getEmployee());
        if (entity.getStatus() != OffboardingStatus.REQUESTED) {
            throw new BusinessException("OFFBOARDING_REVIEW_STATUS_INVALID", "Chỉ xử lý yêu cầu nghỉ việc ở trạng thái chờ duyệt.", HttpStatus.CONFLICT);
        }
        OffboardingDetailResponse oldSnapshot = toDetail(entity);
        OffboardingStatus nextStatus = request.approved() ? OffboardingStatus.MANAGER_APPROVED : OffboardingStatus.MANAGER_REJECTED;
        entity.setStatus(nextStatus);
        entity.setManagerReviewedBy(accessScopeService.getCurrentUserRequired());
        entity.setManagerReviewedAt(LocalDateTime.now());
        entity.setManagerReviewNote(request.note().trim());
        entity = offboardingCaseRepository.save(entity);

        appendHistory(entity, OffboardingStatus.REQUESTED, nextStatus, request.approved() ? "MANAGER_APPROVE" : "MANAGER_REJECT", request.note());
        createPortalItemForEmployee(entity.getEmployee(), PortalInboxItemType.NOTIFICATION, "OFFBOARDING",
                request.approved() ? "Đề nghị nghỉ việc đã được quản lý duyệt" : "Đề nghị nghỉ việc đã bị từ chối",
                request.approved() ? "Yêu cầu nghỉ việc của bạn đã được quản lý duyệt và chuyển sang HR."
                        : "Yêu cầu nghỉ việc của bạn đã bị quản lý từ chối. Vui lòng xem lại nội dung trao đổi.",
                "OFFBOARDING", entity.getOffboardingCaseId().toString(), null);

        OffboardingDetailResponse response = toDetail(entity);
        auditLogService.logSuccess(request.approved() ? "MANAGER_APPROVE" : "MANAGER_REJECT", "OFFBOARDING_CASE", "off_offboarding_case",
                entity.getOffboardingCaseId().toString(), oldSnapshot, response, request.note());
        return response;
    }

    @Transactional
    public OffboardingDetailResponse finalizeByHr(Long offboardingCaseId, FinalizeOffboardingRequest request) {
        OffOffboardingCase entity = getCase(offboardingCaseId);
        if (entity.getStatus() != OffboardingStatus.MANAGER_APPROVED) {
            throw new BusinessException("OFFBOARDING_FINALIZE_STATUS_INVALID", "HR chỉ được chốt offboarding sau khi quản lý đã duyệt.", HttpStatus.CONFLICT);
        }
        OffboardingDetailResponse oldSnapshot = toDetail(entity);
        entity.setStatus(OffboardingStatus.HR_FINALIZED);
        entity.setEffectiveLastWorkingDate(request.effectiveLastWorkingDate());
        entity.setHrFinalizeNote(trimToNull(request.note()));
        entity.setHrFinalizedBy(accessScopeService.getCurrentUserRequired());
        entity.setHrFinalizedAt(LocalDateTime.now());
        entity = offboardingCaseRepository.save(entity);

        appendHistory(entity, OffboardingStatus.MANAGER_APPROVED, OffboardingStatus.HR_FINALIZED, "HR_FINALIZE", request.note());
        createPortalItemForEmployee(entity.getEmployee(), PortalInboxItemType.NOTIFICATION, "OFFBOARDING",
                "HR đã chốt ngày nghỉ việc",
                "HR đã chốt ngày hiệu lực nghỉ việc của bạn. Vui lòng hoàn tất bàn giao và theo dõi các task liên quan.",
                "OFFBOARDING", entity.getOffboardingCaseId().toString(), null);

        OffboardingDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("HR_FINALIZE", "OFFBOARDING_CASE", "off_offboarding_case",
                entity.getOffboardingCaseId().toString(), oldSnapshot, response, request.note());
        return response;
    }

    @Transactional
    public OffboardingChecklistItemResponse createChecklistItem(Long offboardingCaseId, UpsertOffboardingChecklistItemRequest request) {
        OffOffboardingCase entity = getCase(offboardingCaseId);
        accessScopeService.assertManagerCanAccessEmployee(entity.getEmployee());
        if (entity.getStatus().ordinal() < OffboardingStatus.HR_FINALIZED.ordinal()) {
            throw new BusinessException("OFFBOARDING_CHECKLIST_STATUS_INVALID", "Chỉ được thao tác checklist sau khi HR đã chốt offboarding.", HttpStatus.CONFLICT);
        }
        OffOffboardingChecklistItem item = new OffOffboardingChecklistItem();
        item.setOffboardingCase(entity);
        applyChecklist(item, request);
        item = checklistItemRepository.save(item);

        if (item.getOwnerRoleCode() == null || RoleCode.EMPLOYEE.name().equalsIgnoreCase(item.getOwnerRoleCode())) {
            createPortalItemForEmployee(entity.getEmployee(), PortalInboxItemType.TASK, "OFFBOARDING_CHECKLIST",
                    "Có task bàn giao mới", item.getItemName(), "OFFBOARDING", entity.getOffboardingCaseId().toString(),
                    item.getDueDate() == null ? null : item.getDueDate().atTime(17, 0));
        }
        auditLogService.logSuccess("CREATE_CHECKLIST", "OFFBOARDING_CHECKLIST", "off_offboarding_checklist_item",
                item.getOffboardingChecklistItemId().toString(), null, toChecklistResponse(item), "Tạo checklist offboarding.");
        return toChecklistResponse(item);
    }

    @Transactional
    public OffboardingChecklistItemResponse updateChecklistItem(Long checklistItemId, UpsertOffboardingChecklistItemRequest request) {
        OffOffboardingChecklistItem item = checklistItemRepository.findByOffboardingChecklistItemIdAndDeletedFalse(checklistItemId)
                .orElseThrow(() -> new NotFoundException("OFFBOARDING_CHECKLIST_NOT_FOUND", "Không tìm thấy checklist offboarding."));
        accessScopeService.assertManagerCanAccessEmployee(item.getOffboardingCase().getEmployee());
        OffboardingChecklistItemResponse oldSnapshot = toChecklistResponse(item);
        applyChecklist(item, request);
        if (item.getStatus() == OffboardingChecklistStatus.COMPLETED) {
            item.setCompletedAt(LocalDateTime.now());
            item.setCompletedBy(accessScopeService.getCurrentUserRequired());
        } else {
            item.setCompletedAt(null);
            item.setCompletedBy(null);
        }
        item = checklistItemRepository.save(item);
        OffboardingChecklistItemResponse response = toChecklistResponse(item);
        auditLogService.logSuccess("UPDATE_CHECKLIST", "OFFBOARDING_CHECKLIST", "off_offboarding_checklist_item",
                item.getOffboardingChecklistItemId().toString(), oldSnapshot, response, "Cập nhật checklist offboarding.");
        return response;
    }

    @Transactional
    public OffboardingAssetReturnResponse createAssetReturn(Long offboardingCaseId, UpsertOffboardingAssetReturnRequest request) {
        OffOffboardingCase entity = getCase(offboardingCaseId);
        OffOffboardingAssetReturn item = new OffOffboardingAssetReturn();
        item.setOffboardingCase(entity);
        applyAssetReturn(item, request);
        item = assetReturnRepository.save(item);
        auditLogService.logSuccess("CREATE_ASSET_RETURN", "OFFBOARDING_ASSET", "off_offboarding_asset_return",
                item.getOffboardingAssetReturnId().toString(), null, toAssetResponse(item), "Tạo dòng thu hồi tài sản.");
        return toAssetResponse(item);
    }

    @Transactional
    public OffboardingAssetReturnResponse updateAssetReturn(Long assetReturnId, UpsertOffboardingAssetReturnRequest request) {
        OffOffboardingAssetReturn item = assetReturnRepository.findByOffboardingAssetReturnIdAndDeletedFalse(assetReturnId)
                .orElseThrow(() -> new NotFoundException("OFFBOARDING_ASSET_NOT_FOUND", "Không tìm thấy dòng thu hồi tài sản."));
        OffboardingAssetReturnResponse oldSnapshot = toAssetResponse(item);
        applyAssetReturn(item, request);
        item = assetReturnRepository.save(item);
        OffboardingAssetReturnResponse response = toAssetResponse(item);
        auditLogService.logSuccess("UPDATE_ASSET_RETURN", "OFFBOARDING_ASSET", "off_offboarding_asset_return",
                item.getOffboardingAssetReturnId().toString(), oldSnapshot, response, "Cập nhật dòng thu hồi tài sản.");
        return response;
    }

    @Transactional
    public OffboardingDetailResponse revokeAccess(Long offboardingCaseId, RevokeOffboardingAccessRequest request) {
        OffOffboardingCase entity = getCase(offboardingCaseId);
        if (!(entity.getStatus() == OffboardingStatus.HR_FINALIZED || entity.getStatus() == OffboardingStatus.SETTLEMENT_PREPARED)) {
            throw new BusinessException("OFFBOARDING_REVOKE_ACCESS_STATUS_INVALID", "Chỉ được thu hồi quyền truy cập sau khi HR đã chốt offboarding.", HttpStatus.CONFLICT);
        }
        OffboardingDetailResponse oldSnapshot = toDetail(entity);
        Optional<SecUserAccount> userOpt = userAccountRepository.findByEmployeeIdAndDeletedFalse(entity.getEmployee().getEmployeeId());
        if (userOpt.isPresent()) {
            SecUserAccount user = userOpt.get();
            user.setStatus(UserStatus.DISABLED);
            userAccountRepository.save(user);

            List<SecAuthSession> sessions = authSessionRepository.findAllByUserUserIdAndStatusIn(
                    user.getUserId(), List.of(SessionStatus.ACTIVE)
            );
            LocalDateTime now = LocalDateTime.now();
            for (SecAuthSession session : sessions) {
                session.setStatus(SessionStatus.REVOKED);
                session.setRevokedAt(now);
                session.setRevokeReason("OFFBOARDING_ACCESS_REVOKED");
            }
            authSessionRepository.saveAll(sessions);
        }

        entity.setStatus(OffboardingStatus.ACCESS_REVOKED);
        entity.setAccessRevokedBy(accessScopeService.getCurrentUserRequired());
        entity.setAccessRevokedAt(LocalDateTime.now());
        entity.setAccessRevokeNote(request.note().trim());
        entity = offboardingCaseRepository.save(entity);
        appendHistory(entity, oldSnapshot.status() == null ? null : OffboardingStatus.valueOf(oldSnapshot.status()),
                OffboardingStatus.ACCESS_REVOKED, "REVOKE_ACCESS", request.note());

        createPortalItemForEmployee(entity.getEmployee(), PortalInboxItemType.NOTIFICATION, "OFFBOARDING",
                "Quyền truy cập HRM đã được thu hồi",
                "Tài khoản HRM của bạn đã được thu hồi theo quy trình offboarding.",
                "OFFBOARDING", entity.getOffboardingCaseId().toString(), null);

        OffboardingDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("REVOKE_ACCESS", "OFFBOARDING_CASE", "off_offboarding_case",
                entity.getOffboardingCaseId().toString(), oldSnapshot, response, request.note());
        return response;
    }

    @Transactional
    public OffboardingDetailResponse prepareSettlement(Long offboardingCaseId, PrepareOffboardingSettlementRequest request) {
        OffOffboardingCase entity = getCase(offboardingCaseId);
        if (!(entity.getStatus() == OffboardingStatus.HR_FINALIZED || entity.getStatus() == OffboardingStatus.ACCESS_REVOKED)) {
            throw new BusinessException("OFFBOARDING_SETTLEMENT_STATUS_INVALID", "Chỉ được chuẩn bị settlement sau khi offboarding đã được chốt.", HttpStatus.CONFLICT);
        }
        if (entity.getEffectiveLastWorkingDate() == null) {
            throw new BusinessException("OFFBOARDING_EFFECTIVE_DATE_REQUIRED", "Offboarding chưa có ngày hiệu lực nghỉ việc.", HttpStatus.CONFLICT);
        }

        LocalDate date = entity.getEffectiveLastWorkingDate();
        AttAttendancePeriod attendancePeriod = attendancePeriodRepository.findByPeriodYearAndPeriodMonthAndDeletedFalse(date.getYear(), date.getMonthValue())
                .orElseThrow(() -> new BusinessException("OFFBOARDING_ATTENDANCE_PERIOD_REQUIRED", "Chưa có kỳ công tương ứng để chốt offboarding.", HttpStatus.CONFLICT));
        if (attendancePeriod.getPeriodStatus() != AttendancePeriodStatus.CLOSED) {
            throw new BusinessException("OFFBOARDING_ATTENDANCE_PERIOD_NOT_CLOSED", "Kỳ công tương ứng chưa được chốt.", HttpStatus.CONFLICT);
        }

        List<LeaLeaveBalance> balances = leaveBalanceRepository.findAllByEmployeeEmployeeIdAndLeaveYearAndDeletedFalse(
                entity.getEmployee().getEmployeeId(), date.getYear()
        );
        BigDecimal remainingLeaveUnits = balances.stream()
                .map(LeaLeaveBalance::getAvailableUnits)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .max(BigDecimal.ZERO)
                .setScale(SCALE_MONEY, RoundingMode.HALF_UP);

        BigDecimal leaveAmount = calculateLeaveSettlementAmount(entity.getEmployee().getEmployeeId(), date, remainingLeaveUnits);

        PayPayrollPeriod payrollPeriod = payrollPeriodRepository.findByPeriodYearAndPeriodMonthAndDeletedFalse(date.getYear(), date.getMonthValue())
                .orElse(null);
        if (payrollPeriod == null && request.createPayrollDraftIfMissing()) {
            payrollService.createPayrollPeriod(new PayrollPeriodCreateRequest(date.getYear(), date.getMonthValue(), "Kỳ lương tạo từ offboarding settlement"));
            payrollPeriod = payrollPeriodRepository.findByPeriodYearAndPeriodMonthAndDeletedFalse(date.getYear(), date.getMonthValue()).orElse(null);
        }
        if (payrollPeriod == null) {
            throw new BusinessException("OFFBOARDING_PAYROLL_PERIOD_REQUIRED", "Chưa có kỳ lương tương ứng cho tháng nghỉ việc.", HttpStatus.CONFLICT);
        }

        PayPayrollItem payrollItem = payrollItemRepository.findByPayrollPeriodPayrollPeriodIdAndEmployeeEmployeeIdAndDeletedFalse(
                payrollPeriod.getPayrollPeriodId(), entity.getEmployee().getEmployeeId()
        ).orElse(null);
        if (payrollItem == null && request.createPayrollDraftIfMissing()) {
            payrollService.generatePayrollDraft(payrollPeriod.getPayrollPeriodId(), new GeneratePayrollDraftRequest(false, "Tạo bảng lương để phục vụ offboarding settlement"));
            payrollItem = payrollItemRepository.findByPayrollPeriodPayrollPeriodIdAndEmployeeEmployeeIdAndDeletedFalse(
                    payrollPeriod.getPayrollPeriodId(), entity.getEmployee().getEmployeeId()
            ).orElse(null);
        }
        if (payrollItem == null) {
            throw new BusinessException("OFFBOARDING_PAYROLL_ITEM_REQUIRED", "Chưa tìm thấy dòng lương cuối cùng của nhân sự trong kỳ lương tương ứng.", HttpStatus.CONFLICT);
        }

        OffboardingDetailResponse oldSnapshot = toDetail(entity);
        entity.setStatus(OffboardingStatus.SETTLEMENT_PREPARED);
        entity.setFinalAttendanceYear(date.getYear());
        entity.setFinalAttendanceMonth(date.getMonthValue());
        entity.setLeaveSettlementUnits(remainingLeaveUnits);
        entity.setLeaveSettlementAmount(leaveAmount);
        entity.setFinalPayrollPeriod(payrollPeriod);
        entity.setFinalPayrollItem(payrollItem);
        entity.setSettlementPreparedBy(accessScopeService.getCurrentUserRequired());
        entity.setSettlementPreparedAt(LocalDateTime.now());
        entity.setSettlementNote(request.note().trim());
        entity = offboardingCaseRepository.save(entity);
        appendHistory(entity, oldSnapshot.status() == null ? null : OffboardingStatus.valueOf(oldSnapshot.status()),
                OffboardingStatus.SETTLEMENT_PREPARED, "PREPARE_SETTLEMENT", request.note());

        createPortalItemForEmployee(entity.getEmployee(), PortalInboxItemType.NOTIFICATION, "OFFBOARDING",
                "Settlement offboarding đã được chuẩn bị",
                "HR đã chuẩn bị settlement cuối cùng cho hồ sơ offboarding của bạn.",
                "OFFBOARDING", entity.getOffboardingCaseId().toString(), null);

        OffboardingDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("PREPARE_SETTLEMENT", "OFFBOARDING_CASE", "off_offboarding_case",
                entity.getOffboardingCaseId().toString(), oldSnapshot, response, request.note());
        return response;
    }

    @Transactional
    public OffboardingDetailResponse closeOffboarding(Long offboardingCaseId, CloseOffboardingRequest request) {
        OffOffboardingCase entity = getCase(offboardingCaseId);
        if (entity.getStatus() != OffboardingStatus.SETTLEMENT_PREPARED && entity.getStatus() != OffboardingStatus.ACCESS_REVOKED) {
            throw new BusinessException("OFFBOARDING_CLOSE_STATUS_INVALID", "Chỉ được đóng hồ sơ offboarding sau bước settlement hoặc thu hồi truy cập.", HttpStatus.CONFLICT);
        }
        long pendingChecklist = checklistItemRepository.countByOffboardingCaseOffboardingCaseIdAndDeletedFalseAndStatusIn(
                offboardingCaseId, List.of(OffboardingChecklistStatus.OPEN, OffboardingChecklistStatus.IN_PROGRESS)
        );
        if (pendingChecklist > 0) {
            throw new BusinessException("OFFBOARDING_CHECKLIST_PENDING", "Vẫn còn checklist bàn giao chưa hoàn tất.", HttpStatus.CONFLICT);
        }
        long pendingAssets = assetReturnRepository.countByOffboardingCaseOffboardingCaseIdAndDeletedFalseAndStatus(
                offboardingCaseId, OffboardingAssetReturnStatus.PENDING
        );
        if (pendingAssets > 0) {
            throw new BusinessException("OFFBOARDING_ASSET_PENDING", "Vẫn còn tài sản chưa được xử lý bàn giao/thu hồi.", HttpStatus.CONFLICT);
        }
        if (userAccountRepository.findByEmployeeIdAndDeletedFalse(entity.getEmployee().getEmployeeId()).isPresent()
                && entity.getAccessRevokedAt() == null) {
            throw new BusinessException("OFFBOARDING_ACCESS_REVOKE_REQUIRED", "Phải thu hồi quyền truy cập trước khi đóng offboarding.", HttpStatus.CONFLICT);
        }

        OffboardingDetailResponse oldSnapshot = toDetail(entity);
        entity.setStatus(OffboardingStatus.CLOSED);
        entity.setClosedBy(accessScopeService.getCurrentUserRequired());
        entity.setClosedAt(LocalDateTime.now());
        entity.setCloseNote(request.note().trim());
        entity = offboardingCaseRepository.save(entity);

        HrEmployee employee = entity.getEmployee();
        employee.setEmploymentStatus(EmploymentStatus.RESIGNED);
        employeeRepository.save(employee);

        terminateActiveContracts(employee.getEmployeeId(), entity.getEffectiveLastWorkingDate());
        appendHistory(entity, oldSnapshot.status() == null ? null : OffboardingStatus.valueOf(oldSnapshot.status()),
                OffboardingStatus.CLOSED, "CLOSE", request.note());

        createPortalItemForEmployee(entity.getEmployee(), PortalInboxItemType.NOTIFICATION, "OFFBOARDING",
                "Hồ sơ offboarding đã được đóng",
                "Quy trình nghỉ việc của bạn đã được đóng hoàn tất trên hệ thống HRM.",
                "OFFBOARDING", entity.getOffboardingCaseId().toString(), null);

        OffboardingDetailResponse response = toDetail(entity);
        auditLogService.logSuccess("CLOSE", "OFFBOARDING_CASE", "off_offboarding_case",
                entity.getOffboardingCaseId().toString(), oldSnapshot, response, request.note());
        return response;
    }

    private void ensureNoOpenCase(Long employeeId) {
        Set<OffboardingStatus> openStatuses = EnumSet.of(
                OffboardingStatus.REQUESTED,
                OffboardingStatus.MANAGER_APPROVED,
                OffboardingStatus.HR_FINALIZED,
                OffboardingStatus.ACCESS_REVOKED,
                OffboardingStatus.SETTLEMENT_PREPARED
        );
        offboardingCaseRepository.findFirstByEmployeeEmployeeIdAndDeletedFalseAndStatusInOrderByCreatedAtDescOffboardingCaseIdDesc(employeeId, openStatuses)
                .ifPresent(item -> {
                    throw new BusinessException("OFFBOARDING_OPEN_CASE_EXISTS", "Nhân sự đã có hồ sơ offboarding đang xử lý.", HttpStatus.CONFLICT);
                });
    }

    private BigDecimal calculateLeaveSettlementAmount(Long employeeId, LocalDate date, BigDecimal remainingLeaveUnits) {
        if (remainingLeaveUnits == null || remainingLeaveUnits.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(SCALE_MONEY, RoundingMode.HALF_UP);
        }
        BigDecimal baseSalary = compensationRepository.findEffectiveByEmployeeAndDate(employeeId, date).stream()
                .findFirst()
                .map(PayEmployeeCompensation::getBaseSalary)
                .orElseGet(() -> findActiveContract(employeeId, date).map(CtLaborContract::getBaseSalary).orElse(BigDecimal.ZERO));
        if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(SCALE_MONEY, RoundingMode.HALF_UP);
        }
        return baseSalary.divide(STANDARD_WORKING_DAYS, 8, RoundingMode.HALF_UP)
                .multiply(remainingLeaveUnits)
                .setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }

    private Optional<CtLaborContract> findActiveContract(Long employeeId, LocalDate date) {
        return laborContractRepository.findAll((root, query, builder) -> builder.and(
                        builder.isFalse(root.get("deleted")),
                        builder.equal(root.join("employee").get("employeeId"), employeeId),
                        root.get("contractStatus").in(ContractStatus.ACTIVE, ContractStatus.SUSPENDED),
                        builder.lessThanOrEqualTo(root.get("effectiveDate"), date),
                        builder.or(builder.isNull(root.get("endDate")), builder.greaterThanOrEqualTo(root.get("endDate"), date))
                ), Sort.by("effectiveDate").descending().and(Sort.by("laborContractId").descending()))
                .stream().findFirst();
    }

    private void terminateActiveContracts(Long employeeId, LocalDate endDate) {
        List<CtLaborContract> contracts = laborContractRepository.findAll((root, query, builder) -> builder.and(
                        builder.isFalse(root.get("deleted")),
                        builder.equal(root.join("employee").get("employeeId"), employeeId),
                        root.get("contractStatus").in(ContractStatus.ACTIVE, ContractStatus.SUSPENDED)
                ));
        for (CtLaborContract contract : contracts) {
            contract.setContractStatus(ContractStatus.TERMINATED);
            if (endDate != null) {
                contract.setEndDate(endDate);
            }
        }
        laborContractRepository.saveAll(contracts);
    }

    private void applyChecklist(OffOffboardingChecklistItem item, UpsertOffboardingChecklistItemRequest request) {
        item.setItemType(request.itemType());
        item.setItemName(request.itemName().trim());
        item.setOwnerRoleCode(trimToNull(request.ownerRoleCode()));
        item.setDueDate(request.dueDate());
        item.setStatus(request.status() == null ? OffboardingChecklistStatus.OPEN : request.status());
        item.setNote(trimToNull(request.note()));
    }

    private void applyAssetReturn(OffOffboardingAssetReturn item, UpsertOffboardingAssetReturnRequest request) {
        item.setAssetCode(trimToNull(request.assetCode()));
        item.setAssetName(request.assetName().trim());
        item.setAssetType(trimToNull(request.assetType()));
        item.setExpectedReturnDate(request.expectedReturnDate());
        item.setReturnedDate(request.returnedDate());
        item.setStatus(request.status());
        item.setNote(trimToNull(request.note()));
        item.setUpdatedByUser(accessScopeService.getCurrentUserRequired());
        item.setUpdatedAtAction(LocalDateTime.now());
    }

    private void appendHistory(OffOffboardingCase entity, OffboardingStatus fromStatus, OffboardingStatus toStatus, String actionCode, String note) {
        OffOffboardingHistory history = new OffOffboardingHistory();
        history.setOffboardingCase(entity);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setActionCode(actionCode);
        history.setActionNote(trimToNull(note));
        history.setChangedBy(accessScopeService.getCurrentUserRequired());
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private void createPortalItemForEmployee(HrEmployee employee,
                                             PortalInboxItemType itemType,
                                             String categoryCode,
                                             String title,
                                             String message,
                                             String relatedModule,
                                             String relatedEntityId,
                                             LocalDateTime dueAt) {
        Optional<SecUserAccount> userOpt = userAccountRepository.findByEmployeeIdAndDeletedFalse(employee.getEmployeeId());
        if (userOpt.isEmpty()) {
            return;
        }
        PorPortalInboxItem item = new PorPortalInboxItem();
        item.setUser(userOpt.get());
        item.setEmployee(employee);
        item.setItemType(itemType);
        item.setCategoryCode(categoryCode);
        item.setTitle(title);
        item.setMessage(message);
        item.setRelatedModule(relatedModule);
        item.setRelatedEntityId(relatedEntityId);
        item.setDueAt(dueAt);
        item.setTaskStatus(PortalTaskStatus.OPEN);
        item.setStatus(RecordStatus.ACTIVE);
        portalInboxItemRepository.save(item);
    }

    private OffOffboardingCase getCase(Long offboardingCaseId) {
        return offboardingCaseRepository.findByOffboardingCaseIdAndDeletedFalse(offboardingCaseId)
                .orElseThrow(() -> new NotFoundException("OFFBOARDING_CASE_NOT_FOUND", "Không tìm thấy hồ sơ offboarding."));
    }

    private OffboardingListItemResponse toListItem(OffOffboardingCase entity) {
        HrEmployee employee = entity.getEmployee();
        return new OffboardingListItemResponse(
                entity.getOffboardingCaseId(),
                entity.getOffboardingCode(),
                employee.getEmployeeId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getOrgUnit() == null ? null : employee.getOrgUnit().getOrgUnitId(),
                employee.getOrgUnit() == null ? null : employee.getOrgUnit().getOrgUnitName(),
                employee.getManagerEmployee() == null ? null : employee.getManagerEmployee().getEmployeeId(),
                employee.getManagerEmployee() == null ? null : employee.getManagerEmployee().getFullName(),
                entity.getStatus().name(),
                entity.getRequestDate(),
                entity.getRequestedLastWorkingDate(),
                entity.getEffectiveLastWorkingDate(),
                entity.getHrFinalizedAt(),
                entity.getClosedAt()
        );
    }

    private OffboardingDetailResponse toDetail(OffOffboardingCase entity) {
        HrEmployee employee = entity.getEmployee();
        List<OffboardingChecklistItemResponse> checklistItems = checklistItemRepository
                .findAllByOffboardingCaseOffboardingCaseIdAndDeletedFalseOrderByDueDateAscOffboardingChecklistItemIdAsc(entity.getOffboardingCaseId())
                .stream().map(this::toChecklistResponse).toList();
        List<OffboardingAssetReturnResponse> assetReturns = assetReturnRepository
                .findAllByOffboardingCaseOffboardingCaseIdAndDeletedFalseOrderByExpectedReturnDateAscOffboardingAssetReturnIdAsc(entity.getOffboardingCaseId())
                .stream().map(this::toAssetResponse).toList();
        List<OffboardingHistoryResponse> histories = historyRepository
                .findAllByOffboardingCaseOffboardingCaseIdOrderByChangedAtDescOffboardingHistoryIdDesc(entity.getOffboardingCaseId())
                .stream().map(this::toHistoryResponse).toList();

        return new OffboardingDetailResponse(
                entity.getOffboardingCaseId(),
                entity.getOffboardingCode(),
                employee.getEmployeeId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getEmploymentStatus() == null ? null : employee.getEmploymentStatus().name(),
                employee.getOrgUnit() == null ? null : employee.getOrgUnit().getOrgUnitId(),
                employee.getOrgUnit() == null ? null : employee.getOrgUnit().getOrgUnitName(),
                employee.getManagerEmployee() == null ? null : employee.getManagerEmployee().getEmployeeId(),
                employee.getManagerEmployee() == null ? null : employee.getManagerEmployee().getFullName(),
                entity.getStatus().name(),
                entity.getRequestReason(),
                entity.getRequestDate(),
                entity.getRequestedLastWorkingDate(),
                entity.getManagerReviewNote(),
                entity.getManagerReviewedAt(),
                entity.getEffectiveLastWorkingDate(),
                entity.getHrFinalizeNote(),
                entity.getHrFinalizedAt(),
                entity.getAccessRevokeNote(),
                entity.getAccessRevokedAt(),
                entity.getFinalAttendanceYear(),
                entity.getFinalAttendanceMonth(),
                entity.getLeaveSettlementUnits(),
                entity.getLeaveSettlementAmount(),
                entity.getFinalPayrollPeriod() == null ? null : entity.getFinalPayrollPeriod().getPayrollPeriodId(),
                entity.getFinalPayrollItem() == null ? null : entity.getFinalPayrollItem().getPayrollItemId(),
                entity.getSettlementNote(),
                entity.getSettlementPreparedAt(),
                entity.getCloseNote(),
                entity.getClosedAt(),
                checklistItems,
                assetReturns,
                histories
        );
    }

    private OffboardingChecklistItemResponse toChecklistResponse(OffOffboardingChecklistItem entity) {
        return new OffboardingChecklistItemResponse(
                entity.getOffboardingChecklistItemId(),
                entity.getItemType() == null ? null : entity.getItemType().name(),
                entity.getItemName(),
                entity.getOwnerRoleCode(),
                entity.getDueDate(),
                entity.getStatus() == null ? null : entity.getStatus().name(),
                entity.getNote(),
                entity.getCompletedAt()
        );
    }

    private OffboardingAssetReturnResponse toAssetResponse(OffOffboardingAssetReturn entity) {
        return new OffboardingAssetReturnResponse(
                entity.getOffboardingAssetReturnId(),
                entity.getAssetCode(),
                entity.getAssetName(),
                entity.getAssetType(),
                entity.getExpectedReturnDate(),
                entity.getReturnedDate(),
                entity.getStatus() == null ? null : entity.getStatus().name(),
                entity.getNote(),
                entity.getUpdatedAtAction()
        );
    }

    private OffboardingHistoryResponse toHistoryResponse(OffOffboardingHistory entity) {
        return new OffboardingHistoryResponse(
                entity.getOffboardingHistoryId(),
                entity.getFromStatus() == null ? null : entity.getFromStatus().name(),
                entity.getToStatus() == null ? null : entity.getToStatus().name(),
                entity.getActionCode(),
                entity.getActionNote(),
                entity.getChangedBy() == null ? null : entity.getChangedBy().getUsername(),
                entity.getChangedAt()
        );
    }

    private String generateCode() {
        return "OFF-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(java.util.Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
