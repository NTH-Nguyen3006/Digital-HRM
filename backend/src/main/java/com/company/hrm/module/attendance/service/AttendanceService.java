package com.company.hrm.module.attendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.hrm.common.constant.AttendanceAdjustmentStatus;
import com.company.hrm.common.constant.AttendanceDailyStatus;
import com.company.hrm.common.constant.AttendanceLogEventType;
import com.company.hrm.common.constant.AttendanceLogSourceType;
import com.company.hrm.common.constant.AttendanceOvertimeStatus;
import com.company.hrm.common.constant.AttendancePeriodStatus;
import com.company.hrm.common.constant.LeaveRequestStatus;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.ForbiddenException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.attendance.dto.AssignShiftRequest;
import com.company.hrm.module.attendance.dto.AttendanceAdjustmentDetailResponse;
import com.company.hrm.module.attendance.dto.AttendanceAdjustmentHistoryResponse;
import com.company.hrm.module.attendance.dto.AttendanceAdjustmentListItemResponse;
import com.company.hrm.module.attendance.dto.AttendanceLogResponse;
import com.company.hrm.module.attendance.dto.AttendancePeriodCloseRequest;
import com.company.hrm.module.attendance.dto.AttendancePeriodReopenRequest;
import com.company.hrm.module.attendance.dto.AttendancePeriodResponse;
import com.company.hrm.module.attendance.dto.CancelAttendanceAdjustmentRequest;
import com.company.hrm.module.attendance.dto.CreateAttendanceAdjustmentRequest;
import com.company.hrm.module.attendance.dto.CreateAttendanceLogRequest;
import com.company.hrm.module.attendance.dto.CreateOvertimeRequest;
import com.company.hrm.module.attendance.dto.DailyAttendanceDetailResponse;
import com.company.hrm.module.attendance.dto.DailyAttendanceListItemResponse;
import com.company.hrm.module.attendance.dto.FinalizeAttendanceAdjustmentRequest;
import com.company.hrm.module.attendance.dto.OvertimeListItemResponse;
import com.company.hrm.module.attendance.dto.ReviewAttendanceAdjustmentRequest;
import com.company.hrm.module.attendance.dto.ReviewOvertimeRequest;
import com.company.hrm.module.attendance.dto.ShiftAssignmentResponse;
import com.company.hrm.module.attendance.dto.ShiftDetailResponse;
import com.company.hrm.module.attendance.dto.ShiftListItemResponse;
import com.company.hrm.module.attendance.dto.ShiftVersionResponse;
import com.company.hrm.module.attendance.dto.UpdateAttendanceAdjustmentRequest;
import com.company.hrm.module.attendance.dto.UpsertShiftRequest;
import com.company.hrm.module.attendance.entity.AttAttendanceAdjustmentHistory;
import com.company.hrm.module.attendance.entity.AttAttendanceAdjustmentRequest;
import com.company.hrm.module.attendance.entity.AttAttendanceLog;
import com.company.hrm.module.attendance.entity.AttAttendancePeriod;
import com.company.hrm.module.attendance.entity.AttDailyAttendance;
import com.company.hrm.module.attendance.entity.AttOvertimeRequest;
import com.company.hrm.module.attendance.entity.AttShift;
import com.company.hrm.module.attendance.entity.AttShiftAssignment;
import com.company.hrm.module.attendance.entity.AttShiftVersion;
import com.company.hrm.module.attendance.repository.AttAttendanceAdjustmentHistoryRepository;
import com.company.hrm.module.attendance.repository.AttAttendanceAdjustmentRequestRepository;
import com.company.hrm.module.attendance.repository.AttAttendanceLogRepository;
import com.company.hrm.module.attendance.repository.AttAttendancePeriodRepository;
import com.company.hrm.module.attendance.repository.AttDailyAttendanceRepository;
import com.company.hrm.module.attendance.repository.AttOvertimeRequestRepository;
import com.company.hrm.module.attendance.repository.AttShiftAssignmentRepository;
import com.company.hrm.module.attendance.repository.AttShiftRepository;
import com.company.hrm.module.attendance.repository.AttShiftVersionRepository;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.leave.repository.LeaLeaveRequestRepository;
import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import com.company.hrm.module.orgunit.repository.HrOrgUnitRepository;
import com.company.hrm.module.storage.service.StorageFileService;
import com.company.hrm.module.systemconfig.entity.SysPlatformSetting;
import com.company.hrm.module.systemconfig.repository.SysPlatformSettingRepository;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.security.SecurityUserContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AttendanceService {

    private static final int DUPLICATE_SELF_LOG_WINDOW_MINUTES = 3;
    private static final String OT_MAX_MINUTES_PER_DAY = "attendance.ot.max_minutes_per_day";
    private static final String OT_MAX_MINUTES_PER_MONTH = "attendance.ot.max_minutes_per_month";

    private final AttShiftRepository shiftRepository;
    private final AttShiftVersionRepository shiftVersionRepository;
    private final AttShiftAssignmentRepository shiftAssignmentRepository;
    private final AttAttendanceLogRepository attendanceLogRepository;
    private final AttAttendanceAdjustmentRequestRepository adjustmentRequestRepository;
    private final AttAttendanceAdjustmentHistoryRepository adjustmentHistoryRepository;
    private final AttOvertimeRequestRepository overtimeRequestRepository;
    private final AttAttendancePeriodRepository attendancePeriodRepository;
    private final AttDailyAttendanceRepository dailyAttendanceRepository;
    private final HrEmployeeRepository employeeRepository;
    private final HrOrgUnitRepository orgUnitRepository;
    private final SecUserAccountRepository userAccountRepository;
    private final AttendanceAccessScopeService accessScopeService;
    private final AuditLogService auditLogService;
    private final StorageFileService storageFileService;
    private final LeaLeaveRequestRepository leaveRequestRepository;
    private final SysPlatformSettingRepository platformSettingRepository;
    private final ObjectMapper objectMapper;

    public AttendanceService(
            AttShiftRepository shiftRepository,
            AttShiftVersionRepository shiftVersionRepository,
            AttShiftAssignmentRepository shiftAssignmentRepository,
            AttAttendanceLogRepository attendanceLogRepository,
            AttAttendanceAdjustmentRequestRepository adjustmentRequestRepository,
            AttAttendanceAdjustmentHistoryRepository adjustmentHistoryRepository,
            AttOvertimeRequestRepository overtimeRequestRepository,
            AttAttendancePeriodRepository attendancePeriodRepository,
            AttDailyAttendanceRepository dailyAttendanceRepository,
            HrEmployeeRepository employeeRepository,
            HrOrgUnitRepository orgUnitRepository,
            SecUserAccountRepository userAccountRepository,
            AttendanceAccessScopeService accessScopeService,
            AuditLogService auditLogService,
            StorageFileService storageFileService,
            LeaLeaveRequestRepository leaveRequestRepository,
            SysPlatformSettingRepository platformSettingRepository,
            ObjectMapper objectMapper) {
        this.shiftRepository = shiftRepository;
        this.shiftVersionRepository = shiftVersionRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
        this.attendanceLogRepository = attendanceLogRepository;
        this.adjustmentRequestRepository = adjustmentRequestRepository;
        this.adjustmentHistoryRepository = adjustmentHistoryRepository;
        this.overtimeRequestRepository = overtimeRequestRepository;
        this.attendancePeriodRepository = attendancePeriodRepository;
        this.dailyAttendanceRepository = dailyAttendanceRepository;
        this.employeeRepository = employeeRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.userAccountRepository = userAccountRepository;
        this.accessScopeService = accessScopeService;
        this.auditLogService = auditLogService;
        this.storageFileService = storageFileService;
        this.leaveRequestRepository = leaveRequestRepository;
        this.platformSettingRepository = platformSettingRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ShiftListItemResponse> listShifts(String keyword, RecordStatus status, int page, int size) {
        Specification<AttShift> specification = (root, query, builder) -> builder.isFalse(root.get("deleted"));
        if (keyword != null && !keyword.isBlank()) {
            String likeValue = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("shiftCode")), likeValue),
                    builder.like(builder.lower(root.get("shiftName")), likeValue)));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }
        Page<AttShift> result = shiftRepository.findAll(specification,
                PageRequest.of(page, size, Sort.by("sortOrder").ascending().and(Sort.by("shiftCode").ascending())));
        List<ShiftListItemResponse> items = result.getContent().stream().map(this::toShiftListItem).toList();
        return toPageResponse(result, items, page, size);
    }

    @Transactional(readOnly = true)
    public ShiftDetailResponse getShiftDetail(Long shiftId) {
        AttShift shift = getShift(shiftId);
        return toShiftDetail(shift);
    }

    @Transactional
    public ShiftDetailResponse upsertShift(Long shiftId, UpsertShiftRequest request) {
        validateShiftRequest(request);
        AttShift shift;
        ShiftDetailResponse oldSnapshot = null;
        if (shiftId == null) {
            if (shiftRepository.existsByShiftCodeIgnoreCaseAndDeletedFalse(request.shiftCode().trim())) {
                throw new BusinessException("ATTENDANCE_SHIFT_CODE_EXISTS", "Mã ca làm việc đã tồn tại.",
                        HttpStatus.CONFLICT);
            }
            shift = new AttShift();
            shift.setShiftCode(request.shiftCode().trim().toUpperCase(Locale.ROOT));
        } else {
            shift = getShift(shiftId);
            oldSnapshot = toShiftDetail(shift);
            if (shiftRepository.existsByShiftCodeIgnoreCaseAndDeletedFalseAndShiftIdNot(request.shiftCode().trim(),
                    shiftId)) {
                throw new BusinessException("ATTENDANCE_SHIFT_CODE_EXISTS", "Mã ca làm việc đã tồn tại.",
                        HttpStatus.CONFLICT);
            }
            if (!request.effectiveFrom().isAfter(LocalDate.now())) {
                throw new BusinessException("ATTENDANCE_SHIFT_RETROACTIVE_BLOCKED",
                        "Chỉ được tạo version ca làm việc có hiệu lực trong tương lai để tránh sửa dữ liệu retroactive.",
                        HttpStatus.CONFLICT);
            }
        }

        shift.setShiftCode(request.shiftCode().trim().toUpperCase(Locale.ROOT));
        shift.setShiftName(request.shiftName().trim());
        shift.setDescription(trimToNull(request.description()));
        shift.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        shift.setStatus(RecordStatus.ACTIVE);
        shift = shiftRepository.save(shift);

        Integer currentMaxVersionNo = shiftVersionRepository.findMaxVersionNoByShiftId(shift.getShiftId());
        AttShiftVersion latestVersion = shiftVersionRepository
                .findAllByShiftShiftIdAndDeletedFalseOrderByVersionNoDesc(shift.getShiftId()).stream().findFirst()
                .orElse(null);
        if (latestVersion != null) {
            if (!request.effectiveFrom().isAfter(latestVersion.getEffectiveFrom())) {
                throw new BusinessException("ATTENDANCE_SHIFT_EFFECTIVE_FROM_INVALID",
                        "Ngày hiệu lực version mới phải lớn hơn version gần nhất.", HttpStatus.BAD_REQUEST);
            }
            latestVersion.setEffectiveTo(request.effectiveFrom().minusDays(1));
            shiftVersionRepository.save(latestVersion);
        }

        AttShiftVersion version = new AttShiftVersion();
        version.setShift(shift);
        version.setVersionNo(currentMaxVersionNo == null ? 1 : currentMaxVersionNo + 1);
        version.setEffectiveFrom(request.effectiveFrom());
        version.setEffectiveTo(null);
        version.setStartTime(request.startTime());
        version.setEndTime(request.endTime());
        version.setCrossesMidnight(request.crossesMidnight());
        version.setBreakMinutes(request.breakMinutes());
        version.setLateGraceMinutes(request.lateGraceMinutes());
        version.setEarlyLeaveGraceMinutes(request.earlyLeaveGraceMinutes());
        version.setOtAllowed(request.otAllowed());
        version.setNightShift(request.nightShift());
        version.setMinWorkMinutesForPresent(request.minWorkMinutesForPresent());
        version.setStatus(RecordStatus.ACTIVE);
        version.setNote(trimToNull(request.note()));
        shiftVersionRepository.save(version);

        ShiftDetailResponse response = toShiftDetail(shift);
        auditLogService.logSuccess(shiftId == null ? "CREATE" : "UPDATE", "ATTENDANCE_SHIFT", "att_shift",
                shift.getShiftId().toString(), oldSnapshot, response,
                shiftId == null ? "Tạo ca làm việc mới." : "Cập nhật ca làm việc và version.");
        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<ShiftAssignmentResponse> listAssignments(Long employeeId, Long orgUnitId, Long shiftId,
            LocalDate attendanceDate, int page, int size) {
        Specification<AttShiftAssignment> specification = (root, query, builder) -> builder
                .isFalse(root.get("deleted"));
        if (employeeId != null) {
            specification = specification
                    .and((root, query, builder) -> builder.equal(root.join("employee").get("employeeId"), employeeId));
        }
        if (orgUnitId != null) {
            HrOrgUnit orgUnit = getOrgUnit(orgUnitId);
            String pathCode = orgUnit.getPathCode();
            specification = specification.and((root, query, builder) -> builder
                    .like(root.join("employee").join("orgUnit").get("pathCode"), pathCode + "%"));
        }
        if (shiftId != null) {
            specification = specification
                    .and((root, query, builder) -> builder.equal(root.join("shift").get("shiftId"), shiftId));
        }
        if (attendanceDate != null) {
            specification = specification.and((root, query, builder) -> builder.and(
                    builder.lessThanOrEqualTo(root.get("effectiveFrom"), attendanceDate),
                    builder.or(builder.isNull(root.get("effectiveTo")),
                            builder.greaterThanOrEqualTo(root.get("effectiveTo"), attendanceDate))));
        }
        Page<AttShiftAssignment> result = shiftAssignmentRepository.findAll(specification, PageRequest.of(page, size,
                Sort.by("effectiveFrom").descending().and(Sort.by("shiftAssignmentId").descending())));
        List<ShiftAssignmentResponse> items = result.getContent().stream().map(this::toShiftAssignmentResponse)
                .toList();
        return toPageResponse(result, items, page, size);
    }

    @Transactional
    public List<ShiftAssignmentResponse> assignShift(AssignShiftRequest request) {
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new BusinessException("ATTENDANCE_ASSIGNMENT_DATE_INVALID",
                    "effectiveTo không được nhỏ hơn effectiveFrom.", HttpStatus.BAD_REQUEST);
        }
        AttShift shift = getShift(request.shiftId());
        LocalDate effectiveTo = request.effectiveTo() == null ? LocalDate.of(9999, 12, 31) : request.effectiveTo();
        List<HrEmployee> employees = employeeRepository.findAllByEmployeeIdInAndDeletedFalse(request.employeeIds());
        if (employees.size() != request.employeeIds().size()) {
            throw new BusinessException("ATTENDANCE_ASSIGNMENT_EMPLOYEE_NOT_FOUND",
                    "Có nhân viên trong danh sách phân ca không tồn tại.", HttpStatus.BAD_REQUEST);
        }

        List<ShiftAssignmentResponse> responses = new ArrayList<>();
        for (HrEmployee employee : employees) {
            if (shiftAssignmentRepository.existsOverlappingAssignment(employee.getEmployeeId(), null,
                    request.effectiveFrom(), effectiveTo)) {
                throw new BusinessException("ATTENDANCE_ASSIGNMENT_OVERLAP", "Nhân viên " + employee.getEmployeeCode()
                        + " đã có phân ca hiệu lực trong khoảng thời gian này.", HttpStatus.CONFLICT);
            }
            AttShiftAssignment entity = new AttShiftAssignment();
            entity.setEmployee(employee);
            entity.setShift(shift);
            entity.setEffectiveFrom(request.effectiveFrom());
            entity.setEffectiveTo(request.effectiveTo());
            entity.setAssignmentNote(trimToNull(request.assignmentNote()));
            entity.setAssignmentBatchRef(trimToNull(request.assignmentBatchRef()));
            entity = shiftAssignmentRepository.save(entity);
            responses.add(toShiftAssignmentResponse(entity));
            auditLogService.logSuccess("ASSIGN", "ATTENDANCE_SHIFT_ASSIGNMENT", "att_shift_assignment",
                    entity.getShiftAssignmentId().toString(),
                    null, responses.get(responses.size() - 1), "Gán ca cho nhân viên.");
        }
        return responses;
    }

    @Transactional
    public AttendanceLogResponse createSelfLog(CreateAttendanceLogRequest request, AttendanceLogEventType eventType) {
        HrEmployee employee = accessScopeService.getCurrentEmployeeRequired();
        validateSelfLogSource(request.sourceType());
        LocalDateTime eventTime = LocalDateTime.now();
        LocalDate attendanceDate = request.attendanceDate() == null ? eventTime.toLocalDate()
                : request.attendanceDate();
        if (attendanceDate.isAfter(LocalDate.now()) || attendanceDate.isBefore(LocalDate.now().minusDays(1))) {
            throw new BusinessException("ATTENDANCE_SELF_LOG_DATE_INVALID",
                    "Nhân viên chỉ được tự chấm công cho hôm nay hoặc ngày công hôm trước trong trường hợp ca qua đêm.",
                    HttpStatus.BAD_REQUEST);
        }
        assertPeriodOpen(attendanceDate);
        List<AttAttendanceLog> existingLogs = attendanceLogRepository
                .findAllByEmployeeEmployeeIdAndAttendanceDateAndDeletedFalseOrderByEventTimeAscAttendanceLogIdAsc(
                        employee.getEmployeeId(), attendanceDate);
        existingLogs.stream()
                .filter(log -> log.getEventType() == eventType)
                .map(AttAttendanceLog::getEventTime)
                .max(LocalDateTime::compareTo)
                .ifPresent(lastEventTime -> {
                    long minutes = Math.abs(ChronoUnit.MINUTES.between(lastEventTime, eventTime));
                    if (minutes < DUPLICATE_SELF_LOG_WINDOW_MINUTES) {
                        throw new BusinessException("ATTENDANCE_DUPLICATE_SELF_LOG",
                                "Bạn vừa ghi nhận thao tác này rồi. Vui lòng chờ thêm trước khi thao tác lại.",
                                HttpStatus.CONFLICT);
                    }
                });

        AttShiftAssignment assignment = shiftAssignmentRepository
                .findEffectiveAssignment(employee.getEmployeeId(), attendanceDate).orElse(null);
        AttAttendanceLog entity = new AttAttendanceLog();
        entity.setEmployee(employee);
        entity.setAttendanceDate(attendanceDate);
        entity.setEventType(eventType);
        entity.setEventTime(eventTime);
        entity.setSourceType(request.sourceType());
        entity.setShiftAssignment(assignment);
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        entity.setDeviceRef(trimToNull(request.deviceRef()));
        entity.setNote(trimToNull(request.note()));
        entity = attendanceLogRepository.save(entity);

        recalculateDailyAttendance(employee, attendanceDate, null);
        AttendanceLogResponse response = toAttendanceLogResponse(entity);
        auditLogService.logSuccess(eventType == AttendanceLogEventType.CHECK_IN ? "CHECK_IN" : "CHECK_OUT",
                "ATTENDANCE_LOG", "att_attendance_log", entity.getAttendanceLogId().toString(), null, response,
                "Nhân viên tự chấm công.");
        return response;
    }

    @Transactional(readOnly = true)
    public List<AttendanceLogResponse> listMyLogs(LocalDate fromDate, LocalDate toDate) {
        HrEmployee employee = accessScopeService.getCurrentEmployeeRequired();
        validateDateRange(fromDate, toDate);
        return attendanceLogRepository
                .findAllByEmployeeEmployeeIdAndAttendanceDateBetweenAndDeletedFalseOrderByEventTimeDescAttendanceLogIdDesc(
                        employee.getEmployeeId(), fromDate, toDate)
                .stream()
                .map(this::toAttendanceLogResponse)
                .toList();
    }

    @Transactional
    public PageResponse<DailyAttendanceListItemResponse> listDailyAttendance(LocalDate fromDate, LocalDate toDate,
            Long orgUnitId, Long employeeId, Long shiftId, int page, int size) {
        validateDateRange(fromDate, toDate);
        ensureDailySummaries(fromDate, toDate, orgUnitId, employeeId);

        Specification<AttDailyAttendance> specification = (root, query, builder) -> builder
                .isFalse(root.get("deleted"));
        specification = specification
                .and((root, query, builder) -> builder.between(root.get("attendanceDate"), fromDate, toDate));
        if (employeeId != null) {
            specification = specification
                    .and((root, query, builder) -> builder.equal(root.join("employee").get("employeeId"), employeeId));
        }
        if (orgUnitId != null) {
            HrOrgUnit orgUnit = getOrgUnit(orgUnitId);
            specification = specification.and((root, query, builder) -> builder
                    .like(root.join("employee").join("orgUnit").get("pathCode"), orgUnit.getPathCode() + "%"));
        }
        if (shiftId != null) {
            specification = specification.and((root, query, builder) -> builder
                    .equal(root.join("shiftAssignment").join("shift").get("shiftId"), shiftId));
        }
        Page<AttDailyAttendance> result = dailyAttendanceRepository.findAll(specification,
                PageRequest.of(page, size,
                        Sort.by("attendanceDate").descending().and(Sort.by("employee.employeeCode").ascending())));
        List<DailyAttendanceListItemResponse> items = result.getContent().stream().map(this::toDailyAttendanceListItem)
                .toList();
        return toPageResponse(result, items, page, size);
    }

    @Transactional
    public DailyAttendanceDetailResponse getDailyAttendanceDetail(Long employeeId, LocalDate attendanceDate) {
        HrEmployee employee = getEmployee(employeeId);
        ensureDailySummaries(attendanceDate, attendanceDate, null, employeeId);
        AttDailyAttendance dailyAttendance = dailyAttendanceRepository
                .findByEmployeeEmployeeIdAndAttendanceDateAndDeletedFalse(employeeId, attendanceDate)
                .orElseThrow(() -> new NotFoundException("ATTENDANCE_DAILY_NOT_FOUND",
                        "Không tìm thấy bản ghi bảng công ngày."));
        List<AttendanceLogResponse> logs = attendanceLogRepository
                .findAllByEmployeeEmployeeIdAndAttendanceDateAndDeletedFalseOrderByEventTimeAscAttendanceLogIdAsc(
                        employeeId, attendanceDate)
                .stream().map(this::toAttendanceLogResponse).toList();
        List<AttendanceAdjustmentListItemResponse> adjustments = adjustmentRequestRepository
                .findAll((root, query, builder) -> builder.and(
                        builder.isFalse(root.get("deleted")),
                        builder.equal(root.join("employee").get("employeeId"), employeeId),
                        builder.equal(root.get("attendanceDate"), attendanceDate)))
                .stream()
                .sorted(Comparator.comparing(AttAttendanceAdjustmentRequest::getCreatedAt).reversed())
                .map(this::toAdjustmentListItem)
                .toList();
        List<OvertimeListItemResponse> overtimeRequests = overtimeRequestRepository
                .findAll((root, query, builder) -> builder.and(
                        builder.isFalse(root.get("deleted")),
                        builder.equal(root.join("employee").get("employeeId"), employeeId),
                        builder.equal(root.get("attendanceDate"), attendanceDate)))
                .stream()
                .sorted(Comparator.comparing(AttOvertimeRequest::getCreatedAt).reversed())
                .map(this::toOvertimeListItem)
                .toList();
        return new DailyAttendanceDetailResponse(toDailyAttendanceListItem(dailyAttendance), logs, adjustments,
                overtimeRequests);
    }

    @Transactional(readOnly = true)
    public List<AttendanceAdjustmentListItemResponse> listMyAdjustmentRequests() {
        HrEmployee employee = accessScopeService.getCurrentEmployeeRequired();
        return adjustmentRequestRepository
                .findAllByEmployeeEmployeeIdAndDeletedFalseOrderByCreatedAtDescAdjustmentRequestIdDesc(
                        employee.getEmployeeId())
                .stream().map(this::toAdjustmentListItem).toList();
    }

    @Transactional
    public AttendanceAdjustmentDetailResponse createAdjustmentRequest(CreateAttendanceAdjustmentRequest request) {
        HrEmployee employee = accessScopeService.getCurrentEmployeeRequired();
        assertPeriodOpen(request.attendanceDate());
        validateAdjustmentRequestTimes(request.attendanceDate(), request.proposedCheckInAt(),
                request.proposedCheckOutAt());
        requireEvidenceIfPresent(request.evidenceFileKey(), "ATTENDANCE_ADJUSTMENT_EVIDENCE_INVALID",
                "File minh chứng điều chỉnh công không hợp lệ.");

        AttAttendanceAdjustmentRequest entity = new AttAttendanceAdjustmentRequest();
        entity.setRequestCode(generateRequestCode("ADJ", employee.getEmployeeId()));
        entity.setEmployee(employee);
        entity.setAttendanceDate(request.attendanceDate());
        entity.setIssueType(request.issueType());
        entity.setProposedCheckInAt(request.proposedCheckInAt());
        entity.setProposedCheckOutAt(request.proposedCheckOutAt());
        entity.setReason(request.reason().trim());
        entity.setEvidenceFileKey(trimToNull(request.evidenceFileKey()));
        entity.setRequestStatus(
                request.submit() ? AttendanceAdjustmentStatus.SUBMITTED : AttendanceAdjustmentStatus.DRAFT);
        entity.setSubmittedAt(LocalDateTime.now());
        if (request.copyFromAdjustmentRequestId() != null) {
            AttAttendanceAdjustmentRequest copied = getAdjustmentRequest(request.copyFromAdjustmentRequestId());
            if (!copied.getEmployee().getEmployeeId().equals(employee.getEmployeeId())) {
                throw new ForbiddenException("ATTENDANCE_ADJUSTMENT_COPY_SCOPE_DENIED",
                        "Bạn chỉ được sao chép yêu cầu điều chỉnh công của chính mình.");
            }
            entity.setCopiedFromAdjustmentRequest(copied);
        }
        entity = adjustmentRequestRepository.save(entity);
        appendAdjustmentHistory(entity, null, entity.getRequestStatus(), request.submit() ? "SUBMIT" : "DRAFT_SAVE",
                entity.getReason());
        AttendanceAdjustmentDetailResponse response = toAdjustmentDetail(entity);
        auditLogService.logSuccess("CREATE", "ATTENDANCE_ADJUSTMENT", "att_adjustment_request",
                entity.getAdjustmentRequestId().toString(), null, response, "Tạo yêu cầu điều chỉnh công.");
        return response;
    }

    @Transactional
    public AttendanceAdjustmentDetailResponse updateAdjustmentRequest(Long adjustmentRequestId,
            UpdateAttendanceAdjustmentRequest request) {
        AttAttendanceAdjustmentRequest entity = getAdjustmentRequest(adjustmentRequestId);
        assertSelfAdjustmentOwner(entity);
        if (entity.getRequestStatus() != AttendanceAdjustmentStatus.DRAFT
                && entity.getRequestStatus() != AttendanceAdjustmentStatus.SUBMITTED) {
            throw new BusinessException("ATTENDANCE_ADJUSTMENT_UPDATE_STATUS_INVALID",
                    "Chỉ được sửa yêu cầu điều chỉnh công ở trạng thái nháp hoặc chờ duyệt.", HttpStatus.CONFLICT);
        }
        assertPeriodOpen(entity.getAttendanceDate());
        assertPeriodOpen(request.attendanceDate());
        validateAdjustmentRequestTimes(request.attendanceDate(), request.proposedCheckInAt(),
                request.proposedCheckOutAt());
        requireEvidenceIfPresent(request.evidenceFileKey(), "ATTENDANCE_ADJUSTMENT_EVIDENCE_INVALID",
                "File minh chứng điều chỉnh công không hợp lệ.");

        AttendanceAdjustmentDetailResponse oldSnapshot = toAdjustmentDetail(entity);
        AttendanceAdjustmentStatus oldStatus = entity.getRequestStatus();
        entity.setAttendanceDate(request.attendanceDate());
        entity.setIssueType(request.issueType());
        entity.setProposedCheckInAt(request.proposedCheckInAt());
        entity.setProposedCheckOutAt(request.proposedCheckOutAt());
        entity.setReason(request.reason().trim());
        entity.setEvidenceFileKey(trimToNull(request.evidenceFileKey()));
        entity.setRequestStatus(
                request.submit() ? AttendanceAdjustmentStatus.SUBMITTED : AttendanceAdjustmentStatus.DRAFT);
        entity = adjustmentRequestRepository.save(entity);
        appendAdjustmentHistory(entity, oldStatus, entity.getRequestStatus(), "UPDATE", entity.getReason());
        AttendanceAdjustmentDetailResponse response = toAdjustmentDetail(entity);
        auditLogService.logSuccess("UPDATE", "ATTENDANCE_ADJUSTMENT", "att_adjustment_request",
                entity.getAdjustmentRequestId().toString(), oldSnapshot, response, "Cập nhật yêu cầu điều chỉnh công.");
        return response;
    }

    @Transactional
    public AttendanceAdjustmentDetailResponse cancelAdjustmentRequest(Long adjustmentRequestId,
            CancelAttendanceAdjustmentRequest request) {
        AttAttendanceAdjustmentRequest entity = getAdjustmentRequest(adjustmentRequestId);
        assertSelfAdjustmentOwner(entity);
        if (entity.getRequestStatus() != AttendanceAdjustmentStatus.DRAFT
                && entity.getRequestStatus() != AttendanceAdjustmentStatus.SUBMITTED) {
            throw new BusinessException("ATTENDANCE_ADJUSTMENT_CANCEL_STATUS_INVALID",
                    "Chỉ được hủy yêu cầu điều chỉnh công ở trạng thái nháp hoặc chờ duyệt.", HttpStatus.CONFLICT);
        }
        assertPeriodOpen(entity.getAttendanceDate());

        AttendanceAdjustmentDetailResponse oldSnapshot = toAdjustmentDetail(entity);
        AttendanceAdjustmentStatus oldStatus = entity.getRequestStatus();
        entity.setRequestStatus(AttendanceAdjustmentStatus.CANCELLED);
        entity.setCanceledAt(LocalDateTime.now());
        entity.setCanceledBy(getCurrentUserOrNull());
        entity.setCancelNote(request.cancelNote().trim());
        entity = adjustmentRequestRepository.save(entity);
        appendAdjustmentHistory(entity, oldStatus, AttendanceAdjustmentStatus.CANCELLED, "CANCEL",
                request.cancelNote());
        AttendanceAdjustmentDetailResponse response = toAdjustmentDetail(entity);
        auditLogService.logSuccess("CANCEL", "ATTENDANCE_ADJUSTMENT", "att_adjustment_request",
                entity.getAdjustmentRequestId().toString(), oldSnapshot, response, request.cancelNote());
        return response;
    }

    @Transactional(readOnly = true)
    public List<AttendanceAdjustmentListItemResponse> listPendingAdjustmentRequestsForManager() {
        String orgPathPrefix = accessScopeService.getManagerOrgPathPrefix()
                .orElseThrow(() -> new ForbiddenException("ATTENDANCE_MANAGER_SCOPE_REQUIRED",
                        "Bạn không thuộc phạm vi quản lý để xem yêu cầu điều chỉnh công."));
        return adjustmentRequestRepository
                .findPendingByManagerScope(AttendanceAdjustmentStatus.SUBMITTED, orgPathPrefix)
                .stream().map(this::toAdjustmentListItem).toList();
    }

    @Transactional
    public AttendanceAdjustmentDetailResponse reviewAdjustmentByManager(Long adjustmentRequestId,
            ReviewAttendanceAdjustmentRequest request) {
        AttAttendanceAdjustmentRequest entity = getAdjustmentRequest(adjustmentRequestId);
        accessScopeService.assertManagerCanAccessEmployee(entity.getEmployee());
        if (entity.getRequestStatus() != AttendanceAdjustmentStatus.SUBMITTED) {
            throw new BusinessException("ATTENDANCE_ADJUSTMENT_REVIEW_STATUS_INVALID",
                    "Yêu cầu điều chỉnh công không còn ở trạng thái chờ duyệt.", HttpStatus.CONFLICT);
        }
        AttendanceAdjustmentDetailResponse oldSnapshot = toAdjustmentDetail(entity);
        AttendanceAdjustmentStatus targetStatus = request.approved() ? AttendanceAdjustmentStatus.APPROVED
                : AttendanceAdjustmentStatus.REJECTED;
        if (!request.approved() && trimToNull(request.note()) == null) {
            throw new BusinessException("ATTENDANCE_ADJUSTMENT_REJECT_NOTE_REQUIRED",
                    "Khi từ chối yêu cầu điều chỉnh công, bắt buộc nhập lý do.", HttpStatus.BAD_REQUEST);
        }
        if (request.approved()) {
            entity.setApprovedAt(LocalDateTime.now());
            entity.setApprovedBy(getCurrentUserOrNull());
            entity.setManagerNote(trimToNull(request.note()));
        } else {
            entity.setRejectedAt(LocalDateTime.now());
            entity.setRejectedBy(getCurrentUserOrNull());
            entity.setRejectionNote(trimToNull(request.note()));
        }
        AttendanceAdjustmentStatus fromStatus = entity.getRequestStatus();
        entity.setRequestStatus(targetStatus);
        entity = adjustmentRequestRepository.save(entity);
        appendAdjustmentHistory(entity, fromStatus, targetStatus,
                request.approved() ? "MANAGER_APPROVE" : "MANAGER_REJECT", request.note());
        AttendanceAdjustmentDetailResponse response = toAdjustmentDetail(entity);
        auditLogService.logSuccess(request.approved() ? "APPROVE" : "REJECT", "ATTENDANCE_ADJUSTMENT",
                "att_adjustment_request",
                entity.getAdjustmentRequestId().toString(), oldSnapshot, response, request.note());
        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceAdjustmentListItemResponse> listAdminAdjustmentRequests(
            String keyword,
            AttendanceAdjustmentStatus status,
            Long employeeId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size) {
        Specification<AttAttendanceAdjustmentRequest> specification = (root, query, builder) -> builder
                .isFalse(root.get("deleted"));
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("requestCode")), like),
                    builder.like(builder.lower(root.join("employee").get("employeeCode")), like),
                    builder.like(builder.lower(root.join("employee").get("fullName")), like)));
        }
        if (status != null) {
            specification = specification
                    .and((root, query, builder) -> builder.equal(root.get("requestStatus"), status));
        }
        if (employeeId != null) {
            specification = specification
                    .and((root, query, builder) -> builder.equal(root.join("employee").get("employeeId"), employeeId));
        }
        if (fromDate != null) {
            specification = specification
                    .and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("attendanceDate"), fromDate));
        }
        if (toDate != null) {
            specification = specification
                    .and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("attendanceDate"), toDate));
        }
        Page<AttAttendanceAdjustmentRequest> result = adjustmentRequestRepository.findAll(specification,
                PageRequest.of(page, size,
                        Sort.by("createdAt").descending().and(Sort.by("adjustmentRequestId").descending())));
        List<AttendanceAdjustmentListItemResponse> items = result.getContent().stream().map(this::toAdjustmentListItem)
                .toList();
        return toPageResponse(result, items, page, size);
    }

    @Transactional(readOnly = true)
    public AttendanceAdjustmentDetailResponse getAdjustmentDetail(Long adjustmentRequestId) {
        return toAdjustmentDetail(getAdjustmentRequest(adjustmentRequestId));
    }

    @Transactional
    public AttendanceAdjustmentDetailResponse finalizeAdjustment(Long adjustmentRequestId,
            FinalizeAttendanceAdjustmentRequest request) {
        AttAttendanceAdjustmentRequest entity = getAdjustmentRequest(adjustmentRequestId);
        if (entity.getRequestStatus() != AttendanceAdjustmentStatus.APPROVED) {
            throw new BusinessException("ATTENDANCE_ADJUSTMENT_FINALIZE_STATUS_INVALID",
                    "Chỉ yêu cầu điều chỉnh công đã được duyệt mới được chốt.", HttpStatus.CONFLICT);
        }
        assertPeriodOpen(entity.getAttendanceDate());
        AttendanceAdjustmentDetailResponse oldSnapshot = toAdjustmentDetail(entity);
        AttendanceAdjustmentStatus fromStatus = entity.getRequestStatus();

        if (!request.approved()) {
            if (trimToNull(request.note()) == null) {
                throw new BusinessException("ATTENDANCE_ADJUSTMENT_FINALIZE_NOTE_REQUIRED",
                        "Khi từ chối chốt điều chỉnh công, bắt buộc nhập lý do.", HttpStatus.BAD_REQUEST);
            }
            entity.setRequestStatus(AttendanceAdjustmentStatus.REJECTED);
            entity.setRejectedAt(LocalDateTime.now());
            entity.setRejectedBy(getCurrentUserOrNull());
            entity.setRejectionNote(trimToNull(request.note()));
            entity = adjustmentRequestRepository.save(entity);
            appendAdjustmentHistory(entity, fromStatus, AttendanceAdjustmentStatus.REJECTED, "HR_REJECT",
                    request.note());
        } else {
            entity.setRequestStatus(AttendanceAdjustmentStatus.FINALIZED);
            entity.setFinalizedAt(LocalDateTime.now());
            entity.setFinalizedBy(getCurrentUserOrNull());
            entity.setFinalizeNote(trimToNull(request.note()));
            entity = adjustmentRequestRepository.save(entity);
            createSyntheticLogsFromAdjustment(entity);
            recalculateDailyAttendance(entity.getEmployee(), entity.getAttendanceDate(), null);
            appendAdjustmentHistory(entity, fromStatus, AttendanceAdjustmentStatus.FINALIZED, "HR_FINALIZE",
                    request.note());
        }

        AttendanceAdjustmentDetailResponse response = toAdjustmentDetail(entity);
        auditLogService.logSuccess(request.approved() ? "FINALIZE" : "REJECT", "ATTENDANCE_ADJUSTMENT",
                "att_adjustment_request",
                entity.getAdjustmentRequestId().toString(), oldSnapshot, response, request.note());
        return response;
    }

    @Transactional(readOnly = true)
    public List<OvertimeListItemResponse> listMyOvertimeRequests() {
        HrEmployee employee = accessScopeService.getCurrentEmployeeRequired();
        return overtimeRequestRepository
                .findAllByEmployeeEmployeeIdAndDeletedFalseOrderByCreatedAtDescOvertimeRequestIdDesc(
                        employee.getEmployeeId())
                .stream().map(this::toOvertimeListItem).toList();
    }

    @Transactional
    public OvertimeListItemResponse createOvertimeRequest(CreateOvertimeRequest request) {
        HrEmployee employee = accessScopeService.getCurrentEmployeeRequired();
        assertPeriodOpen(request.attendanceDate());
        validateOvertimeRequest(request.attendanceDate(), request.overtimeStartAt(), request.overtimeEndAt());
        requireEvidenceIfPresent(request.evidenceFileKey(), "ATTENDANCE_OT_EVIDENCE_INVALID",
                "File minh chứng OT không hợp lệ.");

        ScheduledShiftWindow shiftWindow = getScheduledShiftWindow(employee, request.attendanceDate()).orElse(null);
        if (shiftWindow != null && overlaps(request.overtimeStartAt(), request.overtimeEndAt(),
                shiftWindow.plannedStartAt(), shiftWindow.plannedEndAt())) {
            throw new BusinessException("ATTENDANCE_OT_OVERLAP_SHIFT",
                    "Khung giờ OT bị chồng lấn với ca làm việc đã phân.", HttpStatus.BAD_REQUEST);
        }

        AttOvertimeRequest entity = new AttOvertimeRequest();
        entity.setRequestCode(generateRequestCode("OT", employee.getEmployeeId()));
        entity.setEmployee(employee);
        entity.setAttendanceDate(request.attendanceDate());
        entity.setOvertimeStartAt(request.overtimeStartAt());
        entity.setOvertimeEndAt(request.overtimeEndAt());
        entity.setRequestedMinutes(
                (int) ChronoUnit.MINUTES.between(request.overtimeStartAt(), request.overtimeEndAt()));
        entity.setReason(request.reason().trim());
        entity.setEvidenceFileKey(trimToNull(request.evidenceFileKey()));
        entity.setRequestStatus(AttendanceOvertimeStatus.SUBMITTED);
        entity.setSubmittedAt(LocalDateTime.now());
        entity = overtimeRequestRepository.save(entity);
        OvertimeListItemResponse response = toOvertimeListItem(entity);
        auditLogService.logSuccess("CREATE", "ATTENDANCE_OT", "att_overtime_request",
                entity.getOvertimeRequestId().toString(), null, response, "Tạo yêu cầu OT.");
        return response;
    }

    @Transactional(readOnly = true)
    public List<OvertimeListItemResponse> listPendingOvertimeRequestsForManager() {
        String orgPathPrefix = accessScopeService.getManagerOrgPathPrefix()
                .orElseThrow(() -> new ForbiddenException("ATTENDANCE_MANAGER_SCOPE_REQUIRED",
                        "Bạn không thuộc phạm vi quản lý để xem yêu cầu OT."));
        return overtimeRequestRepository.findPendingByManagerScope(AttendanceOvertimeStatus.SUBMITTED, orgPathPrefix)
                .stream().map(this::toOvertimeListItem).toList();
    }

    @Transactional
    public OvertimeListItemResponse reviewOvertimeByManager(Long overtimeRequestId, ReviewOvertimeRequest request) {
        AttOvertimeRequest entity = getOvertimeRequest(overtimeRequestId);
        accessScopeService.assertManagerCanAccessEmployee(entity.getEmployee());
        if (entity.getRequestStatus() != AttendanceOvertimeStatus.SUBMITTED) {
            throw new BusinessException("ATTENDANCE_OT_REVIEW_STATUS_INVALID",
                    "Yêu cầu OT không còn ở trạng thái chờ duyệt.", HttpStatus.CONFLICT);
        }

        OvertimeListItemResponse oldSnapshot = toOvertimeListItem(entity);
        if (!request.approved() && trimToNull(request.note()) == null) {
            throw new BusinessException("ATTENDANCE_OT_REJECT_NOTE_REQUIRED",
                    "Khi từ chối yêu cầu OT, bắt buộc nhập lý do.", HttpStatus.BAD_REQUEST);
        }
        if (request.approved()) {
            int dailyLimit = getIntegerSetting(OT_MAX_MINUTES_PER_DAY, 240);
            int monthlyLimit = getIntegerSetting(OT_MAX_MINUTES_PER_MONTH, 1200);
            int approvedDaily = zeroIfNull(overtimeRequestRepository.sumApprovedMinutesByEmployeeAndDate(
                    entity.getEmployee().getEmployeeId(), entity.getAttendanceDate(), AttendanceOvertimeStatus.APPROVED,
                    entity.getOvertimeRequestId()));
            YearMonth ym = YearMonth.of(entity.getAttendanceDate().getYear(),
                    entity.getAttendanceDate().getMonthValue());
            int approvedMonth = zeroIfNull(overtimeRequestRepository.sumApprovedMinutesByEmployeeAndMonth(
                    entity.getEmployee().getEmployeeId(), ym.getYear(), ym.getMonthValue(),
                    AttendanceOvertimeStatus.APPROVED, entity.getOvertimeRequestId()));
            if (approvedDaily + entity.getRequestedMinutes() > dailyLimit) {
                throw new BusinessException("ATTENDANCE_OT_DAILY_LIMIT_EXCEEDED",
                        "Yêu cầu OT vượt trần OT theo ngày đã cấu hình.", HttpStatus.CONFLICT);
            }
            if (approvedMonth + entity.getRequestedMinutes() > monthlyLimit) {
                throw new BusinessException("ATTENDANCE_OT_MONTHLY_LIMIT_EXCEEDED",
                        "Yêu cầu OT vượt trần OT theo tháng đã cấu hình.", HttpStatus.CONFLICT);
            }
            entity.setRequestStatus(AttendanceOvertimeStatus.APPROVED);
            entity.setApprovedAt(LocalDateTime.now());
            entity.setApprovedBy(getCurrentUserOrNull());
            entity.setManagerNote(trimToNull(request.note()));
            entity = overtimeRequestRepository.save(entity);
            recalculateDailyAttendance(entity.getEmployee(), entity.getAttendanceDate(), null);
        } else {
            entity.setRequestStatus(AttendanceOvertimeStatus.REJECTED);
            entity.setRejectedAt(LocalDateTime.now());
            entity.setRejectedBy(getCurrentUserOrNull());
            entity.setRejectionNote(trimToNull(request.note()));
            entity = overtimeRequestRepository.save(entity);
        }
        OvertimeListItemResponse response = toOvertimeListItem(entity);
        auditLogService.logSuccess(request.approved() ? "APPROVE" : "REJECT", "ATTENDANCE_OT", "att_overtime_request",
                entity.getOvertimeRequestId().toString(), oldSnapshot, response, request.note());
        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<OvertimeListItemResponse> listAdminOvertimeRequests(
            String keyword,
            AttendanceOvertimeStatus status,
            Long employeeId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size) {
        Specification<AttOvertimeRequest> specification = (root, query, builder) -> builder
                .isFalse(root.get("deleted"));
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("requestCode")), like),
                    builder.like(builder.lower(root.join("employee").get("employeeCode")), like),
                    builder.like(builder.lower(root.join("employee").get("fullName")), like)));
        }
        if (status != null) {
            specification = specification
                    .and((root, query, builder) -> builder.equal(root.get("requestStatus"), status));
        }
        if (employeeId != null) {
            specification = specification
                    .and((root, query, builder) -> builder.equal(root.join("employee").get("employeeId"), employeeId));
        }
        if (fromDate != null) {
            specification = specification
                    .and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("attendanceDate"), fromDate));
        }
        if (toDate != null) {
            specification = specification
                    .and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("attendanceDate"), toDate));
        }
        Page<AttOvertimeRequest> result = overtimeRequestRepository.findAll(specification,
                PageRequest.of(page, size,
                        Sort.by("createdAt").descending().and(Sort.by("overtimeRequestId").descending())));
        List<OvertimeListItemResponse> items = result.getContent().stream().map(this::toOvertimeListItem).toList();
        return toPageResponse(result, items, page, size);
    }

    @Transactional(readOnly = true)
    public List<AttendancePeriodResponse> listPeriods() {
        return attendancePeriodRepository.findAllByDeletedFalseOrderByPeriodYearDescPeriodMonthDesc()
                .stream().map(this::toAttendancePeriodResponse).toList();
    }

    @Transactional
    public AttendancePeriodResponse closePeriod(AttendancePeriodCloseRequest request) {
        YearMonth yearMonth = YearMonth.of(request.periodYear(), request.periodMonth());
        LocalDate fromDate = yearMonth.atDay(1);
        LocalDate toDate = yearMonth.atEndOfMonth();

        long openAdjustmentCount = adjustmentRequestRepository.countByAttendanceDateBetweenAndRequestStatusIn(
                fromDate, toDate, List.of(AttendanceAdjustmentStatus.DRAFT, AttendanceAdjustmentStatus.SUBMITTED,
                        AttendanceAdjustmentStatus.APPROVED));
        if (openAdjustmentCount > 0) {
            throw new BusinessException("ATTENDANCE_PERIOD_CLOSE_BLOCKED_ADJUSTMENT",
                    "Không thể chốt kỳ công khi vẫn còn yêu cầu điều chỉnh công chưa hoàn tất.", HttpStatus.CONFLICT);
        }
        long openOtCount = overtimeRequestRepository.countByAttendanceDateBetweenAndRequestStatusIn(
                fromDate, toDate, List.of(AttendanceOvertimeStatus.SUBMITTED));
        if (openOtCount > 0) {
            throw new BusinessException("ATTENDANCE_PERIOD_CLOSE_BLOCKED_OT",
                    "Không thể chốt kỳ công khi vẫn còn yêu cầu OT chờ duyệt.", HttpStatus.CONFLICT);
        }

        ensureDailySummaries(fromDate, toDate, null, null);

        AttAttendancePeriod period = attendancePeriodRepository
                .findByPeriodYearAndPeriodMonthAndDeletedFalse(request.periodYear(), request.periodMonth())
                .orElseGet(AttAttendancePeriod::new);
        period.setPeriodCode(String.format("ATT-%04d%02d", request.periodYear(), request.periodMonth()));
        period.setPeriodYear(request.periodYear());
        period.setPeriodMonth(request.periodMonth());
        period.setPeriodStartDate(fromDate);
        period.setPeriodEndDate(toDate);
        period.setPeriodStatus(request.closeDirectly() ? AttendancePeriodStatus.CLOSED : AttendancePeriodStatus.REVIEW);
        period.setNote(trimToNull(request.note()));
        if (request.closeDirectly()) {
            period.setClosedAt(LocalDateTime.now());
            period.setClosedBy(getCurrentUserOrNull());
        } else {
            period.setClosedAt(null);
            period.setClosedBy(null);
        }
        period.setTotalEmployeeCount(countEmployeesForSummary(fromDate, toDate, null, null));
        int totalAnomalyDayCount = dailyAttendanceRepository.findAll((root, query, builder) -> builder.and(
                builder.isFalse(root.get("deleted")),
                builder.between(root.get("attendanceDate"), fromDate, toDate),
                builder.greaterThan(root.get("anomalyCount"), 0))).size();
        period.setTotalAnomalyDayCount(totalAnomalyDayCount);
        period = attendancePeriodRepository.save(period);

        List<AttDailyAttendance> summaries = dailyAttendanceRepository.findAll((root, query, builder) -> builder.and(
                builder.isFalse(root.get("deleted")),
                builder.between(root.get("attendanceDate"), fromDate, toDate)));
        for (AttDailyAttendance summary : summaries) {
            summary.setAttendancePeriod(period);
        }
        dailyAttendanceRepository.saveAll(summaries);

        AttendancePeriodResponse response = toAttendancePeriodResponse(period);
        auditLogService.logSuccess(request.closeDirectly() ? "CLOSE" : "REVIEW", "ATTENDANCE_PERIOD",
                "att_attendance_period",
                period.getAttendancePeriodId().toString(), null, response, "Tính và cập nhật trạng thái kỳ công.");
        return response;
    }

    @Transactional
    public AttendancePeriodResponse reopenPeriod(Long attendancePeriodId, AttendancePeriodReopenRequest request) {
        AttAttendancePeriod period = getAttendancePeriod(attendancePeriodId);
        if (period.getPeriodStatus() != AttendancePeriodStatus.CLOSED) {
            throw new BusinessException("ATTENDANCE_PERIOD_REOPEN_STATUS_INVALID",
                    "Chỉ kỳ công đã chốt mới được mở lại.", HttpStatus.CONFLICT);
        }
        AttendancePeriodResponse oldSnapshot = toAttendancePeriodResponse(period);
        period.setPeriodStatus(AttendancePeriodStatus.REVIEW);
        period.setReopenedFlag(true);
        period.setReopenedAt(LocalDateTime.now());
        period.setReopenedBy(getCurrentUserOrNull());
        period.setReopenReason(request.reason().trim());
        period.setClosedAt(null);
        period.setClosedBy(null);
        period = attendancePeriodRepository.save(period);
        AttendancePeriodResponse response = toAttendancePeriodResponse(period);
        auditLogService.logSuccess("REOPEN", "ATTENDANCE_PERIOD", "att_attendance_period",
                period.getAttendancePeriodId().toString(),
                oldSnapshot, response, request.reason());
        return response;
    }

    @Transactional
    public String exportAnomalyCsv(LocalDate fromDate, LocalDate toDate, Long orgUnitId) {
        validateDateRange(fromDate, toDate);
        ensureDailySummaries(fromDate, toDate, orgUnitId, null);
        Specification<AttDailyAttendance> specification = (root, query, builder) -> builder.and(
                builder.isFalse(root.get("deleted")),
                builder.between(root.get("attendanceDate"), fromDate, toDate),
                builder.greaterThan(root.get("anomalyCount"), 0));
        if (orgUnitId != null) {
            HrOrgUnit orgUnit = getOrgUnit(orgUnitId);
            specification = specification.and((root, query, builder) -> builder
                    .like(root.join("employee").join("orgUnit").get("pathCode"), orgUnit.getPathCode() + "%"));
        }
        List<AttDailyAttendance> rows = dailyAttendanceRepository.findAll(specification,
                Sort.by("attendanceDate").ascending().and(Sort.by("employee.employeeCode").ascending()));
        StringBuilder csv = new StringBuilder();
        csv.append(
                "attendanceDate,employeeCode,employeeName,orgUnitName,shiftCode,shiftName,dailyStatus,anomalyCodes,lateMinutes,earlyLeaveMinutes,missingCheckIn,missingCheckOut,approvedOtMinutes,adjustmentRequestCode\n");
        for (AttDailyAttendance row : rows) {
            String adjustmentCode = row.getFinalizedAdjustmentRequest() == null ? ""
                    : row.getFinalizedAdjustmentRequest().getRequestCode();
            csv.append(csv(row.getAttendanceDate()))
                    .append(',').append(csv(row.getEmployee().getEmployeeCode()))
                    .append(',').append(csv(row.getEmployee().getFullName()))
                    .append(',')
                    .append(csv(row.getEmployee().getOrgUnit() == null ? null
                            : row.getEmployee().getOrgUnit().getOrgUnitName()))
                    .append(',')
                    .append(csv(row.getShiftAssignment() == null ? null
                            : row.getShiftAssignment().getShift().getShiftCode()))
                    .append(',')
                    .append(csv(row.getShiftAssignment() == null ? null
                            : row.getShiftAssignment().getShift().getShiftName()))
                    .append(',').append(csv(row.getDailyStatus().name()))
                    .append(',').append(csv(row.getAnomalyCodes()))
                    .append(',').append(csv(row.getLateMinutes()))
                    .append(',').append(csv(row.getEarlyLeaveMinutes()))
                    .append(',').append(csv(row.isMissingCheckIn()))
                    .append(',').append(csv(row.isMissingCheckOut()))
                    .append(',').append(csv(row.getApprovedOtMinutes()))
                    .append(',').append(csv(adjustmentCode))
                    .append('\n');
        }
        return csv.toString();
    }

    private void ensureDailySummaries(LocalDate fromDate, LocalDate toDate, Long orgUnitId, Long employeeId) {
        List<HrEmployee> employees = findEmployeesForSummary(fromDate, toDate, orgUnitId, employeeId);
        for (HrEmployee employee : employees) {
            LocalDate current = fromDate;
            while (!current.isAfter(toDate)) {
                recalculateDailyAttendance(employee, current, attendancePeriodRepository
                        .findByPeriodYearAndPeriodMonthAndDeletedFalse(current.getYear(), current.getMonthValue())
                        .orElse(null));
                current = current.plusDays(1);
            }
        }
    }

    @Transactional
    protected AttDailyAttendance recalculateDailyAttendance(HrEmployee employee, LocalDate attendanceDate,
            AttAttendancePeriod forcedPeriod) {
        AttDailyAttendance summary = dailyAttendanceRepository
                .findByEmployeeEmployeeIdAndAttendanceDateAndDeletedFalse(employee.getEmployeeId(), attendanceDate)
                .orElseGet(AttDailyAttendance::new);

        AttShiftAssignment assignment = shiftAssignmentRepository
                .findEffectiveAssignment(employee.getEmployeeId(), attendanceDate).orElse(null);
        AttShiftVersion shiftVersion = assignment == null ? null
                : shiftVersionRepository.findEffectiveVersion(assignment.getShift().getShiftId(), attendanceDate)
                        .orElse(null);
        ScheduledShiftWindow shiftWindow = shiftVersion == null ? null
                : buildScheduledShiftWindow(attendanceDate, shiftVersion);
        List<AttAttendanceLog> logs = attendanceLogRepository
                .findAllByEmployeeEmployeeIdAndAttendanceDateAndDeletedFalseOrderByEventTimeAscAttendanceLogIdAsc(
                        employee.getEmployeeId(), attendanceDate);

        LocalDateTime actualCheckInAt = logs.stream()
                .filter(log -> log.getEventType() == AttendanceLogEventType.CHECK_IN)
                .map(AttAttendanceLog::getEventTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime actualCheckOutAt = logs.stream()
                .filter(log -> log.getEventType() == AttendanceLogEventType.CHECK_OUT)
                .map(AttAttendanceLog::getEventTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        int workedMinutes = 0;
        if (actualCheckInAt != null && actualCheckOutAt != null && actualCheckOutAt.isAfter(actualCheckInAt)) {
            workedMinutes = (int) ChronoUnit.MINUTES.between(actualCheckInAt, actualCheckOutAt)
                    - (shiftVersion == null ? 0 : zeroIfNull(shiftVersion.getBreakMinutes()));
            workedMinutes = Math.max(workedMinutes, 0);
        }

        int lateMinutes = 0;
        int earlyLeaveMinutes = 0;
        if (shiftWindow != null && actualCheckInAt != null) {
            LocalDateTime graceStart = shiftWindow.plannedStartAt()
                    .plusMinutes(zeroIfNull(shiftVersion.getLateGraceMinutes()));
            if (actualCheckInAt.isAfter(graceStart)) {
                lateMinutes = (int) ChronoUnit.MINUTES.between(graceStart, actualCheckInAt);
            }
        }
        if (shiftWindow != null && actualCheckOutAt != null) {
            LocalDateTime earlyThreshold = shiftWindow.plannedEndAt()
                    .minusMinutes(zeroIfNull(shiftVersion.getEarlyLeaveGraceMinutes()));
            if (actualCheckOutAt.isBefore(earlyThreshold)) {
                earlyLeaveMinutes = (int) ChronoUnit.MINUTES.between(actualCheckOutAt, earlyThreshold);
            }
        }

        boolean onLeave = leaveRequestRepository.existsFinalizedLeaveOnDate(employee.getEmployeeId(), attendanceDate,
                LeaveRequestStatus.FINALIZED);
        int approvedOtMinutes = overtimeRequestRepository
                .findApprovedByEmployeeAndDate(employee.getEmployeeId(), attendanceDate,
                        AttendanceOvertimeStatus.APPROVED)
                .stream().map(AttOvertimeRequest::getRequestedMinutes).filter(Objects::nonNull)
                .mapToInt(Integer::intValue).sum();

        List<String> anomalyCodes = new ArrayList<>();
        boolean missingCheckIn = actualCheckInAt == null && actualCheckOutAt != null;
        boolean missingCheckOut = actualCheckInAt != null && actualCheckOutAt == null;

        if (assignment == null) {
            anomalyCodes.add("NO_SHIFT");
        }
        if (logs.isEmpty() && !onLeave) {
            anomalyCodes.add("NO_LOG");
        }
        if (missingCheckIn) {
            anomalyCodes.add("MISSING_CHECK_IN");
        }
        if (missingCheckOut) {
            anomalyCodes.add("MISSING_CHECK_OUT");
        }
        if (lateMinutes > 0) {
            anomalyCodes.add("LATE");
        }
        if (earlyLeaveMinutes > 0) {
            anomalyCodes.add("EARLY_LEAVE");
        }

        AttendanceDailyStatus dailyStatus;
        if (onLeave && logs.isEmpty()) {
            dailyStatus = AttendanceDailyStatus.ON_LEAVE;
        } else if (assignment == null && logs.isEmpty()) {
            dailyStatus = AttendanceDailyStatus.NO_SHIFT;
        } else if (logs.isEmpty()) {
            dailyStatus = AttendanceDailyStatus.ABSENT;
        } else if (missingCheckIn || missingCheckOut) {
            dailyStatus = AttendanceDailyStatus.INCOMPLETE;
        } else {
            dailyStatus = AttendanceDailyStatus.PRESENT;
        }

        AttAttendanceAdjustmentRequest finalizedAdjustment = adjustmentRequestRepository.findByEmployeeDateAndStatus(
                employee.getEmployeeId(), attendanceDate, AttendanceAdjustmentStatus.FINALIZED).stream().findFirst()
                .orElse(null);

        summary.setEmployee(employee);
        summary.setAttendanceDate(attendanceDate);
        summary.setShiftAssignment(assignment);
        summary.setShiftVersion(shiftVersion);
        summary.setAttendancePeriod(forcedPeriod);
        summary.setFinalizedAdjustmentRequest(finalizedAdjustment);
        summary.setPlannedStartAt(shiftWindow == null ? null : shiftWindow.plannedStartAt());
        summary.setPlannedEndAt(shiftWindow == null ? null : shiftWindow.plannedEndAt());
        summary.setActualCheckInAt(actualCheckInAt);
        summary.setActualCheckOutAt(actualCheckOutAt);
        summary.setWorkedMinutes(workedMinutes);
        summary.setLateMinutes(lateMinutes);
        summary.setEarlyLeaveMinutes(earlyLeaveMinutes);
        summary.setApprovedOtMinutes(approvedOtMinutes);
        summary.setMissingCheckIn(missingCheckIn);
        summary.setMissingCheckOut(missingCheckOut);
        summary.setAnomalyCount(anomalyCodes.size());
        summary.setAnomalyCodes(String.join(",", anomalyCodes));
        summary.setDailyStatus(dailyStatus);
        summary.setOnLeave(onLeave);
        summary.setCalculatedAt(LocalDateTime.now());
        return dailyAttendanceRepository.save(summary);
    }

    private List<HrEmployee> findEmployeesForSummary(LocalDate fromDate, LocalDate toDate, Long orgUnitId,
            Long employeeId) {
        Specification<HrEmployee> specification = (root, query, builder) -> builder.and(
                builder.isFalse(root.get("deleted")),
                builder.lessThanOrEqualTo(root.get("hireDate"), toDate));
        if (employeeId != null) {
            specification = specification
                    .and((root, query, builder) -> builder.equal(root.get("employeeId"), employeeId));
        }
        if (orgUnitId != null) {
            HrOrgUnit orgUnit = getOrgUnit(orgUnitId);
            specification = specification.and((root, query, builder) -> builder
                    .like(root.join("orgUnit").get("pathCode"), orgUnit.getPathCode() + "%"));
        }
        return employeeRepository.findAll(specification, Sort.by("employeeCode").ascending());
    }

    private int countEmployeesForSummary(LocalDate fromDate, LocalDate toDate, Long orgUnitId, Long employeeId) {
        return findEmployeesForSummary(fromDate, toDate, orgUnitId, employeeId).size();
    }

    private Optional<ScheduledShiftWindow> getScheduledShiftWindow(HrEmployee employee, LocalDate attendanceDate) {
        AttShiftAssignment assignment = shiftAssignmentRepository
                .findEffectiveAssignment(employee.getEmployeeId(), attendanceDate).orElse(null);
        if (assignment == null) {
            return Optional.empty();
        }
        AttShiftVersion version = shiftVersionRepository
                .findEffectiveVersion(assignment.getShift().getShiftId(), attendanceDate).orElse(null);
        if (version == null) {
            return Optional.empty();
        }
        return Optional.of(buildScheduledShiftWindow(attendanceDate, version));
    }

    private ScheduledShiftWindow buildScheduledShiftWindow(LocalDate attendanceDate, AttShiftVersion version) {
        LocalDateTime start = LocalDateTime.of(attendanceDate, version.getStartTime());
        LocalDate endDate = version.isCrossesMidnight() ? attendanceDate.plusDays(1) : attendanceDate;
        LocalDateTime end = LocalDateTime.of(endDate, version.getEndTime());
        return new ScheduledShiftWindow(start, end, version);
    }

    private void createSyntheticLogsFromAdjustment(AttAttendanceAdjustmentRequest request) {
        if (attendanceLogRepository
                .existsByAdjustmentRequestAdjustmentRequestIdAndDeletedFalse(request.getAdjustmentRequestId())) {
            return;
        }
        AttShiftAssignment assignment = shiftAssignmentRepository
                .findEffectiveAssignment(request.getEmployee().getEmployeeId(), request.getAttendanceDate())
                .orElse(null);

        if (request.getProposedCheckInAt() != null) {
            AttAttendanceLog checkIn = new AttAttendanceLog();
            checkIn.setEmployee(request.getEmployee());
            checkIn.setAttendanceDate(request.getAttendanceDate());
            checkIn.setEventType(AttendanceLogEventType.CHECK_IN);
            checkIn.setEventTime(request.getProposedCheckInAt());
            checkIn.setSourceType(AttendanceLogSourceType.ADJUSTMENT);
            checkIn.setShiftAssignment(assignment);
            checkIn.setAdjustmentRequest(request);
            checkIn.setNote("Synthetic log from finalized adjustment request");
            attendanceLogRepository.save(checkIn);
        }
        if (request.getProposedCheckOutAt() != null) {
            AttAttendanceLog checkOut = new AttAttendanceLog();
            checkOut.setEmployee(request.getEmployee());
            checkOut.setAttendanceDate(request.getAttendanceDate());
            checkOut.setEventType(AttendanceLogEventType.CHECK_OUT);
            checkOut.setEventTime(request.getProposedCheckOutAt());
            checkOut.setSourceType(AttendanceLogSourceType.ADJUSTMENT);
            checkOut.setShiftAssignment(assignment);
            checkOut.setAdjustmentRequest(request);
            checkOut.setNote("Synthetic log from finalized adjustment request");
            attendanceLogRepository.save(checkOut);
        }
    }

    private void appendAdjustmentHistory(
            AttAttendanceAdjustmentRequest request,
            AttendanceAdjustmentStatus fromStatus,
            AttendanceAdjustmentStatus toStatus,
            String actionCode,
            String note) {
        AttAttendanceAdjustmentHistory history = new AttAttendanceAdjustmentHistory();
        history.setAdjustmentRequest(request);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setActionCode(actionCode);
        history.setActionNote(trimToNull(note));
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(getCurrentUserOrNull());
        history.setSnapshotJson(toJson(toAdjustmentListItem(request)));
        adjustmentHistoryRepository.save(history);
    }

    private void assertPeriodOpen(LocalDate attendanceDate) {
        boolean periodClosed = attendancePeriodRepository.findClosedPeriodByDate(attendanceDate).isPresent();
        if (periodClosed) {
            throw new BusinessException("ATTENDANCE_PERIOD_CLOSED",
                    "Ngày công thuộc kỳ đã chốt. Không thể thay đổi dữ liệu chấm công hoặc điều chỉnh công.",
                    HttpStatus.CONFLICT);
        }
    }

    private void validateShiftRequest(UpsertShiftRequest request) {
        if (!request.crossesMidnight() && !request.endTime().isAfter(request.startTime())) {
            throw new BusinessException("ATTENDANCE_SHIFT_TIME_INVALID",
                    "Ca không qua đêm phải có endTime lớn hơn startTime.", HttpStatus.BAD_REQUEST);
        }
        if (request.crossesMidnight() && !request.endTime().isBefore(request.startTime())) {
            throw new BusinessException("ATTENDANCE_SHIFT_TIME_INVALID",
                    "Ca qua đêm phải có endTime nhỏ hơn startTime.", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateAdjustmentRequestTimes(LocalDate attendanceDate, LocalDateTime proposedCheckInAt,
            LocalDateTime proposedCheckOutAt) {
        if (proposedCheckInAt == null && proposedCheckOutAt == null) {
            throw new BusinessException("ATTENDANCE_ADJUSTMENT_TIME_REQUIRED",
                    "Phải đề xuất ít nhất check-in hoặc check-out.", HttpStatus.BAD_REQUEST);
        }
        if (proposedCheckInAt != null && proposedCheckOutAt != null && !proposedCheckOutAt.isAfter(proposedCheckInAt)) {
            throw new BusinessException("ATTENDANCE_ADJUSTMENT_TIME_INVALID",
                    "proposedCheckOutAt phải lớn hơn proposedCheckInAt.", HttpStatus.BAD_REQUEST);
        }
        if (proposedCheckInAt != null && proposedCheckInAt.toLocalDate().isBefore(attendanceDate.minusDays(1))) {
            throw new BusinessException("ATTENDANCE_ADJUSTMENT_TIME_INVALID",
                    "proposedCheckInAt không phù hợp với ngày công.", HttpStatus.BAD_REQUEST);
        }
        if (proposedCheckOutAt != null && proposedCheckOutAt.toLocalDate().isAfter(attendanceDate.plusDays(1))) {
            throw new BusinessException("ATTENDANCE_ADJUSTMENT_TIME_INVALID",
                    "proposedCheckOutAt không phù hợp với ngày công.", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateOvertimeRequest(LocalDate attendanceDate, LocalDateTime overtimeStartAt,
            LocalDateTime overtimeEndAt) {
        if (!overtimeEndAt.isAfter(overtimeStartAt)) {
            throw new BusinessException("ATTENDANCE_OT_TIME_INVALID", "overtimeEndAt phải lớn hơn overtimeStartAt.",
                    HttpStatus.BAD_REQUEST);
        }
        if (overtimeStartAt.toLocalDate().isBefore(attendanceDate.minusDays(1))
                || overtimeEndAt.toLocalDate().isAfter(attendanceDate.plusDays(1))) {
            throw new BusinessException("ATTENDANCE_OT_TIME_INVALID", "Khung giờ OT không phù hợp với attendanceDate.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void validateSelfLogSource(AttendanceLogSourceType sourceType) {
        if (sourceType != AttendanceLogSourceType.WEB && sourceType != AttendanceLogSourceType.MOBILE_APP) {
            throw new BusinessException("ATTENDANCE_SELF_LOG_SOURCE_INVALID",
                    "Nhân viên chỉ được chấm công tự phục vụ qua WEB hoặc MOBILE_APP.", HttpStatus.BAD_REQUEST);
        }
    }

    private void requireEvidenceIfPresent(String fileKey, String code, String message) {
        if (trimToNull(fileKey) != null) {
            storageFileService.requireActiveReference(fileKey.trim(), code, message);
        }
    }

    private void assertSelfAdjustmentOwner(AttAttendanceAdjustmentRequest entity) {
        HrEmployee currentEmployee = accessScopeService.getCurrentEmployeeRequired();
        if (!entity.getEmployee().getEmployeeId().equals(currentEmployee.getEmployeeId())) {
            throw new ForbiddenException("ATTENDANCE_ADJUSTMENT_SELF_SCOPE_DENIED",
                    "Bạn chỉ được thao tác yêu cầu điều chỉnh công của chính mình.");
        }
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new BusinessException("ATTENDANCE_DATE_RANGE_REQUIRED", "fromDate và toDate là bắt buộc.",
                    HttpStatus.BAD_REQUEST);
        }
        if (toDate.isBefore(fromDate)) {
            throw new BusinessException("ATTENDANCE_DATE_RANGE_INVALID", "toDate không được nhỏ hơn fromDate.",
                    HttpStatus.BAD_REQUEST);
        }
        if (ChronoUnit.DAYS.between(fromDate, toDate) > 62) {
            throw new BusinessException("ATTENDANCE_DATE_RANGE_TOO_LARGE",
                    "Khoảng ngày tối đa cho một lần truy vấn là 62 ngày.", HttpStatus.BAD_REQUEST);
        }
    }

    private AttShift getShift(Long shiftId) {
        return shiftRepository.findByShiftIdAndDeletedFalse(shiftId)
                .orElseThrow(() -> new NotFoundException("ATTENDANCE_SHIFT_NOT_FOUND", "Không tìm thấy ca làm việc."));
    }

    private HrEmployee getEmployee(Long employeeId) {
        return employeeRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new NotFoundException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên."));
    }

    private HrOrgUnit getOrgUnit(Long orgUnitId) {
        return orgUnitRepository.findByOrgUnitIdAndDeletedFalse(orgUnitId)
                .orElseThrow(() -> new NotFoundException("ORG_UNIT_NOT_FOUND", "Không tìm thấy đơn vị tổ chức."));
    }

    private AttAttendanceAdjustmentRequest getAdjustmentRequest(Long adjustmentRequestId) {
        return adjustmentRequestRepository.findByAdjustmentRequestIdAndDeletedFalse(adjustmentRequestId)
                .orElseThrow(() -> new NotFoundException("ATTENDANCE_ADJUSTMENT_NOT_FOUND",
                        "Không tìm thấy yêu cầu điều chỉnh công."));
    }

    private AttOvertimeRequest getOvertimeRequest(Long overtimeRequestId) {
        return overtimeRequestRepository.findByOvertimeRequestIdAndDeletedFalse(overtimeRequestId)
                .orElseThrow(() -> new NotFoundException("ATTENDANCE_OT_NOT_FOUND", "Không tìm thấy yêu cầu OT."));
    }

    private AttAttendancePeriod getAttendancePeriod(Long attendancePeriodId) {
        return attendancePeriodRepository.findByAttendancePeriodIdAndDeletedFalse(attendancePeriodId)
                .orElseThrow(() -> new NotFoundException("ATTENDANCE_PERIOD_NOT_FOUND", "Không tìm thấy kỳ công."));
    }

    private SecUserAccount getCurrentUserOrNull() {
        return SecurityUserContext.getCurrentUserId()
                .flatMap(userAccountRepository::findById)
                .orElse(null);
    }

    private String generateRequestCode(String prefix, Long employeeId) {
        return prefix + "-" + employeeId + "-" + System.currentTimeMillis();
    }

    private int getIntegerSetting(String settingKey, int defaultValue) {
        return platformSettingRepository.findBySettingKeyIgnoreCaseAndDeletedFalse(settingKey)
                .map(SysPlatformSetting::getSettingValue)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException exception) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    private ShiftListItemResponse toShiftListItem(AttShift shift) {
        AttShiftVersion latest = shiftVersionRepository
                .findAllByShiftShiftIdAndDeletedFalseOrderByVersionNoDesc(shift.getShiftId()).stream().findFirst()
                .orElse(null);
        return new ShiftListItemResponse(
                shift.getShiftId(),
                shift.getShiftCode(),
                shift.getShiftName(),
                shift.getDescription(),
                shift.getSortOrder(),
                shift.getStatus().name(),
                latest == null ? null : latest.getShiftVersionId(),
                latest == null ? null : latest.getVersionNo(),
                latest == null ? null : latest.getEffectiveFrom(),
                latest == null ? null : latest.getEffectiveTo(),
                latest == null ? null : latest.getStartTime(),
                latest == null ? null : latest.getEndTime(),
                latest != null && latest.isCrossesMidnight(),
                latest == null ? null : latest.getBreakMinutes(),
                latest == null ? null : latest.getLateGraceMinutes(),
                latest == null ? null : latest.getEarlyLeaveGraceMinutes(),
                latest != null && latest.isOtAllowed(),
                latest != null && latest.isNightShift());
    }

    private ShiftDetailResponse toShiftDetail(AttShift shift) {
        List<ShiftVersionResponse> versions = shiftVersionRepository
                .findAllByShiftShiftIdAndDeletedFalseOrderByVersionNoDesc(shift.getShiftId())
                .stream()
                .map(version -> new ShiftVersionResponse(
                        version.getShiftVersionId(),
                        version.getVersionNo(),
                        version.getEffectiveFrom(),
                        version.getEffectiveTo(),
                        version.getStartTime(),
                        version.getEndTime(),
                        version.isCrossesMidnight(),
                        version.getBreakMinutes(),
                        version.getLateGraceMinutes(),
                        version.getEarlyLeaveGraceMinutes(),
                        version.isOtAllowed(),
                        version.isNightShift(),
                        version.getMinWorkMinutesForPresent(),
                        version.getStatus().name(),
                        version.getNote()))
                .toList();
        return new ShiftDetailResponse(
                shift.getShiftId(),
                shift.getShiftCode(),
                shift.getShiftName(),
                shift.getDescription(),
                shift.getSortOrder(),
                shift.getStatus().name(),
                versions);
    }

    private ShiftAssignmentResponse toShiftAssignmentResponse(AttShiftAssignment entity) {
        return new ShiftAssignmentResponse(
                entity.getShiftAssignmentId(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getEmployee().getOrgUnit() == null ? null : entity.getEmployee().getOrgUnit().getOrgUnitId(),
                entity.getEmployee().getOrgUnit() == null ? null : entity.getEmployee().getOrgUnit().getOrgUnitName(),
                entity.getShift().getShiftId(),
                entity.getShift().getShiftCode(),
                entity.getShift().getShiftName(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getAssignmentNote(),
                entity.getAssignmentBatchRef(),
                entity.getCreatedAt(),
                entity.getCreatedBy());
    }

    private AttendanceLogResponse toAttendanceLogResponse(AttAttendanceLog entity) {
        return new AttendanceLogResponse(
                entity.getAttendanceLogId(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getAttendanceDate(),
                entity.getEventType().name(),
                entity.getEventTime(),
                entity.getSourceType().name(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getDeviceRef(),
                entity.getNote(),
                entity.getShiftAssignment() == null ? null : entity.getShiftAssignment().getShiftAssignmentId(),
                entity.getAdjustmentRequest() == null ? null : entity.getAdjustmentRequest().getAdjustmentRequestId());
    }

    private DailyAttendanceListItemResponse toDailyAttendanceListItem(AttDailyAttendance entity) {
        return new DailyAttendanceListItemResponse(
                entity.getDailyAttendanceId(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getEmployee().getOrgUnit() == null ? null : entity.getEmployee().getOrgUnit().getOrgUnitId(),
                entity.getEmployee().getOrgUnit() == null ? null : entity.getEmployee().getOrgUnit().getOrgUnitName(),
                entity.getAttendanceDate(),
                entity.getShiftAssignment() == null ? null : entity.getShiftAssignment().getShift().getShiftId(),
                entity.getShiftAssignment() == null ? null : entity.getShiftAssignment().getShift().getShiftCode(),
                entity.getShiftAssignment() == null ? null : entity.getShiftAssignment().getShift().getShiftName(),
                entity.getShiftVersion() == null ? null : entity.getShiftVersion().getShiftVersionId(),
                entity.getShiftAssignment() == null ? null : entity.getShiftAssignment().getShiftAssignmentId(),
                entity.getPlannedStartAt(),
                entity.getPlannedEndAt(),
                entity.getActualCheckInAt(),
                entity.getActualCheckOutAt(),
                entity.getWorkedMinutes(),
                entity.getLateMinutes(),
                entity.getEarlyLeaveMinutes(),
                entity.getApprovedOtMinutes(),
                entity.isMissingCheckIn(),
                entity.isMissingCheckOut(),
                entity.getAnomalyCount(),
                splitCsv(entity.getAnomalyCodes()),
                entity.getDailyStatus().name(),
                entity.isOnLeave(),
                entity.getAttendancePeriod() == null ? null : entity.getAttendancePeriod().getAttendancePeriodId(),
                entity.getFinalizedAdjustmentRequest() == null ? null
                        : entity.getFinalizedAdjustmentRequest().getAdjustmentRequestId());
    }

    private AttendanceAdjustmentListItemResponse toAdjustmentListItem(AttAttendanceAdjustmentRequest entity) {
        return new AttendanceAdjustmentListItemResponse(
                entity.getAdjustmentRequestId(),
                entity.getRequestCode(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getEmployee().getOrgUnit() == null ? null : entity.getEmployee().getOrgUnit().getOrgUnitId(),
                entity.getEmployee().getOrgUnit() == null ? null : entity.getEmployee().getOrgUnit().getOrgUnitName(),
                entity.getAttendanceDate(),
                entity.getIssueType().name(),
                entity.getProposedCheckInAt(),
                entity.getProposedCheckOutAt(),
                entity.getReason(),
                entity.getEvidenceFileKey(),
                entity.getRequestStatus().name(),
                entity.getSubmittedAt(),
                entity.getApprovedAt(),
                entity.getRejectedAt(),
                entity.getFinalizedAt(),
                entity.getCanceledAt(),
                entity.getManagerNote(),
                entity.getRejectionNote(),
                entity.getFinalizeNote(),
                entity.getCancelNote(),
                entity.getCopiedFromAdjustmentRequest() == null ? null
                        : entity.getCopiedFromAdjustmentRequest().getAdjustmentRequestId());
    }

    private AttendanceAdjustmentDetailResponse toAdjustmentDetail(AttAttendanceAdjustmentRequest entity) {
        List<AttendanceAdjustmentHistoryResponse> history = adjustmentHistoryRepository
                .findAllByAdjustmentRequestAdjustmentRequestIdOrderByChangedAtAscAdjustmentHistoryIdAsc(
                        entity.getAdjustmentRequestId())
                .stream()
                .map(item -> new AttendanceAdjustmentHistoryResponse(
                        item.getAdjustmentHistoryId(),
                        item.getFromStatus() == null ? null : item.getFromStatus().name(),
                        item.getToStatus().name(),
                        item.getActionCode(),
                        item.getActionNote(),
                        item.getChangedAt(),
                        item.getChangedBy() == null ? null : item.getChangedBy().getUserId(),
                        item.getSnapshotJson()))
                .toList();
        return new AttendanceAdjustmentDetailResponse(toAdjustmentListItem(entity), history);
    }

    private OvertimeListItemResponse toOvertimeListItem(AttOvertimeRequest entity) {
        return new OvertimeListItemResponse(
                entity.getOvertimeRequestId(),
                entity.getRequestCode(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getEmployee().getOrgUnit() == null ? null : entity.getEmployee().getOrgUnit().getOrgUnitId(),
                entity.getEmployee().getOrgUnit() == null ? null : entity.getEmployee().getOrgUnit().getOrgUnitName(),
                entity.getAttendanceDate(),
                entity.getOvertimeStartAt(),
                entity.getOvertimeEndAt(),
                entity.getRequestedMinutes(),
                entity.getReason(),
                entity.getEvidenceFileKey(),
                entity.getRequestStatus().name(),
                entity.getSubmittedAt(),
                entity.getApprovedAt(),
                entity.getRejectedAt(),
                entity.getManagerNote(),
                entity.getRejectionNote());
    }

    private AttendancePeriodResponse toAttendancePeriodResponse(AttAttendancePeriod entity) {
        return new AttendancePeriodResponse(
                entity.getAttendancePeriodId(),
                entity.getPeriodCode(),
                entity.getPeriodYear(),
                entity.getPeriodMonth(),
                entity.getPeriodStartDate(),
                entity.getPeriodEndDate(),
                entity.getPeriodStatus().name(),
                entity.getNote(),
                entity.getClosedAt(),
                entity.getClosedBy() == null ? null : entity.getClosedBy().getUserId(),
                entity.getReopenedAt(),
                entity.getReopenedBy() == null ? null : entity.getReopenedBy().getUserId(),
                entity.getReopenReason(),
                entity.isReopenedFlag(),
                entity.getTotalEmployeeCount(),
                entity.getTotalAnomalyDayCount());
    }

    private <E, T> PageResponse<T> toPageResponse(Page<E> pageData, List<T> items, int page, int size) {
        return new PageResponse<>(items, page, size, pageData.getTotalElements(), pageData.getTotalPages(),
                pageData.hasNext(), pageData.hasPrevious());
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private boolean overlaps(LocalDateTime startA, LocalDateTime endA, LocalDateTime startB, LocalDateTime endB) {
        return !startA.isAfter(endB) && !endA.isBefore(startB);
    }

    private int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{\"serializationError\":true}";
        }
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String string = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + string + "\"";
    }

    private record ScheduledShiftWindow(LocalDateTime plannedStartAt, LocalDateTime plannedEndAt,
            AttShiftVersion version) {
    }
}
