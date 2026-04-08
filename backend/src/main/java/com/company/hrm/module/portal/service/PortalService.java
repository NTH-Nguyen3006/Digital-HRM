package com.company.hrm.module.portal.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.hrm.common.constant.*;
import com.company.hrm.common.exception.*;
import com.company.hrm.module.attendance.dto.AttendanceAdjustmentDetailResponse;
import com.company.hrm.module.attendance.dto.AttendanceAdjustmentListItemResponse;
import com.company.hrm.module.attendance.dto.AttendanceLogResponse;
import com.company.hrm.module.attendance.dto.CreateAttendanceAdjustmentRequest;
import com.company.hrm.module.attendance.dto.CreateAttendanceLogRequest;
import com.company.hrm.module.attendance.dto.CreateOvertimeRequest;
import com.company.hrm.module.attendance.dto.OvertimeListItemResponse;
import com.company.hrm.module.attendance.service.AttendanceService;
import com.company.hrm.module.contract.dto.LaborContractListItemResponse;
import com.company.hrm.module.contract.repository.CtLaborContractRepository;
import com.company.hrm.module.contract.service.LaborContractService;
import com.company.hrm.module.employee.dto.EmployeeDetailResponse;
import com.company.hrm.module.employee.dto.EmployeeProfileResponse;
import com.company.hrm.module.employee.dto.ProfileChangeRequestResponse;
import com.company.hrm.module.employee.dto.SubmitProfileChangeRequest;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.employee.service.EmployeeProfileWorkflowService;
import com.company.hrm.module.employee.service.EmployeeService;
import com.company.hrm.module.leave.dto.CreateLeaveRequestRequest;
import com.company.hrm.module.leave.dto.LeaveBalanceResponse;
import com.company.hrm.module.leave.dto.LeaveRequestDetailResponse;
import com.company.hrm.module.leave.service.LeaveService;
import com.company.hrm.module.offboarding.dto.CreateOffboardingRequest;
import com.company.hrm.module.offboarding.dto.OffboardingListItemResponse;
import com.company.hrm.module.offboarding.dto.OffboardingDetailResponse;
import com.company.hrm.module.offboarding.service.OffboardingService;
import com.company.hrm.module.offboarding.service.OffboardingAccessScopeService;
import com.company.hrm.module.payroll.dto.PayrollItemResponse;
import com.company.hrm.module.payroll.dto.SelfPayslipListItemResponse;
import com.company.hrm.module.payroll.service.PayrollService;
import com.company.hrm.module.portal.dto.*;
import com.company.hrm.module.portal.entity.PorPortalInboxItem;
import com.company.hrm.module.portal.repository.PorPortalInboxItemRepository;
import com.company.hrm.module.user.entity.SecUserAccount;

@Service
public class PortalService {

    private final OffboardingAccessScopeService accessScopeService;
    private final EmployeeService employeeService;
    private final EmployeeProfileWorkflowService employeeProfileWorkflowService;
    private final LeaveService leaveService;
    private final AttendanceService attendanceService;
    private final PayrollService payrollService;
    private final LaborContractService laborContractService;
    private final CtLaborContractRepository laborContractRepository;
    private final PorPortalInboxItemRepository portalInboxItemRepository;
    private final OffboardingService offboardingService;

    public PortalService(
            OffboardingAccessScopeService accessScopeService,
            EmployeeService employeeService,
            EmployeeProfileWorkflowService employeeProfileWorkflowService,
            LeaveService leaveService,
            AttendanceService attendanceService,
            PayrollService payrollService,
            LaborContractService laborContractService,
            CtLaborContractRepository laborContractRepository,
            PorPortalInboxItemRepository portalInboxItemRepository,
            OffboardingService offboardingService) {
        this.accessScopeService = accessScopeService;
        this.employeeService = employeeService;
        this.employeeProfileWorkflowService = employeeProfileWorkflowService;
        this.leaveService = leaveService;
        this.attendanceService = attendanceService;
        this.payrollService = payrollService;
        this.laborContractService = laborContractService;
        this.laborContractRepository = laborContractRepository;
        this.portalInboxItemRepository = portalInboxItemRepository;
        this.offboardingService = offboardingService;
    }

