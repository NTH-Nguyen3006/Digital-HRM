package com.company.hrm.module.leave.service;

import com.company.hrm.common.constant.LeaveBalanceStatus;
import com.company.hrm.common.constant.LeaveBalanceTransactionType;
import com.company.hrm.common.constant.LeaveRequestStatus;
import com.company.hrm.common.constant.LeaveUnitType;
import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.ForbiddenException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.leave.dto.*;
import com.company.hrm.module.leave.entity.*;
import com.company.hrm.module.leave.repository.*;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.module.storage.service.StorageFileService;
import com.company.hrm.security.SecurityUserContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveService {

    private static final Set<LeaveRequestStatus> ACTIVE_OVERLAP_STATUSES = Set.of(
            LeaveRequestStatus.SUBMITTED,
            LeaveRequestStatus.APPROVED,
            LeaveRequestStatus.FINALIZED
    );

    private final LeaLeaveBalanceRepository leaveBalanceRepository;
    private final LeaLeaveBalanceTransactionRepository leaveBalanceTransactionRepository;
    private final LeaLeaveRequestRepository leaveRequestRepository;
    private final LeaLeaveRequestHistoryRepository leaveRequestHistoryRepository;
    private final HrEmployeeRepository employeeRepository;
    private final SecUserAccountRepository userAccountRepository;
    private final LeaveTypeService leaveTypeService;
    private final LeaveAccessScopeService leaveAccessScopeService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final StorageFileService storageFileService;

    public LeaveService(
            LeaLeaveBalanceRepository leaveBalanceRepository,
            LeaLeaveBalanceTransactionRepository leaveBalanceTransactionRepository,
            LeaLeaveRequestRepository leaveRequestRepository,
            LeaLeaveRequestHistoryRepository leaveRequestHistoryRepository,
            HrEmployeeRepository employeeRepository,
            SecUserAccountRepository userAccountRepository,
            LeaveTypeService leaveTypeService,
            LeaveAccessScopeService leaveAccessScopeService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper,
            StorageFileService storageFileService
    ) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveBalanceTransactionRepository = leaveBalanceTransactionRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveRequestHistoryRepository = leaveRequestHistoryRepository;
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
        this.leaveTypeService = leaveTypeService;
        this.leaveAccessScopeService = leaveAccessScopeService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
        this.storageFileService = storageFileService;
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveBalanceListItemResponse> listBalances(String keyword, Long leaveTypeId, Integer leaveYear, int page, int size) {
        Specification<LeaLeaveBalance> specification = (root, query, builder) -> builder.isFalse(root.get("deleted"));
        if (keyword != null && !keyword.isBlank()) {
            String likeValue = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.join("employee").get("employeeCode")), likeValue),
                    builder.like(builder.lower(root.join("employee").get("fullName")), likeValue),
                    builder.like(builder.lower(root.join("leaveType").get("leaveTypeCode")), likeValue),
                    builder.like(builder.lower(root.join("leaveType").get("leaveTypeName")), likeValue)
            ));
        }
        if (leaveTypeId != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.join("leaveType").get("leaveTypeId"), leaveTypeId));
        }
        if (leaveYear != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("leaveYear"), leaveYear));
        }
        Page<LeaLeaveBalance> result = leaveBalanceRepository.findAll(specification, PageRequest.of(page, size, Sort.by("leaveYear").descending().and(Sort.by("employee.employeeCode").ascending())));
        return new PageResponse<>(
                result.getContent().stream().map(this::toBalanceListItem).toList(),
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    public LeaveBalanceResponse getBalanceDetail(Long leaveBalanceId) {
        LeaLeaveBalance balance = leaveBalanceRepository.findByLeaveBalanceIdAndDeletedFalse(leaveBalanceId)
                .orElseThrow(() -> new NotFoundException("LEAVE_BALANCE_NOT_FOUND", "Không tìm thấy số dư phép."));
        return toBalanceResponse(balance);
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getMyBalances() {
        HrEmployee currentEmployee = leaveAccessScopeService.getCurrentEmployeeRequired();
        return leaveBalanceRepository.findAllByEmployeeEmployeeIdAndDeletedFalseOrderByLeaveYearDescLeaveTypeLeaveTypeCodeAsc(currentEmployee.getEmployeeId())
                .stream()
                .map(this::toBalanceResponse)
                .toList();
    }

    @Transactional
    public LeaveBalanceResponse adjustBalance(LeaveBalanceAdjustmentRequest request) {
        HrEmployee employee = getEmployee(request.employeeId());
        LeaLeaveType leaveType = leaveTypeService.getLeaveTypeEntity(request.leaveTypeId());
        LeaLeaveBalance balance = getOrCreateBalance(employee, leaveType, request.leaveYear());
        LeaveBalanceResponse oldSnapshot = toBalanceResponse(balance);
        BigDecimal before = nvl(balance.getAvailableUnits());
        BigDecimal after = before.add(scale(request.quantityDelta()));
        if (after.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("LEAVE_BALANCE_NEGATIVE_NOT_ALLOWED", "Điều chỉnh này làm số dư phép âm.", HttpStatus.BAD_REQUEST);
        }

        if (request.openingBalance()) {
            balance.setOpeningUnits(nvl(balance.getOpeningUnits()).add(scale(request.quantityDelta())));
        } else {
            balance.setAdjustedUnits(nvl(balance.getAdjustedUnits()).add(scale(request.quantityDelta())));
        }
        balance.setAvailableUnits(after);
        balance.setLastTransactionAt(LocalDateTime.now());
        balance = leaveBalanceRepository.save(balance);

        createBalanceTransaction(balance, null, request.openingBalance() ? LeaveBalanceTransactionType.OPENING : LeaveBalanceTransactionType.MANUAL_ADJUSTMENT,
                request.quantityDelta(), request.referenceNo(), request.reason(), request.attachmentRef());

        LeaveBalanceResponse response = toBalanceResponse(balance);
        auditLogService.logSuccess("ADJUST_BALANCE", "LEAVE_BALANCE", "lea_leave_balance", balance.getLeaveBalanceId().toString(), oldSnapshot, response, request.reason());
        return response;
    }

    @Transactional
    public LeaveRequestDetailResponse createMyRequest(CreateLeaveRequestRequest request) {
        HrEmployee currentEmployee = leaveAccessScopeService.getCurrentEmployeeRequired();
        LeaLeaveRequest entity = new LeaLeaveRequest();
        entity.setRequestCode(generateRequestCode(currentEmployee.getEmployeeId()));
        applyRequest(entity, currentEmployee, request.leaveTypeId(), request.startDate(), request.endDate(), request.requestedUnits(), request.reason(), request.attachmentRef(), request.submit(), null);
        entity = leaveRequestRepository.save(entity);
        appendHistory(entity, null, entity.getRequestStatus(), request.submit() ? "SUBMIT" : "DRAFT_SAVE", request.reason());
        LeaveRequestDetailResponse response = toRequestDetail(entity);
        auditLogService.logSuccess("CREATE", "LEAVE_REQUEST", "lea_leave_request", entity.getLeaveRequestId().toString(), null, response, "Tạo đơn xin nghỉ.");
        return response;
    }

    @Transactional
    public LeaveRequestDetailResponse updateMyRequest(Long leaveRequestId, UpdateLeaveRequestRequest request) {
        LeaLeaveRequest entity = getLeaveRequest(leaveRequestId);
        leaveAccessScopeService.assertSelfRequestOwner(entity);
        if (entity.getRequestStatus() != LeaveRequestStatus.DRAFT && entity.getRequestStatus() != LeaveRequestStatus.SUBMITTED) {
            throw new BusinessException("LEAVE_REQUEST_UPDATE_STATUS_INVALID", "Chỉ được sửa đơn ở trạng thái nháp hoặc chờ duyệt.", HttpStatus.CONFLICT);
        }
        LeaveRequestDetailResponse oldSnapshot = toRequestDetail(entity);
        LeaveRequestStatus oldStatus = entity.getRequestStatus();
        applyRequest(entity, entity.getEmployee(), request.leaveTypeId(), request.startDate(), request.endDate(), request.requestedUnits(), request.reason(), request.attachmentRef(), request.submit(), leaveRequestId);
        entity = leaveRequestRepository.save(entity);
        appendHistory(entity, oldStatus, entity.getRequestStatus(), "UPDATE", request.reason());
        LeaveRequestDetailResponse response = toRequestDetail(entity);
        auditLogService.logSuccess("UPDATE", "LEAVE_REQUEST", "lea_leave_request", entity.getLeaveRequestId().toString(), oldSnapshot, response, "Cập nhật đơn xin nghỉ.");
        return response;
    }

    @Transactional
    public LeaveRequestDetailResponse cancelMyRequest(Long leaveRequestId, CancelLeaveRequestRequest request) {
        LeaLeaveRequest entity = getLeaveRequest(leaveRequestId);
        leaveAccessScopeService.assertSelfRequestOwner(entity);
        if (entity.getRequestStatus() != LeaveRequestStatus.DRAFT && entity.getRequestStatus() != LeaveRequestStatus.SUBMITTED) {
            throw new BusinessException("LEAVE_REQUEST_CANCEL_STATUS_INVALID", "Chỉ được hủy đơn ở trạng thái nháp hoặc chờ duyệt.", HttpStatus.CONFLICT);
        }
        LeaveRequestDetailResponse oldSnapshot = toRequestDetail(entity);
        LeaveRequestStatus fromStatus = entity.getRequestStatus();
        entity.setRequestStatus(LeaveRequestStatus.CANCELLED);
        entity.setCanceledAt(LocalDateTime.now());
        entity.setCanceledBy(getCurrentUserOrNull());
        entity.setCancelNote(request.cancelNote().trim());
        entity = leaveRequestRepository.save(entity);
        appendHistory(entity, fromStatus, LeaveRequestStatus.CANCELLED, "CANCEL", request.cancelNote());
        LeaveRequestDetailResponse response = toRequestDetail(entity);
        auditLogService.logSuccess("CANCEL", "LEAVE_REQUEST", "lea_leave_request", entity.getLeaveRequestId().toString(), oldSnapshot, response, request.cancelNote());
        return response;
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDetailResponse> listMyRequests() {
        HrEmployee currentEmployee = leaveAccessScopeService.getCurrentEmployeeRequired();
        return leaveRequestRepository.findAllByEmployeeEmployeeIdAndDeletedFalseOrderByCreatedAtDescLeaveRequestIdDesc(currentEmployee.getEmployeeId())
                .stream()
                .map(this::toRequestDetail)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveRequestListItemResponse> listAdminRequests(
            String keyword,
            LeaveRequestStatus status,
            Long leaveTypeId,
            Long employeeId,
            int page,
            int size
    ) {
        Specification<LeaLeaveRequest> specification = (root, query, builder) -> builder.isFalse(root.get("deleted"));
        if (keyword != null && !keyword.isBlank()) {
            String likeValue = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("requestCode")), likeValue),
                    builder.like(builder.lower(root.join("employee").get("employeeCode")), likeValue),
                    builder.like(builder.lower(root.join("employee").get("fullName")), likeValue),
                    builder.like(builder.lower(root.join("leaveType").get("leaveTypeCode")), likeValue)
            ));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("requestStatus"), status));
        }
        if (leaveTypeId != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.join("leaveType").get("leaveTypeId"), leaveTypeId));
        }
        if (employeeId != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.join("employee").get("employeeId"), employeeId));
        }
        Page<LeaLeaveRequest> result = leaveRequestRepository.findAll(specification, PageRequest.of(page, size, Sort.by("createdAt").descending().and(Sort.by("leaveRequestId").descending())));
        return new PageResponse<>(
                result.getContent().stream().map(this::toRequestListItem).toList(),
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    public LeaveRequestDetailResponse getRequestDetail(Long leaveRequestId) {
        LeaLeaveRequest request = getLeaveRequest(leaveRequestId);
        if (!leaveAccessScopeService.isCurrentUserHrOrAdmin()) {
            try {
                leaveAccessScopeService.assertSelfRequestOwner(request);
            } catch (ForbiddenException ex) {
                leaveAccessScopeService.assertManagerCanAccessEmployee(request.getEmployee());
            }
        }
        return toRequestDetail(request);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestListItemResponse> listPendingForManager() {
        String pathPrefix = leaveAccessScopeService.getManagerOrgPathPrefix()
                .orElseThrow(() -> new ForbiddenException("MANAGER_SCOPE_REQUIRED", "Chỉ quản lý mới được xem danh sách phê duyệt nghỉ phép."));
        return leaveRequestRepository.findPendingByApprovalScope(LeaveRequestStatus.SUBMITTED, RoleCode.MANAGER, pathPrefix)
                .stream()
                .map(this::toRequestListItem)
                .toList();
    }

    @Transactional
    public LeaveRequestDetailResponse reviewByManager(Long leaveRequestId, ReviewLeaveRequestRequest request) {
        LeaLeaveRequest entity = getLeaveRequest(leaveRequestId);
        leaveAccessScopeService.assertManagerCanAccessEmployee(entity.getEmployee());
        if (entity.getApprovalRoleCode() != RoleCode.MANAGER) {
            throw new BusinessException("LEAVE_REQUEST_APPROVAL_ROLE_INVALID", "Đơn nghỉ này không thuộc luồng duyệt của quản lý.", HttpStatus.CONFLICT);
        }
        if (entity.getRequestStatus() != LeaveRequestStatus.SUBMITTED) {
            throw new BusinessException("LEAVE_REQUEST_REVIEW_STATUS_INVALID", "Chỉ được duyệt hoặc từ chối đơn ở trạng thái chờ duyệt.", HttpStatus.CONFLICT);
        }
        LeaveRequestDetailResponse oldSnapshot = toRequestDetail(entity);
        LeaveRequestStatus fromStatus = entity.getRequestStatus();
        if (Boolean.TRUE.equals(request.approved())) {
            entity.setRequestStatus(LeaveRequestStatus.APPROVED);
            entity.setApprovedAt(LocalDateTime.now());
            entity.setApprovedBy(getCurrentUserOrNull());
            entity.setApprovalNote(request.note().trim());
            appendHistory(entity, fromStatus, LeaveRequestStatus.APPROVED, "APPROVE", request.note());
        } else {
            entity.setRequestStatus(LeaveRequestStatus.REJECTED);
            entity.setRejectedAt(LocalDateTime.now());
            entity.setRejectedBy(getCurrentUserOrNull());
            entity.setRejectionNote(request.note().trim());
            appendHistory(entity, fromStatus, LeaveRequestStatus.REJECTED, "REJECT", request.note());
        }
        entity = leaveRequestRepository.save(entity);
        LeaveRequestDetailResponse response = toRequestDetail(entity);
        auditLogService.logSuccess("REVIEW", "LEAVE_REQUEST", "lea_leave_request", entity.getLeaveRequestId().toString(), oldSnapshot, response, request.note());
        return response;
    }

    @Transactional
    public LeaveRequestDetailResponse finalizeByHr(Long leaveRequestId, FinalizeLeaveRequestRequest request) {
        LeaLeaveRequest entity = getLeaveRequest(leaveRequestId);
        LeaveRequestDetailResponse oldSnapshot = toRequestDetail(entity);

        boolean hrIsApprover = entity.getApprovalRoleCode() == RoleCode.HR;
        if (hrIsApprover) {
            if (entity.getRequestStatus() != LeaveRequestStatus.SUBMITTED) {
                throw new BusinessException("LEAVE_REQUEST_FINALIZE_STATUS_INVALID", "Đơn nghỉ loại này chỉ được HR xử lý khi đang chờ duyệt.", HttpStatus.CONFLICT);
            }
        } else if (entity.getRequestStatus() != LeaveRequestStatus.APPROVED) {
            throw new BusinessException("LEAVE_REQUEST_FINALIZE_STATUS_INVALID", "Chỉ được chốt đơn đã được quản lý duyệt.", HttpStatus.CONFLICT);
        }

        LeaveRequestStatus fromStatus = entity.getRequestStatus();
        if (!Boolean.TRUE.equals(request.approved())) {
            entity.setRequestStatus(LeaveRequestStatus.REJECTED);
            entity.setRejectedAt(LocalDateTime.now());
            entity.setRejectedBy(getCurrentUserOrNull());
            entity.setRejectionNote(request.note().trim());
            entity = leaveRequestRepository.save(entity);
            appendHistory(entity, fromStatus, LeaveRequestStatus.REJECTED, "FINAL_REJECT", request.note());
            LeaveRequestDetailResponse response = toRequestDetail(entity);
            auditLogService.logSuccess("FINAL_REJECT", "LEAVE_REQUEST", "lea_leave_request", entity.getLeaveRequestId().toString(), oldSnapshot, response, request.note());
            return response;
        }

        LeaLeaveBalance balance = getOrCreateBalance(entity.getEmployee(), entity.getLeaveType(), entity.getStartDate().getYear());
        LeaLeaveTypeRule rule = entity.getLeaveTypeRule();
        BigDecimal before = nvl(balance.getAvailableUnits());
        BigDecimal required = scale(entity.getRequestedUnits());

        if (rule.isRequiresBalanceCheck() && !rule.isAllowNegativeBalance() && before.compareTo(required) < 0) {
            throw new BusinessException("LEAVE_BALANCE_INSUFFICIENT", "Số dư phép không đủ để chốt đơn nghỉ.", HttpStatus.CONFLICT, Map.of(
                    "availableUnits", before,
                    "requestedUnits", required
            ));
        }

        balance.setUsedUnits(nvl(balance.getUsedUnits()).add(required));
        balance.setAvailableUnits(before.subtract(required));
        balance.setLastTransactionAt(LocalDateTime.now());
        leaveBalanceRepository.save(balance);
        createBalanceTransaction(balance, entity, LeaveBalanceTransactionType.REQUEST_DEBIT, required.negate(), entity.getRequestCode(), request.note(), entity.getAttachmentRef());

        entity.setRequestStatus(LeaveRequestStatus.FINALIZED);
        entity.setFinalizedAt(LocalDateTime.now());
        entity.setFinalizedBy(getCurrentUserOrNull());
        entity.setFinalizeNote(request.note().trim());
        if (hrIsApprover) {
            entity.setApprovedAt(LocalDateTime.now());
            entity.setApprovedBy(getCurrentUserOrNull());
            entity.setApprovalNote("HR approval at finalization");
        }
        entity = leaveRequestRepository.save(entity);
        appendHistory(entity, fromStatus, LeaveRequestStatus.FINALIZED, "FINALIZE", request.note());

        LeaveRequestDetailResponse response = toRequestDetail(entity);
        auditLogService.logSuccess("FINALIZE", "LEAVE_REQUEST", "lea_leave_request", entity.getLeaveRequestId().toString(), oldSnapshot, response, request.note());
        return response;
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestListItemResponse> getTeamCalendar(LocalDate fromDate, LocalDate toDate) {
        String pathPrefix = leaveAccessScopeService.getManagerOrgPathPrefix()
                .orElseThrow(() -> new ForbiddenException("MANAGER_SCOPE_REQUIRED", "Chỉ quản lý mới được xem lịch nghỉ của team."));
        LocalDate start = fromDate == null ? LocalDate.now().minusDays(7) : fromDate;
        LocalDate end = toDate == null ? LocalDate.now().plusDays(30) : toDate;
        return leaveRequestRepository.findTeamCalendar(pathPrefix, start, end, LeaveRequestStatus.FINALIZED)
                .stream()
                .map(this::toRequestListItem)
                .toList();
    }

    @Transactional
    public LeaveSettlementResponse settleRemainingLeave(LeaveSettlementRequest request) {
        HrEmployee employee = getEmployee(request.employeeId());
        LeaLeaveType leaveType = leaveTypeService.getLeaveTypeEntity(request.leaveTypeId());
        LeaLeaveBalance balance = getOrCreateBalance(employee, leaveType, request.settlementDate().getYear());
        if (balance.getBalanceStatus() == LeaveBalanceStatus.SETTLED) {
            throw new BusinessException("LEAVE_BALANCE_ALREADY_SETTLED", "Số dư phép này đã được quyết toán.", HttpStatus.CONFLICT);
        }
        BigDecimal before = nvl(balance.getAvailableUnits());
        BigDecimal settledUnits = before.max(BigDecimal.ZERO);
        BigDecimal after = before.subtract(settledUnits);
        balance.setSettledUnits(nvl(balance.getSettledUnits()).add(settledUnits));
        balance.setAvailableUnits(after);
        balance.setBalanceStatus(LeaveBalanceStatus.SETTLED);
        balance.setSettlementNote(request.reason().trim());
        balance.setLastTransactionAt(LocalDateTime.now());
        leaveBalanceRepository.save(balance);
        if (settledUnits.compareTo(BigDecimal.ZERO) > 0) {
            createBalanceTransaction(balance, null, LeaveBalanceTransactionType.TERMINATION_SETTLEMENT, settledUnits.negate(), null, request.reason(), null);
        }
        LeaveSettlementResponse response = new LeaveSettlementResponse(
                employee.getEmployeeId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                leaveType.getLeaveTypeId(),
                leaveType.getLeaveTypeCode(),
                leaveType.getLeaveTypeName(),
                request.settlementDate(),
                balance.getLeaveYear(),
                before,
                settledUnits,
                after,
                balance.getBalanceStatus().name()
        );
        auditLogService.logSuccess("SETTLEMENT", "LEAVE_BALANCE", "lea_leave_balance", balance.getLeaveBalanceId().toString(), null, response, request.reason());
        return response;
    }

    @Transactional
    public int closePeriod(LeavePeriodCloseRequest request) {
        List<LeaLeaveBalance> balances = leaveBalanceRepository.findAllByLeaveYearAndDeletedFalseAndBalanceStatusIn(
                request.sourceYear(),
                List.of(LeaveBalanceStatus.OPEN, LeaveBalanceStatus.CLOSED)
        ).stream().filter(item -> request.leaveTypeId() == null || item.getLeaveType().getLeaveTypeId().equals(request.leaveTypeId())).toList();

        int processed = 0;
        for (LeaLeaveBalance balance : balances) {
            if (balance.getBalanceStatus() == LeaveBalanceStatus.SETTLED) {
                continue;
            }
            LeaLeaveTypeRule rule = leaveTypeService.getCurrentRule(balance.getLeaveType().getLeaveTypeId(), LocalDate.of(request.sourceYear(), 12, 31));
            BigDecimal available = nvl(balance.getAvailableUnits()).max(BigDecimal.ZERO);
            BigDecimal carryForward = available.min(nvl(rule.getCarryForwardMaxUnits()));
            BigDecimal resetAmount = available.subtract(carryForward);

            if (resetAmount.compareTo(BigDecimal.ZERO) > 0) {
                createBalanceTransaction(balance, null, LeaveBalanceTransactionType.PERIOD_RESET_OUT, resetAmount.negate(), "YEAR_CLOSE_" + request.sourceYear(), "Reset số dư cuối kỳ", null);
            }
            balance.setAvailableUnits(BigDecimal.ZERO);
            balance.setBalanceStatus(LeaveBalanceStatus.CLOSED);
            balance.setClosedAt(LocalDateTime.now());
            balance.setClosedBy(getCurrentUserOrNull());
            leaveBalanceRepository.save(balance);

            LeaLeaveBalance nextYearBalance = getOrCreateBalance(balance.getEmployee(), balance.getLeaveType(), request.targetYear());
            BigDecimal before = nvl(nextYearBalance.getAvailableUnits());
            BigDecimal newAccrued = nvl(rule.getDefaultEntitlementUnits());
            BigDecimal addition = carryForward.add(newAccrued);

            nextYearBalance.setCarriedForwardUnits(nvl(nextYearBalance.getCarriedForwardUnits()).add(carryForward));
            nextYearBalance.setAccruedUnits(nvl(nextYearBalance.getAccruedUnits()).add(newAccrued));
            nextYearBalance.setAvailableUnits(before.add(addition));
            nextYearBalance.setLastTransactionAt(LocalDateTime.now());
            leaveBalanceRepository.save(nextYearBalance);

            if (carryForward.compareTo(BigDecimal.ZERO) > 0) {
                createBalanceTransaction(nextYearBalance, null, LeaveBalanceTransactionType.PERIOD_CARRY_FORWARD_IN, carryForward, "YEAR_OPEN_" + request.targetYear(), "Bảo lưu phép từ kỳ trước", null);
            }
            if (newAccrued.compareTo(BigDecimal.ZERO) > 0) {
                createBalanceTransaction(nextYearBalance, null, LeaveBalanceTransactionType.OPENING, newAccrued, "YEAR_OPEN_" + request.targetYear(), "Khởi tạo entitlement đầu kỳ", null);
            }
            processed++;
        }
        auditLogService.logSuccess("PERIOD_CLOSE", "LEAVE_BALANCE", "lea_leave_balance", null, null, Map.of("processed", processed, "sourceYear", request.sourceYear(), "targetYear", request.targetYear()), "Chốt kỳ phép.");
        return processed;
    }

    @Transactional(readOnly = true)
    public String exportRequestsCsv(String keyword, LeaveRequestStatus status, Long leaveTypeId, Long employeeId) {
        PageResponse<LeaveRequestListItemResponse> page = listAdminRequests(keyword, status, leaveTypeId, employeeId, 0, 10000);
        String header = "requestCode,employeeCode,employeeName,leaveTypeCode,leaveTypeName,startDate,endDate,requestedUnits,status,approvalRole,submittedDate";
        return header + "\n" + page.items().stream()
                .map(item -> String.join(",",
                        csv(item.requestCode()),
                        csv(item.employeeCode()),
                        csv(item.employeeName()),
                        csv(item.leaveTypeCode()),
                        csv(item.leaveTypeName()),
                        csv(item.startDate()),
                        csv(item.endDate()),
                        csv(item.requestedUnits()),
                        csv(item.requestStatus()),
                        csv(item.approvalRoleCode()),
                        csv(item.submittedDate())
                ))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    private void applyRequest(
            LeaLeaveRequest entity,
            HrEmployee employee,
            Long leaveTypeId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal requestedUnits,
            String reason,
            String attachmentRef,
            boolean submit,
            Long excludeLeaveRequestId
    ) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("LEAVE_REQUEST_DATE_RANGE_INVALID", "endDate phải lớn hơn hoặc bằng startDate.", HttpStatus.BAD_REQUEST);
        }
        LeaLeaveType leaveType = leaveTypeService.getLeaveTypeEntity(leaveTypeId);
        LeaLeaveTypeRule rule = leaveTypeService.getCurrentRule(leaveTypeId, startDate);
        BigDecimal computedUnits = calculateRequestedUnits(rule, startDate, endDate, requestedUnits);
        validateLeaveRule(rule, startDate, endDate, computedUnits, attachmentRef);
        if (leaveRequestRepository.existsOverlappingRequest(employee.getEmployeeId(), excludeLeaveRequestId, ACTIVE_OVERLAP_STATUSES, startDate, endDate)) {
            throw new BusinessException("LEAVE_REQUEST_OVERLAP", "Đã tồn tại đơn nghỉ trùng khoảng thời gian.", HttpStatus.CONFLICT);
        }
        if (submit && rule.isRequiresBalanceCheck() && !rule.isAllowNegativeBalance()) {
            LeaLeaveBalance balance = getOrCreateBalance(employee, leaveType, startDate.getYear());
            if (nvl(balance.getAvailableUnits()).compareTo(computedUnits) < 0) {
                throw new BusinessException("LEAVE_BALANCE_INSUFFICIENT", "Số dư phép không đủ để gửi đơn.", HttpStatus.CONFLICT, Map.of(
                        "availableUnits", balance.getAvailableUnits(),
                        "requestedUnits", computedUnits
                ));
            }
        }

        entity.setEmployee(employee);
        entity.setLeaveType(leaveType);
        entity.setLeaveTypeRule(rule);
        entity.setApprovalRoleCode(rule.getApprovalRoleCode());
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setRequestedUnits(computedUnits);
        entity.setReason(reason.trim());
        entity.setAttachmentRef(blankToNull(attachmentRef));
        if (submit) {
            entity.setRequestStatus(LeaveRequestStatus.SUBMITTED);
            entity.setSubmittedAt(LocalDateTime.now());
        } else {
            entity.setRequestStatus(LeaveRequestStatus.DRAFT);
            entity.setSubmittedAt(null);
        }
    }

    private void validateLeaveRule(LeaLeaveTypeRule rule, LocalDate startDate, LocalDate endDate, BigDecimal requestedUnits, String attachmentRef) {
        if (attachmentRef != null && !attachmentRef.isBlank()) {
            storageFileService.requireActiveReference(attachmentRef, "LEAVE_ATTACHMENT_STORAGE_NOT_FOUND", "attachmentRef không tồn tại hoặc không còn hiệu lực.");
        }
        long noticeDays = ChronoUnit.DAYS.between(LocalDate.now(), startDate);
        if (noticeDays < rule.getMinNoticeDays()) {
            throw new BusinessException("LEAVE_REQUEST_MIN_NOTICE_VIOLATION", "Không đáp ứng số ngày báo trước tối thiểu.", HttpStatus.BAD_REQUEST);
        }
        if (rule.getMaxConsecutiveUnits() != null && requestedUnits.compareTo(rule.getMaxConsecutiveUnits()) > 0) {
            throw new BusinessException("LEAVE_REQUEST_MAX_CONSECUTIVE_VIOLATION", "Vượt quá giới hạn số đơn vị nghỉ liên tiếp.", HttpStatus.BAD_REQUEST);
        }
        if (rule.isRequiresAttachment() && (attachmentRef == null || attachmentRef.isBlank())) {
            throw new BusinessException("LEAVE_REQUEST_ATTACHMENT_REQUIRED", "Loại nghỉ này yêu cầu hồ sơ minh chứng.", HttpStatus.BAD_REQUEST);
        }
    }

    private BigDecimal calculateRequestedUnits(LeaLeaveTypeRule rule, LocalDate startDate, LocalDate endDate, BigDecimal requestedUnits) {
        if (rule.getUnitType() == LeaveUnitType.HOUR) {
            if (requestedUnits == null) {
                throw new BusinessException("LEAVE_REQUEST_UNITS_REQUIRED", "requestedUnits là bắt buộc với loại nghỉ tính theo giờ.", HttpStatus.BAD_REQUEST);
            }
            return scale(requestedUnits);
        }
        if (requestedUnits != null) {
            return scale(requestedUnits);
        }
        return BigDecimal.valueOf(ChronoUnit.DAYS.between(startDate, endDate) + 1L).setScale(2, RoundingMode.HALF_UP);
    }

    private LeaLeaveBalance getOrCreateBalance(HrEmployee employee, LeaLeaveType leaveType, Integer leaveYear) {
        return leaveBalanceRepository.findByEmployeeEmployeeIdAndLeaveTypeLeaveTypeIdAndLeaveYearAndDeletedFalse(
                employee.getEmployeeId(),
                leaveType.getLeaveTypeId(),
                leaveYear
        ).orElseGet(() -> {
            LeaLeaveBalance created = new LeaLeaveBalance();
            created.setEmployee(employee);
            created.setLeaveType(leaveType);
            created.setLeaveYear(leaveYear);
            created.setOpeningUnits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            created.setAccruedUnits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            created.setUsedUnits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            created.setAdjustedUnits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            created.setCarriedForwardUnits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            created.setSettledUnits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            created.setAvailableUnits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            created.setBalanceStatus(LeaveBalanceStatus.OPEN);
            return leaveBalanceRepository.save(created);
        });
    }

    private void createBalanceTransaction(
            LeaLeaveBalance balance,
            LeaLeaveRequest leaveRequest,
            LeaveBalanceTransactionType transactionType,
            BigDecimal quantityDelta,
            String referenceNo,
            String reason,
            String attachmentRef
    ) {
        LeaLeaveBalanceTransaction transaction = new LeaLeaveBalanceTransaction();
        transaction.setLeaveBalance(balance);
        transaction.setEmployee(balance.getEmployee());
        transaction.setLeaveType(balance.getLeaveType());
        transaction.setLeaveRequest(leaveRequest);
        transaction.setTransactionType(transactionType);
        BigDecimal before = nvl(balance.getAvailableUnits()).subtract(scale(quantityDelta));
        transaction.setQuantityDelta(scale(quantityDelta));
        transaction.setBalanceBefore(before);
        transaction.setBalanceAfter(nvl(balance.getAvailableUnits()));
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setReferenceNo(blankToNull(referenceNo));
        transaction.setReason(blankToNull(reason));
        transaction.setAttachmentRef(blankToNull(attachmentRef));
        leaveBalanceTransactionRepository.save(transaction);
    }

    private void appendHistory(LeaLeaveRequest leaveRequest, LeaveRequestStatus fromStatus, LeaveRequestStatus toStatus, String actionCode, String note) {
        LeaLeaveRequestHistory history = new LeaLeaveRequestHistory();
        history.setLeaveRequest(leaveRequest);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setActionCode(actionCode);
        history.setActionNote(blankToNull(note));
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(getCurrentUserOrNull());
        history.setSnapshotJson(toJson(Map.of(
                "requestCode", leaveRequest.getRequestCode(),
                "status", leaveRequest.getRequestStatus().name(),
                "requestedUnits", leaveRequest.getRequestedUnits()
        )));
        leaveRequestHistoryRepository.save(history);
    }

    private LeaLeaveRequest getLeaveRequest(Long leaveRequestId) {
        return leaveRequestRepository.findByLeaveRequestIdAndDeletedFalse(leaveRequestId)
                .orElseThrow(() -> new NotFoundException("LEAVE_REQUEST_NOT_FOUND", "Không tìm thấy đơn nghỉ."));
    }

    private HrEmployee getEmployee(Long employeeId) {
        return employeeRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new NotFoundException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân sự."));
    }

    private SecUserAccount getCurrentUserOrNull() {
        return SecurityUserContext.getCurrentUserId()
                .flatMap(userAccountRepository::findById)
                .orElse(null);
    }

    private LeaveBalanceListItemResponse toBalanceListItem(LeaLeaveBalance entity) {
        return new LeaveBalanceListItemResponse(
                entity.getLeaveBalanceId(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getLeaveType().getLeaveTypeId(),
                entity.getLeaveType().getLeaveTypeCode(),
                entity.getLeaveType().getLeaveTypeName(),
                entity.getLeaveYear(),
                entity.getAvailableUnits(),
                entity.getBalanceStatus().name()
        );
    }

    private LeaveBalanceResponse toBalanceResponse(LeaLeaveBalance entity) {
        return new LeaveBalanceResponse(
                entity.getLeaveBalanceId(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getLeaveType().getLeaveTypeId(),
                entity.getLeaveType().getLeaveTypeCode(),
                entity.getLeaveType().getLeaveTypeName(),
                entity.getLeaveYear(),
                entity.getOpeningUnits(),
                entity.getAccruedUnits(),
                entity.getUsedUnits(),
                entity.getAdjustedUnits(),
                entity.getCarriedForwardUnits(),
                entity.getSettledUnits(),
                entity.getAvailableUnits(),
                entity.getBalanceStatus().name(),
                entity.getLastTransactionAt(),
                leaveBalanceTransactionRepository.findAllByLeaveBalanceLeaveBalanceIdOrderByTransactionDateDescLeaveBalanceTxnIdDesc(entity.getLeaveBalanceId())
                        .stream()
                        .map(this::toBalanceTxnResponse)
                        .toList()
        );
    }

    private LeaveBalanceTransactionResponse toBalanceTxnResponse(LeaLeaveBalanceTransaction entity) {
        return new LeaveBalanceTransactionResponse(
                entity.getLeaveBalanceTxnId(),
                entity.getTransactionType().name(),
                entity.getQuantityDelta(),
                entity.getBalanceBefore(),
                entity.getBalanceAfter(),
                entity.getTransactionDate(),
                entity.getReferenceNo(),
                entity.getReason()
        );
    }

    private LeaveRequestListItemResponse toRequestListItem(LeaLeaveRequest entity) {
        return new LeaveRequestListItemResponse(
                entity.getLeaveRequestId(),
                entity.getRequestCode(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getLeaveType().getLeaveTypeId(),
                entity.getLeaveType().getLeaveTypeCode(),
                entity.getLeaveType().getLeaveTypeName(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getRequestedUnits(),
                entity.getRequestStatus().name(),
                entity.getApprovalRoleCode().name(),
                entity.getSubmittedAt() == null ? null : entity.getSubmittedAt().toLocalDate()
        );
    }

    private LeaveRequestDetailResponse toRequestDetail(LeaLeaveRequest entity) {
        return new LeaveRequestDetailResponse(
                entity.getLeaveRequestId(),
                entity.getRequestCode(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getLeaveType().getLeaveTypeId(),
                entity.getLeaveType().getLeaveTypeCode(),
                entity.getLeaveType().getLeaveTypeName(),
                entity.getLeaveTypeRule().getLeaveTypeRuleId(),
                entity.getLeaveTypeRule().getVersionNo(),
                entity.getApprovalRoleCode().name(),
                entity.getRequestStatus().name(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getRequestedUnits(),
                entity.getReason(),
                entity.getAttachmentRef(),
                entity.getSubmittedAt(),
                entity.getApprovedAt(),
                entity.getApprovedBy() == null ? null : entity.getApprovedBy().getUsername(),
                entity.getApprovalNote(),
                entity.getRejectedAt(),
                entity.getRejectedBy() == null ? null : entity.getRejectedBy().getUsername(),
                entity.getRejectionNote(),
                entity.getFinalizedAt(),
                entity.getFinalizedBy() == null ? null : entity.getFinalizedBy().getUsername(),
                entity.getFinalizeNote(),
                entity.getCanceledAt(),
                entity.getCanceledBy() == null ? null : entity.getCanceledBy().getUsername(),
                entity.getCancelNote(),
                leaveRequestHistoryRepository.findAllByLeaveRequestLeaveRequestIdOrderByChangedAtDescLeaveRequestHistoryIdDesc(entity.getLeaveRequestId())
                        .stream()
                        .map(item -> new LeaveRequestHistoryResponse(
                                item.getLeaveRequestHistoryId(),
                                item.getFromStatus() == null ? null : item.getFromStatus().name(),
                                item.getToStatus().name(),
                                item.getActionCode(),
                                item.getActionNote(),
                                item.getChangedAt(),
                                item.getChangedBy() == null ? null : item.getChangedBy().getUsername()
                        ))
                        .toList()
        );
    }

    private String generateRequestCode(Long employeeId) {
        return "LR-" + employeeId + "-" + System.currentTimeMillis();
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String raw = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + raw + "\"";
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }
}