    @Transactional(readOnly = true)
    public PortalDashboardResponse getDashboard() {
        HrEmployee employee = accessScopeService.getCurrentEmployeeRequired();
        SecUserAccount user = accessScopeService.getCurrentUserRequired();

        List<LeaveBalanceResponse> balances = leaveService.getMyBalances();
        List<LeaveRequestDetailResponse> leaveRequests = leaveService.listMyRequests();
        List<AttendanceAdjustmentListItemResponse> adjustmentRequests = attendanceService.listMyAdjustmentRequests();
        List<SelfPayslipListItemResponse> payslips = payrollService.listMyPayslips();

        long unreadInboxCount = portalInboxItemRepository
                .countByUserUserIdAndDeletedFalseAndReadAtIsNullAndStatus(user.getUserId(), RecordStatus.ACTIVE);
        long openPortalTaskCount = portalInboxItemRepository
                .countByUserUserIdAndDeletedFalseAndItemTypeAndTaskStatusAndStatus(user.getUserId(),
                        PortalInboxItemType.TASK, PortalTaskStatus.OPEN, RecordStatus.ACTIVE);

        long pendingLeaveRequestCount = leaveRequests.stream()
                .filter(item -> item.requestStatus() != null)
                .filter(item -> {
                    LeaveRequestStatus status = LeaveRequestStatus.valueOf(item.requestStatus());
                    return status == LeaveRequestStatus.DRAFT || status == LeaveRequestStatus.SUBMITTED
                            || status == LeaveRequestStatus.APPROVED;
                }).count();
        long pendingAttendanceAdjustmentCount = adjustmentRequests.stream()
                .filter(item -> item.requestStatus() != null)
                .filter(item -> {
                    AttendanceAdjustmentStatus status = AttendanceAdjustmentStatus.valueOf(item.requestStatus());
                    return status == AttendanceAdjustmentStatus.DRAFT
                            || status == AttendanceAdjustmentStatus.SUBMITTED
                            || status == AttendanceAdjustmentStatus.APPROVED;
                }).count();
        long publishedPayslipCount = payslips.stream()
                .filter(item -> item.itemStatus() != null
                        && PayrollItemStatus.valueOf(item.itemStatus()) == PayrollItemStatus.PUBLISHED)
                .count();
        BigDecimal totalAvailableLeaveUnits = balances.stream()
                .map(LeaveBalanceResponse::availableUnits)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PortalDashboardResponse(
                employee.getEmployeeId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getEmploymentStatus() == null ? null : employee.getEmploymentStatus().name(),
                employee.getOrgUnit() == null ? null : employee.getOrgUnit().getOrgUnitName(),
                employee.getJobTitle() == null ? null : employee.getJobTitle().getJobTitleName(),
                unreadInboxCount,
                openPortalTaskCount,
                balances.size(),
                pendingLeaveRequestCount,
                pendingAttendanceAdjustmentCount,
                publishedPayslipCount,
                totalAvailableLeaveUnits);
    }

    @Transactional(readOnly = true)
    public PortalProfileResponse getMyProfile() {
        HrEmployee employee = accessScopeService.getCurrentEmployeeRequired();
        EmployeeDetailResponse detail = employeeService.getDetail(employee.getEmployeeId());
        EmployeeProfileResponse profile = employeeService.getProfile(employee.getEmployeeId());
        return new PortalProfileResponse(detail, profile);
    }

    @Transactional(readOnly = true)
    public List<ProfileChangeRequestResponse> listMyProfileChangeRequests() {
        return employeeProfileWorkflowService.listMyProfileChangeRequests();
    }

    @Transactional
    public ProfileChangeRequestResponse submitProfileChangeRequestFromPortal(SubmitProfileChangeRequest request) {
        return employeeProfileWorkflowService.submitMyProfileChangeRequest(request);
    }

    @Transactional(readOnly = true)
    public PortalLeaveOverviewResponse getLeaveOverview() {
        List<LeaveBalanceResponse> balances = leaveService.getMyBalances();
        List<LeaveRequestDetailResponse> recentRequests = leaveService.listMyRequests();
        return new PortalLeaveOverviewResponse(balances, recentRequests);
    }

    @Transactional
    public LeaveRequestDetailResponse createLeaveFromPortal(CreateLeaveRequestRequest request) {
        return leaveService.createMyRequest(request);
    }

    @Transactional(readOnly = true)
    public PortalAttendanceOverviewResponse getAttendanceOverview(LocalDate fromDate, LocalDate toDate) {
        LocalDate actualFrom = fromDate == null ? LocalDate.now().withDayOfMonth(1) : fromDate;
        LocalDate actualTo = toDate == null ? LocalDate.now() : toDate;
        List<AttendanceLogResponse> logs = attendanceService.listMyLogs(actualFrom, actualTo);
        List<AttendanceAdjustmentListItemResponse> adjustments = attendanceService.listMyAdjustmentRequests();
        List<OvertimeListItemResponse> otRequests = attendanceService.listMyOvertimeRequests();
        return new PortalAttendanceOverviewResponse(logs, adjustments, otRequests);
    }

    @Transactional
    public AttendanceAdjustmentDetailResponse createAttendanceAdjustmentFromPortal(
            CreateAttendanceAdjustmentRequest request) {
        return attendanceService.createAdjustmentRequest(request);
    }

    @Transactional(readOnly = true)
    public PortalContractOverviewResponse getContractOverview() {
        HrEmployee employee = accessScopeService.getCurrentEmployeeRequired();
        List<LaborContractListItemResponse> items = laborContractService
                .list(null, employee.getEmployeeId(), null, null, null, 0, 50).items();
        return new PortalContractOverviewResponse(items);
    }

    @Transactional(readOnly = true)
    public String exportMyContract(Long laborContractId) {
        HrEmployee employee = accessScopeService.getCurrentEmployeeRequired();
        boolean owned = laborContractRepository.findByLaborContractIdAndDeletedFalse(laborContractId)
                .map(contract -> contract.getEmployee() != null
                        && contract.getEmployee().getEmployeeId().equals(employee.getEmployeeId()))
                .orElse(false);
        if (!owned) {
            throw new ForbiddenException("PORTAL_CONTRACT_SCOPE_DENIED", "Bạn chỉ được xem hợp đồng của chính mình.");
        }
        return laborContractService.exportContractHtml(laborContractId);
    }

    @Transactional(readOnly = true)
    public PortalPayrollOverviewResponse getPayrollOverview() {
        return new PortalPayrollOverviewResponse(payrollService.listMyPayslips());
    }

    @Transactional(readOnly = true)
    public PayrollItemResponse getPayslipDetail(Long payrollPeriodId) {
        return payrollService.getMyPayslip(payrollPeriodId);
    }

    @Transactional(readOnly = true)
    public List<PortalInboxItemResponse> listInbox() {
        SecUserAccount user = accessScopeService.getCurrentUserRequired();
        return portalInboxItemRepository
                .findAllByUserUserIdAndDeletedFalseOrderByCreatedAtDescPortalInboxItemIdDesc(user.getUserId())
                .stream()
                .map(this::toInboxResponse)
                .toList();
    }

    @Transactional
    public PortalInboxItemResponse markInboxRead(Long portalInboxItemId) {
        SecUserAccount user = accessScopeService.getCurrentUserRequired();
        PorPortalInboxItem item = portalInboxItemRepository.findByPortalInboxItemIdAndDeletedFalse(portalInboxItemId)
                .orElseThrow(() -> new NotFoundException("PORTAL_INBOX_ITEM_NOT_FOUND", "Không tìm thấy inbox item."));
        if (!item.getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("PORTAL_INBOX_SCOPE_DENIED",
                    "Bạn chỉ được thao tác trên inbox của chính mình.");
        }
        if (item.getReadAt() == null) {
            item.setReadAt(java.time.LocalDateTime.now());
            portalInboxItemRepository.save(item);
        }
        return toInboxResponse(item);
    }

    @Transactional(readOnly = true)
    public List<PortalInboxItemResponse> listMyTasks() {
        SecUserAccount user = accessScopeService.getCurrentUserRequired();
        return portalInboxItemRepository
                .findAllByUserUserIdAndDeletedFalseAndItemTypeOrderByCreatedAtDescPortalInboxItemIdDesc(
                        user.getUserId(), PortalInboxItemType.TASK)
                .stream()
                .map(this::toInboxResponse)
                .toList();
    }

    @Transactional
    public PortalInboxItemResponse updateTaskStatus(Long portalInboxItemId, PortalTaskStatus status) {
        SecUserAccount user = accessScopeService.getCurrentUserRequired();
        PorPortalInboxItem item = portalInboxItemRepository.findByPortalInboxItemIdAndDeletedFalse(portalInboxItemId)
                .orElseThrow(() -> new NotFoundException("PORTAL_TASK_NOT_FOUND", "Không tìm thấy task."));
        if (!item.getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("PORTAL_TASK_SCOPE_DENIED", "Bạn chỉ được thao tác trên task của chính mình.");
        }
        item.setTaskStatus(status);
        if (status == PortalTaskStatus.DONE) {
            item.setCompletedAt(java.time.LocalDateTime.now());
        }
        return toInboxResponse(portalInboxItemRepository.save(item));
    }

    @Transactional
    public PortalCheckInStatusResponse getCheckInStatus() {
        HrEmployee employee = accessScopeService.getCurrentEmployeeRequired();
        List<AttendanceLogResponse> logs = attendanceService.listMyLogs(LocalDate.now().minusDays(1), LocalDate.now());
        AttendanceLogResponse latest = logs.stream().findFirst().orElse(null);

        boolean canCheckIn = true;
        boolean canCheckOut = false;

        if (latest != null) {
            AttendanceLogEventType type = AttendanceLogEventType.valueOf(latest.eventType());
            if (type == AttendanceLogEventType.CHECK_IN) {
                canCheckIn = false;
                canCheckOut = true;
            }
        }

        return new PortalCheckInStatusResponse(
                latest == null ? null : latest.eventTime(),
                latest == null ? null : latest.eventType(),
                canCheckIn,
                canCheckOut);
    }

    @Transactional
    public AttendanceLogResponse checkIn(CreateAttendanceLogRequest request) {
        return attendanceService.createSelfLog(request, AttendanceLogEventType.CHECK_IN);
    }

    @Transactional
    public AttendanceLogResponse checkOut(CreateAttendanceLogRequest request) {
        return attendanceService.createSelfLog(request, AttendanceLogEventType.CHECK_OUT);
    }

    @Transactional
    public OvertimeListItemResponse submitOvertimeRequest(CreateOvertimeRequest request) {
        return attendanceService.createOvertimeRequest(request);
    }

    @Transactional(readOnly = true)
    public List<OvertimeListItemResponse> listMyOvertimeRequests() {
        return attendanceService.listMyOvertimeRequests();
    }

    @Transactional
    public OffboardingDetailResponse submitResignationRequest(CreateOffboardingRequest request) {
        return offboardingService.createMyRequest(request);
    }

    @Transactional(readOnly = true)
    public List<OffboardingListItemResponse> listMyResignationRequests() {
        return offboardingService.listMyRequests();
    }

    private PortalInboxItemResponse toInboxResponse(PorPortalInboxItem item) {
        return new PortalInboxItemResponse(
                item.getPortalInboxItemId(),
                item.getItemType() == null ? null : item.getItemType().name(),
                item.getCategoryCode(),
                item.getTitle(),
                item.getMessage(),
                item.getRelatedModule(),
                item.getRelatedEntityId(),
                item.getDueAt(),
                item.getReadAt(),
                item.getTaskStatus() == null ? null : item.getTaskStatus().name(),
                item.getCompletedAt(),
                item.getStatus() == null ? null : item.getStatus().name(),
                item.getCreatedAt());
    }
}
