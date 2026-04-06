package com.company.hrm.module.reporting.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.ReportCode;
import com.company.hrm.common.constant.ReportRunStatus;
import com.company.hrm.common.constant.ReportRunTriggerType;
import com.company.hrm.common.constant.ReportScheduleFrequency;
import com.company.hrm.common.constant.StorageVisibilityScope;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.config.AppProperties;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.auth.service.MailService;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.payroll.service.PayrollService;
import com.company.hrm.module.reporting.dto.DashboardBreakdownItemResponse;
import com.company.hrm.module.reporting.dto.DashboardTrendPointResponse;
import com.company.hrm.module.reporting.dto.HeadcountDashboardResponse;
import com.company.hrm.module.reporting.dto.HealthMetricResponse;
import com.company.hrm.module.reporting.dto.ReportScheduleConfigResponse;
import com.company.hrm.module.reporting.dto.ReportScheduleConfigUpsertRequest;
import com.company.hrm.module.reporting.dto.ReportScheduleRunResponse;
import com.company.hrm.module.reporting.dto.SystemHealthDashboardResponse;
import com.company.hrm.module.reporting.dto.TeamDashboardResponse;
import com.company.hrm.module.reporting.entity.RepReportScheduleConfig;
import com.company.hrm.module.reporting.entity.RepReportScheduleRun;
import com.company.hrm.module.reporting.repository.RepReportScheduleConfigRepository;
import com.company.hrm.module.reporting.repository.RepReportScheduleRunRepository;
import com.company.hrm.module.storage.dto.StoredFileResponse;
import com.company.hrm.module.storage.service.StorageFileService;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ReportingService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RepReportScheduleConfigRepository reportScheduleConfigRepository;
    private final RepReportScheduleRunRepository reportScheduleRunRepository;
    private final ReportAccessScopeService reportAccessScopeService;
    private final PayrollService payrollService;
    private final StorageFileService storageFileService;
    private final MailService mailService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    public ReportingService(
            NamedParameterJdbcTemplate jdbcTemplate,
            RepReportScheduleConfigRepository reportScheduleConfigRepository,
            RepReportScheduleRunRepository reportScheduleRunRepository,
            ReportAccessScopeService reportAccessScopeService,
            PayrollService payrollService,
            StorageFileService storageFileService,
            MailService mailService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper,
            AppProperties appProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.reportScheduleConfigRepository = reportScheduleConfigRepository;
        this.reportScheduleRunRepository = reportScheduleRunRepository;
        this.reportAccessScopeService = reportAccessScopeService;
        this.payrollService = payrollService;
        this.storageFileService = storageFileService;
        this.mailService = mailService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
    }

    @Transactional(readOnly = true)
    public HeadcountDashboardResponse getHeadcountDashboard() {
        Long totalEmployeeCount = count("""
                select count(1)
                from dbo.hr_employee
                where is_deleted = 0
                """);
        Long activeEmployeeCount = count("""
                select count(1)
                from dbo.hr_employee
                where is_deleted = 0 and employment_status = 'ACTIVE'
                """);
        Long probationEmployeeCount = count("""
                select count(1)
                from dbo.hr_employee
                where is_deleted = 0 and employment_status = 'PROBATION'
                """);
        Long suspendedEmployeeCount = count("""
                select count(1)
                from dbo.hr_employee
                where is_deleted = 0 and employment_status = 'SUSPENDED'
                """);
        Long resignedEmployeeCount = count("""
                select count(1)
                from dbo.hr_employee
                where is_deleted = 0 and employment_status = 'RESIGNED'
                """);
        Long terminatedEmployeeCount = count("""
                select count(1)
                from dbo.hr_employee
                where is_deleted = 0 and employment_status = 'TERMINATED'
                """);
        Long contractExpiring30DayCount = count("""
                select count(1)
                from dbo.ct_labor_contract c
                where c.is_deleted = 0
                  and c.contract_status = 'ACTIVE'
                  and c.end_date is not null
                  and c.end_date between :fromDate and :toDate
                """, params()
                .addValue("fromDate", LocalDate.now())
                .addValue("toDate", LocalDate.now().plusDays(30)));
        Long pendingOnboardingCount = count("""
                select count(1)
                from dbo.onb_onboarding
                where is_deleted = 0
                  and status in ('DRAFT', 'IN_PROGRESS', 'READY_FOR_JOIN')
                """);
        Long openOffboardingCount = count("""
                select count(1)
                from dbo.off_offboarding_case
                where is_deleted = 0
                  and status not in ('CLOSED', 'CANCELLED', 'MANAGER_REJECTED')
                """);

        List<DashboardBreakdownItemResponse> employmentStatusBreakdown = jdbcTemplate.query("""
                select employment_status as statusCode, count(1) as totalCount
                from dbo.hr_employee
                where is_deleted = 0
                group by employment_status
                order by employment_status
                """,
                params(),
                (rs, rowNum) -> new DashboardBreakdownItemResponse(
                        rs.getString("statusCode"),
                        rs.getString("statusCode"),
                        rs.getLong("totalCount")));

        List<DashboardBreakdownItemResponse> orgUnitHeadcountBreakdown = jdbcTemplate.query("""
                select o.org_unit_code as orgUnitCode,
                       o.org_unit_name as orgUnitName,
                       count(1) as totalCount
                from dbo.hr_employee e
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where e.is_deleted = 0
                  and e.employment_status in ('PROBATION', 'ACTIVE', 'SUSPENDED')
                group by o.org_unit_code, o.org_unit_name
                order by totalCount desc, o.org_unit_name asc
                """,
                params(),
                (rs, rowNum) -> new DashboardBreakdownItemResponse(
                        rs.getString("orgUnitCode"),
                        rs.getString("orgUnitName"),
                        rs.getLong("totalCount")));

        List<DashboardTrendPointResponse> monthlyMovement = buildMonthlyMovement(
                LocalDate.now().withDayOfMonth(1).minusMonths(5), 6, null);

        return new HeadcountDashboardResponse(
                totalEmployeeCount,
                activeEmployeeCount,
                probationEmployeeCount,
                suspendedEmployeeCount,
                resignedEmployeeCount,
                terminatedEmployeeCount,
                contractExpiring30DayCount,
                pendingOnboardingCount,
                openOffboardingCount,
                employmentStatusBreakdown,
                orgUnitHeadcountBreakdown,
                monthlyMovement);
    }

    @Transactional(readOnly = true)
    public TeamDashboardResponse getTeamDashboard() {
        HrEmployee managerEmployee = reportAccessScopeService.getCurrentEmployeeRequired();
        String orgPathPrefix = reportAccessScopeService.getManagerOrgPathPrefixRequired();
        MapSqlParameterSource params = params()
                .addValue("orgPathPrefix", orgPathPrefix)
                .addValue("today", LocalDate.now())
                .addValue("fromDate", LocalDate.now())
                .addValue("toDate", LocalDate.now().plusDays(30));

        Long teamHeadcount = count("""
                select count(1)
                from dbo.hr_employee e
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where e.is_deleted = 0
                  and o.path_code like :orgPathPrefix + '%'
                  and e.employment_status in ('PROBATION', 'ACTIVE', 'SUSPENDED')
                """, params);
        Long presentTodayCount = count("""
                select count(1)
                from dbo.att_daily_attendance d
                inner join dbo.hr_employee e on d.employee_id = e.employee_id
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where d.is_deleted = 0
                  and e.is_deleted = 0
                  and d.attendance_date = :today
                  and o.path_code like :orgPathPrefix + '%'
                  and d.daily_status = 'PRESENT'
                """, params);
        Long onLeaveTodayCount = count("""
                select count(1)
                from dbo.att_daily_attendance d
                inner join dbo.hr_employee e on d.employee_id = e.employee_id
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where d.is_deleted = 0
                  and e.is_deleted = 0
                  and d.attendance_date = :today
                  and o.path_code like :orgPathPrefix + '%'
                  and d.on_leave = 1
                """, params);
        Long absentTodayCount = count("""
                select count(1)
                from dbo.att_daily_attendance d
                inner join dbo.hr_employee e on d.employee_id = e.employee_id
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where d.is_deleted = 0
                  and e.is_deleted = 0
                  and d.attendance_date = :today
                  and o.path_code like :orgPathPrefix + '%'
                  and d.daily_status = 'ABSENT'
                """, params);
        Long pendingLeaveApprovalCount = count("""
                select count(1)
                from dbo.lea_leave_request r
                inner join dbo.hr_employee e on r.employee_id = e.employee_id
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where r.is_deleted = 0
                  and e.is_deleted = 0
                  and r.request_status = 'SUBMITTED'
                  and r.approval_role_code = 'MANAGER'
                  and o.path_code like :orgPathPrefix + '%'
                """, params);
        Long pendingAttendanceAdjustmentCount = count("""
                select count(1)
                from dbo.att_adjustment_request r
                inner join dbo.hr_employee e on r.employee_id = e.employee_id
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where r.is_deleted = 0
                  and e.is_deleted = 0
                  and r.request_status = 'SUBMITTED'
                  and o.path_code like :orgPathPrefix + '%'
                """, params);
        Long pendingOvertimeCount = count("""
                select count(1)
                from dbo.att_overtime_request r
                inner join dbo.hr_employee e on r.employee_id = e.employee_id
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where r.is_deleted = 0
                  and e.is_deleted = 0
                  and r.request_status = 'SUBMITTED'
                  and o.path_code like :orgPathPrefix + '%'
                """, params);
        Long contractExpiring30DayCount = count("""
                select count(1)
                from dbo.ct_labor_contract c
                inner join dbo.hr_employee e on c.employee_id = e.employee_id
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where c.is_deleted = 0
                  and e.is_deleted = 0
                  and c.contract_status = 'ACTIVE'
                  and c.end_date is not null
                  and c.end_date between :fromDate and :toDate
                  and o.path_code like :orgPathPrefix + '%'
                """, params);
        Long openOffboardingCount = count("""
                select count(1)
                from dbo.off_offboarding_case oc
                inner join dbo.hr_employee e on oc.employee_id = e.employee_id
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where oc.is_deleted = 0
                  and e.is_deleted = 0
                  and oc.status not in ('CLOSED', 'CANCELLED', 'MANAGER_REJECTED')
                  and o.path_code like :orgPathPrefix + '%'
                """, params);

        List<DashboardBreakdownItemResponse> teamOrgBreakdown = jdbcTemplate.query("""
                select o.org_unit_code as orgUnitCode,
                       o.org_unit_name as orgUnitName,
                       count(1) as totalCount
                from dbo.hr_employee e
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where e.is_deleted = 0
                  and o.path_code like :orgPathPrefix + '%'
                  and e.employment_status in ('PROBATION', 'ACTIVE', 'SUSPENDED')
                group by o.org_unit_code, o.org_unit_name
                order by totalCount desc, o.org_unit_name asc
                """,
                params,
                (rs, rowNum) -> new DashboardBreakdownItemResponse(
                        rs.getString("orgUnitCode"),
                        rs.getString("orgUnitName"),
                        rs.getLong("totalCount")));

        List<DashboardBreakdownItemResponse> todayStatusBreakdown = jdbcTemplate.query("""
                select d.daily_status as statusCode, count(1) as totalCount
                from dbo.att_daily_attendance d
                inner join dbo.hr_employee e on d.employee_id = e.employee_id
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where d.is_deleted = 0
                  and e.is_deleted = 0
                  and d.attendance_date = :today
                  and o.path_code like :orgPathPrefix + '%'
                group by d.daily_status
                order by d.daily_status
                """,
                params,
                (rs, rowNum) -> new DashboardBreakdownItemResponse(
                        rs.getString("statusCode"),
                        rs.getString("statusCode"),
                        rs.getLong("totalCount")));

        return new TeamDashboardResponse(
                managerEmployee.getEmployeeId(),
                managerEmployee.getEmployeeCode(),
                managerEmployee.getFullName(),
                teamHeadcount,
                presentTodayCount,
                onLeaveTodayCount,
                absentTodayCount,
                pendingLeaveApprovalCount,
                pendingAttendanceAdjustmentCount,
                pendingOvertimeCount,
                contractExpiring30DayCount,
                openOffboardingCount,
                teamOrgBreakdown,
                todayStatusBreakdown);
    }

    @Transactional(readOnly = true)
    public String exportOrgMovementCsv(LocalDate fromDate, LocalDate toDate, Long orgUnitId) {
        LocalDate resolvedFrom = defaultFromDate(fromDate);
        LocalDate resolvedTo = defaultToDate(toDate);
        String orgPathPrefix = resolveOrgPathPrefix(orgUnitId);
        MapSqlParameterSource params = params()
                .addValue("fromDate", resolvedFrom)
                .addValue("toDate", resolvedTo)
                .addValue("orgPathPrefix", orgPathPrefix);
        return exportQueryAsCsv("""
                select movement_type as movementType,
                       movement_date as movementDate,
                       employee_code as employeeCode,
                       employee_name as employeeName,
                       org_unit_code as orgUnitCode,
                       org_unit_name as orgUnitName,
                       job_title_code as jobTitleCode,
                       job_title_name as jobTitleName,
                       reference_code as referenceCode,
                       movement_status as movementStatus
                from (
                    select 'JOIN' as movement_type,
                           e.hire_date as movement_date,
                           e.employee_code,
                           e.full_name as employee_name,
                           o.org_unit_code,
                           o.org_unit_name,
                           j.job_title_code,
                           j.job_title_name,
                           e.employee_code as reference_code,
                           e.employment_status as movement_status
                    from dbo.hr_employee e
                    inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                    inner join dbo.hr_job_title j on e.job_title_id = j.job_title_id
                    where e.is_deleted = 0
                      and e.hire_date between :fromDate and :toDate
                      and (:orgPathPrefix is null or o.path_code like :orgPathPrefix + '%')

                    union all

                    select 'LEAVE' as movement_type,
                           oc.effective_last_working_date as movement_date,
                           e.employee_code,
                           e.full_name as employee_name,
                           o.org_unit_code,
                           o.org_unit_name,
                           j.job_title_code,
                           j.job_title_name,
                           oc.offboarding_code as reference_code,
                           oc.status as movement_status
                    from dbo.off_offboarding_case oc
                    inner join dbo.hr_employee e on oc.employee_id = e.employee_id
                    inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                    inner join dbo.hr_job_title j on e.job_title_id = j.job_title_id
                    where oc.is_deleted = 0
                      and e.is_deleted = 0
                      and oc.effective_last_working_date between :fromDate and :toDate
                      and oc.status in ('HR_FINALIZED', 'ACCESS_REVOKED', 'SETTLEMENT_PREPARED', 'CLOSED')
                      and (:orgPathPrefix is null or o.path_code like :orgPathPrefix + '%')
                ) t
                order by movementDate asc, employeeCode asc
                """, params);
    }

    @Transactional(readOnly = true)
    public String exportContractExpiryCsv(LocalDate fromDate, LocalDate toDate, Long orgUnitId) {
        LocalDate resolvedFrom = defaultFromDate(fromDate);
        LocalDate resolvedTo = defaultToDate(toDate == null ? LocalDate.now().plusDays(60) : toDate);
        String orgPathPrefix = resolveOrgPathPrefix(orgUnitId);
        return exportQueryAsCsv("""
                select c.contract_number as contractNumber,
                       ct.contract_type_code as contractTypeCode,
                       ct.contract_type_name as contractTypeName,
                       e.employee_code as employeeCode,
                       e.full_name as employeeName,
                       o.org_unit_code as orgUnitCode,
                       o.org_unit_name as orgUnitName,
                       j.job_title_code as jobTitleCode,
                       j.job_title_name as jobTitleName,
                       c.effective_date as effectiveDate,
                       c.end_date as endDate,
                       c.contract_status as contractStatus,
                       c.base_salary as baseSalary,
                       c.salary_currency as salaryCurrency
                from dbo.ct_labor_contract c
                inner join dbo.hr_employee e on c.employee_id = e.employee_id
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                inner join dbo.hr_job_title j on c.job_title_id = j.job_title_id
                inner join dbo.ct_contract_type ct on c.contract_type_id = ct.contract_type_id
                where c.is_deleted = 0
                  and e.is_deleted = 0
                  and c.contract_status = 'ACTIVE'
                  and c.end_date is not null
                  and c.end_date between :fromDate and :toDate
                  and (:orgPathPrefix is null or o.path_code like :orgPathPrefix + '%')
                order by c.end_date asc, e.employee_code asc
                """, params()
                .addValue("fromDate", resolvedFrom)
                .addValue("toDate", resolvedTo)
                .addValue("orgPathPrefix", orgPathPrefix));
    }

    @Transactional(readOnly = true)
    public String exportLeaveBalanceCsv(Integer leaveYear, Long orgUnitId) {
        int resolvedYear = leaveYear == null ? LocalDate.now().getYear() : leaveYear;
        String orgPathPrefix = resolveOrgPathPrefix(orgUnitId);
        return exportQueryAsCsv("""
                select lb.leave_year as leaveYear,
                       e.employee_code as employeeCode,
                       e.full_name as employeeName,
                       o.org_unit_code as orgUnitCode,
                       o.org_unit_name as orgUnitName,
                       lt.leave_type_code as leaveTypeCode,
                       lt.leave_type_name as leaveTypeName,
                       lb.opening_units as openingUnits,
                       lb.accrued_units as accruedUnits,
                       lb.used_units as usedUnits,
                       lb.adjusted_units as adjustedUnits,
                       lb.carried_forward_units as carriedForwardUnits,
                       lb.settled_units as settledUnits,
                       lb.available_units as availableUnits,
                       lb.balance_status as balanceStatus
                from dbo.lea_leave_balance lb
                inner join dbo.hr_employee e on lb.employee_id = e.employee_id
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                inner join dbo.lea_leave_type lt on lb.leave_type_id = lt.leave_type_id
                where lb.is_deleted = 0
                  and e.is_deleted = 0
                  and lt.is_deleted = 0
                  and lb.leave_year = :leaveYear
                  and (:orgPathPrefix is null or o.path_code like :orgPathPrefix + '%')
                order by e.employee_code asc, lt.leave_type_code asc
                """, params()
                .addValue("leaveYear", resolvedYear)
                .addValue("orgPathPrefix", orgPathPrefix));
    }

    @Transactional(readOnly = true)
    public String exportAttendanceAnomalyOtCsv(LocalDate fromDate, LocalDate toDate, Long orgUnitId) {
        LocalDate resolvedFrom = defaultFromDate(fromDate);
        LocalDate resolvedTo = defaultToDate(toDate);
        String orgPathPrefix = resolveOrgPathPrefix(orgUnitId);
        return exportQueryAsCsv("""
                select d.attendance_date as attendanceDate,
                       e.employee_code as employeeCode,
                       e.full_name as employeeName,
                       o.org_unit_code as orgUnitCode,
                       o.org_unit_name as orgUnitName,
                       d.daily_status as dailyStatus,
                       d.anomaly_codes as anomalyCodes,
                       d.late_minutes as lateMinutes,
                       d.early_leave_minutes as earlyLeaveMinutes,
                       d.missing_check_in as missingCheckIn,
                       d.missing_check_out as missingCheckOut,
                       d.approved_ot_minutes as approvedOtMinutes,
                       d.on_leave as onLeave
                from dbo.att_daily_attendance d
                inner join dbo.hr_employee e on d.employee_id = e.employee_id
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where d.is_deleted = 0
                  and e.is_deleted = 0
                  and d.attendance_date between :fromDate and :toDate
                  and (:orgPathPrefix is null or o.path_code like :orgPathPrefix + '%')
                  and (d.anomaly_count > 0 or d.approved_ot_minutes > 0)
                order by d.attendance_date asc, e.employee_code asc
                """, params()
                .addValue("fromDate", resolvedFrom)
                .addValue("toDate", resolvedTo)
                .addValue("orgPathPrefix", orgPathPrefix));
    }

    @Transactional(readOnly = true)
    public String exportPayrollSummaryCsv(Long payrollPeriodId) {
        if (payrollPeriodId == null) {
            throw new BusinessException("REPORT_PAYROLL_PERIOD_REQUIRED", "payrollPeriodId là bắt buộc.",
                    HttpStatus.BAD_REQUEST);
        }
        return exportQueryAsCsv("""
                select pp.period_code as periodCode,
                       e.employee_code as employeeCode,
                       e.full_name as employeeName,
                       o.org_unit_code as orgUnitCode,
                       o.org_unit_name as orgUnitName,
                       i.scheduled_day_count as scheduledDayCount,
                       i.present_day_count as presentDayCount,
                       i.paid_leave_day_count as paidLeaveDayCount,
                       i.unpaid_leave_day_count as unpaidLeaveDayCount,
                       i.absent_day_count as absentDayCount,
                       i.approved_ot_minutes as approvedOtMinutes,
                       i.base_salary_monthly as baseSalaryMonthly,
                       i.base_salary_prorated as baseSalaryProrated,
                       i.fixed_earning_total as fixedEarningTotal,
                       i.fixed_deduction_total as fixedDeductionTotal,
                       i.employee_insurance_amount as employeeInsuranceAmount,
                       i.taxable_income as taxableIncome,
                       i.pit_amount as pitAmount,
                       i.gross_income as grossIncome,
                       i.net_pay as netPay,
                       i.item_status as itemStatus
                from dbo.pay_payroll_item i
                inner join dbo.pay_payroll_period pp on i.payroll_period_id = pp.payroll_period_id
                inner join dbo.hr_employee e on i.employee_id = e.employee_id
                inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                where i.is_deleted = 0
                  and pp.is_deleted = 0
                  and e.is_deleted = 0
                  and pp.payroll_period_id = :payrollPeriodId
                order by e.employee_code asc
                """, params().addValue("payrollPeriodId", payrollPeriodId));
    }

    @Transactional(readOnly = true)
    public String exportPitCsv(Long payrollPeriodId) {
        if (payrollPeriodId == null) {
            throw new BusinessException("REPORT_PAYROLL_PERIOD_REQUIRED", "payrollPeriodId là bắt buộc.",
                    HttpStatus.BAD_REQUEST);
        }
        return payrollService.exportPitReportCsv(payrollPeriodId);
    }

    @Transactional(readOnly = true)
    public String exportOnboardingOffboardingCsv(LocalDate fromDate, LocalDate toDate, Long orgUnitId) {
        LocalDate resolvedFrom = defaultFromDate(fromDate);
        LocalDate resolvedTo = defaultToDate(toDate);
        String orgPathPrefix = resolveOrgPathPrefix(orgUnitId);
        return exportQueryAsCsv("""
                select process_type as processType,
                       reference_code as referenceCode,
                       employee_code as employeeCode,
                       full_name as fullName,
                       org_unit_code as orgUnitCode,
                       org_unit_name as orgUnitName,
                       status_code as statusCode,
                       key_date as keyDate,
                       note as note
                from (
                    select 'ONBOARDING' as process_type,
                           o.onboarding_code as reference_code,
                           o.employee_code as employee_code,
                           o.full_name,
                           ou.org_unit_code,
                           ou.org_unit_name,
                           o.status as status_code,
                           o.planned_start_date as key_date,
                           o.note
                    from dbo.onb_onboarding o
                    inner join dbo.hr_org_unit ou on o.org_unit_id = ou.org_unit_id
                    where o.is_deleted = 0
                      and o.planned_start_date between :fromDate and :toDate
                      and (:orgPathPrefix is null or ou.path_code like :orgPathPrefix + '%')

                    union all

                    select 'OFFBOARDING' as process_type,
                           oc.offboarding_code as reference_code,
                           e.employee_code,
                           e.full_name,
                           ou.org_unit_code,
                           ou.org_unit_name,
                           oc.status as status_code,
                           oc.effective_last_working_date as key_date,
                           oc.request_reason as note
                    from dbo.off_offboarding_case oc
                    inner join dbo.hr_employee e on oc.employee_id = e.employee_id
                    inner join dbo.hr_org_unit ou on e.org_unit_id = ou.org_unit_id
                    where oc.is_deleted = 0
                      and e.is_deleted = 0
                      and oc.request_date between :fromDate and :toDate
                      and (:orgPathPrefix is null or ou.path_code like :orgPathPrefix + '%')
                ) t
                order by keyDate asc, processType asc, referenceCode asc
                """, params()
                .addValue("fromDate", resolvedFrom)
                .addValue("toDate", resolvedTo)
                .addValue("orgPathPrefix", orgPathPrefix));
    }

    @Transactional(readOnly = true)
    public String exportAuditCsv(String moduleCode, String actionCode, String resultCode, String actorUsername,
            LocalDateTime from, LocalDateTime to) {
        return exportQueryAsCsv("""
                select action_at as actionAt,
                       actor_username as actorUsername,
                       module_code as moduleCode,
                       action_code as actionCode,
                       entity_name as entityName,
                       entity_id as entityId,
                       request_id as requestId,
                       ip_address as ipAddress,
                       result_code as resultCode,
                       message as message
                from dbo.sys_audit_log
                where (:moduleCode is null or module_code = :moduleCode)
                  and (:actionCode is null or action_code = :actionCode)
                  and (:resultCode is null or result_code = :resultCode)
                  and (:actorUsername is null or lower(actor_username) like '%' + lower(:actorUsername) + '%')
                  and (:fromValue is null or action_at >= :fromValue)
                  and (:toValue is null or action_at <= :toValue)
                order by action_at desc, audit_log_id desc
                """, params()
                .addValue("moduleCode", trimToNullUpper(moduleCode))
                .addValue("actionCode", trimToNullUpper(actionCode))
                .addValue("resultCode", trimToNullUpper(resultCode))
                .addValue("actorUsername", trimToNull(actorUsername))
                .addValue("fromValue", from == null ? null : Timestamp.valueOf(from))
                .addValue("toValue", to == null ? null : Timestamp.valueOf(to)));
    }

    @Transactional(readOnly = true)
    public SystemHealthDashboardResponse getSystemHealthDashboard() {
        Long profileChangePending = count(
                "select count(1) from dbo.hr_employee_profile_change_request where is_deleted = 0 and request_status = 'PENDING'");
        Long leavePending = count(
                "select count(1) from dbo.lea_leave_request where is_deleted = 0 and request_status in ('SUBMITTED','APPROVED')");
        Long attendanceAdjustmentPending = count(
                "select count(1) from dbo.att_adjustment_request where is_deleted = 0 and request_status in ('SUBMITTED','APPROVED')");
        Long overtimePending = count(
                "select count(1) from dbo.att_overtime_request where is_deleted = 0 and request_status = 'SUBMITTED'");
        Long payrollPending = count(
                "select count(1) from dbo.pay_payroll_period where is_deleted = 0 and period_status in ('DRAFT','TEAM_REVIEW')");
        Long onboardingPending = count(
                "select count(1) from dbo.onb_onboarding where is_deleted = 0 and status in ('DRAFT','IN_PROGRESS','READY_FOR_JOIN')");
        Long offboardingPending = count(
                "select count(1) from dbo.off_offboarding_case where is_deleted = 0 and status not in ('CLOSED','CANCELLED','MANAGER_REJECTED')");

        List<HealthMetricResponse> metrics = List.of(
                new HealthMetricResponse("PROFILE_CHANGE_PENDING", "Profile change chờ xử lý", profileChangePending,
                        severity(profileChangePending, 1L, 5L)),
                new HealthMetricResponse("LEAVE_PENDING", "Đơn nghỉ chờ xử lý", leavePending,
                        severity(leavePending, 5L, 20L)),
                new HealthMetricResponse("ATTENDANCE_ADJUSTMENT_PENDING", "Điều chỉnh công chờ xử lý",
                        attendanceAdjustmentPending, severity(attendanceAdjustmentPending, 5L, 20L)),
                new HealthMetricResponse("OVERTIME_PENDING", "OT chờ xử lý", overtimePending,
                        severity(overtimePending, 5L, 20L)),
                new HealthMetricResponse("PAYROLL_PENDING", "Kỳ lương chưa hoàn tất", payrollPending,
                        severity(payrollPending, 1L, 3L)),
                new HealthMetricResponse("ONBOARDING_PENDING", "Onboarding đang mở", onboardingPending,
                        severity(onboardingPending, 3L, 10L)),
                new HealthMetricResponse("OFFBOARDING_PENDING", "Offboarding đang mở", offboardingPending,
                        severity(offboardingPending, 1L, 5L)));

        Long overdueChecklistCount = count("""
                select count(1) from dbo.off_offboarding_checklist_item
                where is_deleted = 0
                  and due_date < :today
                  and status in ('OPEN','IN_PROGRESS')
                """, params().addValue("today", LocalDate.now()))
                + count("""
                        select count(1) from dbo.onb_onboarding_checklist
                        where is_deleted = 0
                          and due_date < :today
                          and is_completed = 0
                        """, params().addValue("today", LocalDate.now()));

        Long storageFileCount = count("select count(1) from dbo.sys_stored_file where is_deleted = 0");
        Long missingStorageBinaryCount = countMissingStoredBinaries();
        Long auditFailureLast24hCount = count("""
                select count(1)
                from dbo.sys_audit_log
                where result_code = 'FAILED'
                  and action_at >= :fromValue
                """, params().addValue("fromValue", Timestamp.valueOf(LocalDateTime.now().minusHours(24))));
        Long scheduledReportActiveCount = count("""
                select count(1)
                from dbo.rep_report_schedule_config
                where is_deleted = 0 and status = 'ACTIVE'
                """);
        Long scheduledReportFailedLast7dCount = count("""
                select count(1)
                from dbo.rep_report_schedule_run
                where is_deleted = 0
                  and run_status = 'FAILED'
                  and started_at >= :fromValue
                """, params().addValue("fromValue", Timestamp.valueOf(LocalDateTime.now().minusDays(7))));

        return new SystemHealthDashboardResponse(
                LocalDateTime.now(),
                count("select count(1) from dbo.sec_user_account where is_deleted = 0 and status = 'ACTIVE'"),
                count("select count(1) from dbo.sec_user_account where is_deleted = 0 and status = 'LOCKED'"),
                count("select count(1) from dbo.sec_auth_session where status = 'ACTIVE'"),
                storageFileCount,
                missingStorageBinaryCount,
                auditFailureLast24hCount,
                metrics.stream().mapToLong(item -> item.metricValue() == null ? 0L : item.metricValue()).sum(),
                overdueChecklistCount,
                scheduledReportActiveCount,
                scheduledReportFailedLast7dCount,
                metrics);
    }

    @Transactional(readOnly = true)
    public List<ReportScheduleConfigResponse> listSchedules() {
        return reportScheduleConfigRepository.findAllByDeletedFalseOrderByScheduleCodeAsc()
                .stream()
                .map(this::toReportScheduleConfigResponse)
                .toList();
    }

    @Transactional
    public ReportScheduleConfigResponse createSchedule(ReportScheduleConfigUpsertRequest request) {
        validateScheduleRequest(request);
        if (reportScheduleConfigRepository
                .existsByScheduleCodeIgnoreCaseAndDeletedFalse(request.scheduleCode().trim())) {
            throw new BusinessException("REPORT_SCHEDULE_CODE_EXISTS", "Mã lịch báo cáo đã tồn tại.",
                    HttpStatus.CONFLICT);
        }
        RepReportScheduleConfig entity = new RepReportScheduleConfig();
        applySchedule(entity, request);
        entity = reportScheduleConfigRepository.save(entity);
        ReportScheduleConfigResponse response = toReportScheduleConfigResponse(entity);
        auditLogService.logSuccess("CREATE", "REPORT_SCHEDULE", "rep_report_schedule_config",
                entity.getReportScheduleConfigId().toString(), null, response, "Tạo lịch xuất báo cáo định kỳ.");
        return response;
    }

    @Transactional
    public ReportScheduleConfigResponse updateSchedule(Long reportScheduleConfigId,
            ReportScheduleConfigUpsertRequest request) {
        validateScheduleRequest(request);
        RepReportScheduleConfig entity = getSchedule(reportScheduleConfigId);
        ReportScheduleConfigResponse oldSnapshot = toReportScheduleConfigResponse(entity);
        if (reportScheduleConfigRepository.existsByScheduleCodeIgnoreCaseAndDeletedFalseAndReportScheduleConfigIdNot(
                request.scheduleCode().trim(), reportScheduleConfigId)) {
            throw new BusinessException("REPORT_SCHEDULE_CODE_EXISTS", "Mã lịch báo cáo đã tồn tại.",
                    HttpStatus.CONFLICT);
        }
        applySchedule(entity, request);
        entity = reportScheduleConfigRepository.save(entity);
        ReportScheduleConfigResponse response = toReportScheduleConfigResponse(entity);
        auditLogService.logSuccess("UPDATE", "REPORT_SCHEDULE", "rep_report_schedule_config",
                entity.getReportScheduleConfigId().toString(), oldSnapshot, response,
                "Cập nhật lịch xuất báo cáo định kỳ.");
        return response;
    }

    @Transactional(readOnly = true)
    public List<ReportScheduleRunResponse> listScheduleRuns(Long reportScheduleConfigId) {
        getSchedule(reportScheduleConfigId);
        return reportScheduleRunRepository
                .findAllByReportScheduleConfigReportScheduleConfigIdAndDeletedFalseOrderByStartedAtDescReportScheduleRunIdDesc(
                        reportScheduleConfigId)
                .stream()
                .map(this::toReportScheduleRunResponse)
                .toList();
    }

    @Transactional
    public ReportScheduleRunResponse runScheduleNow(Long reportScheduleConfigId) {
        RepReportScheduleConfig config = getSchedule(reportScheduleConfigId);
        SecUserAccount currentUser = reportAccessScopeService.getCurrentUserRequired();
        ReportScheduleRunResponse response = executeSchedule(config, ReportRunTriggerType.MANUAL, currentUser);
        auditLogService.logSuccess("RUN_NOW", "REPORT_SCHEDULE", "rep_report_schedule_run",
                Objects.toString(response.reportScheduleRunId(), null), null, response,
                "Thực thi lịch báo cáo thủ công.");
        return response;
    }

    @Transactional
    public void processDueSchedules() {
        if (!appProperties.getReporting().isSchedulerEnabled()) {
            return;
        }
        List<RepReportScheduleConfig> dueConfigs = reportScheduleConfigRepository
                .findAllByDeletedFalseAndStatusAndNextRunAtLessThanEqualOrderByNextRunAtAsc(RecordStatus.ACTIVE,
                        LocalDateTime.now());
        for (RepReportScheduleConfig config : dueConfigs) {
            try {
                executeSchedule(config, ReportRunTriggerType.SCHEDULED, null);
            } catch (Exception exception) {
                auditLogService.logSystemFailure("SYSTEM", "RUN_FAILED", "REPORT_SCHEDULE",
                        "rep_report_schedule_config",
                        config.getReportScheduleConfigId().toString(), null, null,
                        "Thực thi lịch báo cáo thất bại: " + exception.getMessage());
            }
        }
    }

    private ReportScheduleRunResponse executeSchedule(RepReportScheduleConfig config, ReportRunTriggerType triggerType,
            SecUserAccount triggeredByUser) {
        RepReportScheduleRun run = new RepReportScheduleRun();
        run.setReportScheduleConfig(config);
        run.setTriggerType(triggerType);
        run.setTriggeredByUser(triggeredByUser);
        run.setStartedAt(LocalDateTime.now());
        run.setRunStatus(ReportRunStatus.RUNNING);
        run = reportScheduleRunRepository.save(run);

        try {
            ExportArtifact artifact = generateExportArtifact(config.getReportCode(), config.getParameterJson());
            StoredFileResponse storedFile = storageFileService.storeInternalFile(
                    artifact.fileName(),
                    "REPORTING",
                    "SCHEDULED_EXPORT",
                    StorageVisibilityScope.INTERNAL,
                    "Generated by report schedule " + config.getScheduleCode(),
                    artifact.csvContent().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    "text/csv");

            run.setRunStatus(ReportRunStatus.SUCCESS);
            run.setOutputFileKey(storedFile.fileKey());
            run.setOutputFileName(artifact.fileName());
            run.setOutputRowCount(artifact.rowCount());
            run.setRunMessage("Tạo file báo cáo thành công.");
            run.setFinishedAt(LocalDateTime.now());
            run = reportScheduleRunRepository.save(run);

            config.setLastRunAt(run.getFinishedAt());
            config.setLastRunStatus(ReportRunStatus.SUCCESS);
            config.setLastRunMessage(run.getRunMessage());
            config.setNextRunAt(calculateNextRunAt(LocalDateTime.now(), config.getFrequencyCode(),
                    config.getDayOfWeek(), config.getDayOfMonth(), config.getRunAtHour(), config.getRunAtMinute()));
            reportScheduleConfigRepository.save(config);

            for (String email : parseRecipients(config.getRecipientEmailsCsv())) {
                mailService.sendSimpleMail(
                        email,
                        "[Digital HRM] Scheduled report " + config.getScheduleName(),
                        """
                                Báo cáo định kỳ đã được tạo thành công.

                                Schedule code: %s
                                Report code: %s
                                File key: %s
                                Download URL: /api/v1/storage/files/%s/download
                                Rows: %s
                                """.formatted(config.getScheduleCode(), config.getReportCode().name(),
                                storedFile.fileKey(), storedFile.fileKey(), artifact.rowCount()));
            }

            ReportScheduleRunResponse response = toReportScheduleRunResponse(run);
            auditLogService.logSystemSuccess(
                    triggerType == ReportRunTriggerType.SCHEDULED ? "SYSTEM"
                            : reportAccessScopeService.getCurrentUsername().orElse("UNKNOWN"),
                    "RUN", "REPORT_SCHEDULE", "rep_report_schedule_run", run.getReportScheduleRunId().toString(), null,
                    response, "Thực thi lịch báo cáo thành công.");
            return response;
        } catch (Exception exception) {
            run.setRunStatus(ReportRunStatus.FAILED);
            run.setRunMessage(limit(exception.getMessage(), 1000));
            run.setFinishedAt(LocalDateTime.now());
            run = reportScheduleRunRepository.save(run);

            config.setLastRunAt(run.getFinishedAt());
            config.setLastRunStatus(ReportRunStatus.FAILED);
            config.setLastRunMessage(run.getRunMessage());
            config.setNextRunAt(calculateNextRunAt(LocalDateTime.now(), config.getFrequencyCode(),
                    config.getDayOfWeek(), config.getDayOfMonth(), config.getRunAtHour(), config.getRunAtMinute()));
            reportScheduleConfigRepository.save(config);
            throw exception instanceof RuntimeException runtimeException
                    ? runtimeException
                    : new BusinessException("REPORT_SCHEDULE_RUN_FAILED", "Thực thi lịch báo cáo thất bại.",
                            HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ExportArtifact generateExportArtifact(ReportCode reportCode, String parameterJson) {
        JsonNode node = parseParameterJson(parameterJson);
        return switch (reportCode) {
            case ORG_MOVEMENT -> buildArtifact("org-movement.csv",
                    exportOrgMovementCsv(readLocalDate(node, "fromDate"), readLocalDate(node, "toDate"),
                            readLong(node, "orgUnitId")));
            case CONTRACT_EXPIRY -> buildArtifact("contract-expiry.csv",
                    exportContractExpiryCsv(readLocalDate(node, "fromDate"), readLocalDate(node, "toDate"),
                            readLong(node, "orgUnitId")));
            case LEAVE_BALANCE -> buildArtifact("leave-balance.csv",
                    exportLeaveBalanceCsv(readInteger(node, "leaveYear"), readLong(node, "orgUnitId")));
            case ATTENDANCE_ANOMALY_OT -> buildArtifact("attendance-anomaly-ot.csv",
                    exportAttendanceAnomalyOtCsv(readLocalDate(node, "fromDate"), readLocalDate(node, "toDate"),
                            readLong(node, "orgUnitId")));
            case PAYROLL_SUMMARY -> buildArtifact("payroll-summary.csv",
                    exportPayrollSummaryCsv(readLong(node, "payrollPeriodId")));
            case PIT -> buildArtifact("payroll-pit.csv",
                    exportPitCsv(readLong(node, "payrollPeriodId")));
            case ONBOARDING_OFFBOARDING -> buildArtifact("onboarding-offboarding.csv",
                    exportOnboardingOffboardingCsv(readLocalDate(node, "fromDate"), readLocalDate(node, "toDate"),
                            readLong(node, "orgUnitId")));
            case AUDIT_LOG -> buildArtifact("audit-log.csv",
                    exportAuditCsv(
                            readString(node, "moduleCode"),
                            readString(node, "actionCode"),
                            readString(node, "resultCode"),
                            readString(node, "actorUsername"),
                            readLocalDateTime(node, "from"),
                            readLocalDateTime(node, "to")));
        };
    }

    private ExportArtifact buildArtifact(String fileName, String csvContent) {
        int rowCount = 0;
        if (csvContent != null && !csvContent.isBlank()) {
            rowCount = Math.max(0, csvContent.split("\\R").length - 1);
        }
        return new ExportArtifact(fileName, csvContent == null ? "" : csvContent, rowCount);
    }

    private void applySchedule(RepReportScheduleConfig entity, ReportScheduleConfigUpsertRequest request) {
        entity.setScheduleCode(request.scheduleCode().trim().toUpperCase(Locale.ROOT));
        entity.setScheduleName(request.scheduleName().trim());
        entity.setReportCode(request.reportCode());
        entity.setFrequencyCode(request.frequencyCode());
        entity.setDayOfWeek(request.dayOfWeek());
        entity.setDayOfMonth(request.dayOfMonth());
        entity.setRunAtHour(request.runAtHour());
        entity.setRunAtMinute(request.runAtMinute());
        entity.setRecipientEmailsCsv(joinRecipients(request.recipientEmails()));
        entity.setParameterJson(normalizeJson(request.parameterJson()));
        entity.setStatus(request.status());
        entity.setDescription(trimToNull(request.description()));
        entity.setNextRunAt(request.status() == RecordStatus.ACTIVE
                ? calculateNextRunAt(LocalDateTime.now(), request.frequencyCode(), request.dayOfWeek(),
                        request.dayOfMonth(), request.runAtHour(), request.runAtMinute())
                : null);
    }

    private void validateScheduleRequest(ReportScheduleConfigUpsertRequest request) {
        if (request.frequencyCode() == ReportScheduleFrequency.WEEKLY && request.dayOfWeek() == null) {
            throw new BusinessException("REPORT_SCHEDULE_DAY_OF_WEEK_REQUIRED",
                    "dayOfWeek là bắt buộc với lịch WEEKLY.", HttpStatus.BAD_REQUEST);
        }
        if (request.frequencyCode() == ReportScheduleFrequency.MONTHLY && request.dayOfMonth() == null) {
            throw new BusinessException("REPORT_SCHEDULE_DAY_OF_MONTH_REQUIRED",
                    "dayOfMonth là bắt buộc với lịch MONTHLY.", HttpStatus.BAD_REQUEST);
        }
        parseParameterJson(request.parameterJson());
    }

    private LocalDateTime calculateNextRunAt(LocalDateTime baseTime, ReportScheduleFrequency frequencyCode,
            Integer dayOfWeek, Integer dayOfMonth, Integer runAtHour, Integer runAtMinute) {
        LocalDateTime normalizedBase = baseTime.withSecond(0).withNano(0);
        return switch (frequencyCode) {
            case DAILY -> {
                LocalDateTime candidate = normalizedBase.toLocalDate().atTime(runAtHour, runAtMinute);
                if (!candidate.isAfter(normalizedBase)) {
                    candidate = candidate.plusDays(1);
                }
                yield candidate;
            }
            case WEEKLY -> {
                DayOfWeek targetDay = DayOfWeek.of(dayOfWeek);
                LocalDateTime candidate = normalizedBase.toLocalDate()
                        .with(TemporalAdjusters.nextOrSame(targetDay))
                        .atTime(runAtHour, runAtMinute);
                if (!candidate.isAfter(normalizedBase)) {
                    candidate = candidate.plusWeeks(1);
                }
                yield candidate;
            }
            case MONTHLY -> {
                YearMonth currentMonth = YearMonth.from(normalizedBase);
                LocalDate candidateDate = currentMonth.atDay(Math.min(dayOfMonth, currentMonth.lengthOfMonth()));
                LocalDateTime candidate = candidateDate.atTime(runAtHour, runAtMinute);
                if (!candidate.isAfter(normalizedBase)) {
                    YearMonth nextMonth = currentMonth.plusMonths(1);
                    candidate = nextMonth.atDay(Math.min(dayOfMonth, nextMonth.lengthOfMonth())).atTime(runAtHour,
                            runAtMinute);
                }
                yield candidate;
            }
        };
    }

    private List<DashboardTrendPointResponse> buildMonthlyMovement(LocalDate startMonth, int monthCount,
            String orgPathPrefix) {
        List<DashboardTrendPointResponse> items = new ArrayList<>();
        for (int i = 0; i < monthCount; i++) {
            LocalDate monthStart = startMonth.plusMonths(i);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            MapSqlParameterSource params = params()
                    .addValue("fromDate", monthStart)
                    .addValue("toDate", monthEnd)
                    .addValue("orgPathPrefix", orgPathPrefix);
            Long joinerCount = count("""
                    select count(1)
                    from dbo.hr_employee e
                    inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                    where e.is_deleted = 0
                      and e.hire_date between :fromDate and :toDate
                      and (:orgPathPrefix is null or o.path_code like :orgPathPrefix + '%')
                    """, params);
            Long leaverCount = count("""
                    select count(1)
                    from dbo.off_offboarding_case oc
                    inner join dbo.hr_employee e on oc.employee_id = e.employee_id
                    inner join dbo.hr_org_unit o on e.org_unit_id = o.org_unit_id
                    where oc.is_deleted = 0
                      and e.is_deleted = 0
                      and oc.effective_last_working_date between :fromDate and :toDate
                      and oc.status in ('HR_FINALIZED', 'ACCESS_REVOKED', 'SETTLEMENT_PREPARED', 'CLOSED')
                      and (:orgPathPrefix is null or o.path_code like :orgPathPrefix + '%')
                    """, params);
            items.add(new DashboardTrendPointResponse(monthStart.getMonthValue() + "/" + monthStart.getYear(),
                    joinerCount, leaverCount));
        }
        return items;
    }

    private Long countMissingStoredBinaries() {
        List<String> relativePaths = jdbcTemplate.query("""
                select storage_path
                from dbo.sys_stored_file
                where is_deleted = 0
                """,
                params(),
                (rs, rowNum) -> rs.getString("storage_path"));
        Path baseDir = Paths.get(appProperties.getStorage().getBaseDir()).toAbsolutePath().normalize();
        long missing = 0;
        for (String relativePath : relativePaths) {
            if (relativePath == null || relativePath.isBlank()) {
                missing++;
                continue;
            }
            Path absolutePath = baseDir.resolve(relativePath).normalize();
            if (!absolutePath.startsWith(baseDir) || !Files.exists(absolutePath)
                    || !Files.isRegularFile(absolutePath)) {
                missing++;
            }
        }
        return missing;
    }

    private String exportQueryAsCsv(String sql, MapSqlParameterSource params) {
        return jdbcTemplate.query(sql, params, rs -> {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) {
                    builder.append(',');
                }
                builder.append(csv(metaData.getColumnLabel(i)));
            }
            builder.append('\n');

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) {
                        builder.append(',');
                    }
                    Object value = rs.getObject(i);
                    builder.append(csv(value));
                }
                builder.append('\n');
            }
            return builder.toString();
        });
    }

    private MapSqlParameterSource params() {
        return new MapSqlParameterSource();
    }

    private Long count(String sql) {
        return count(sql, params());
    }

    private Long count(String sql, MapSqlParameterSource params) {
        Long value = jdbcTemplate.queryForObject(sql, params, Long.class);
        return value == null ? 0L : value;
    }

    private String resolveOrgPathPrefix(Long orgUnitId) {
        if (orgUnitId == null) {
            return null;
        }
        try {
            return jdbcTemplate.queryForObject("""
                    select path_code
                    from dbo.hr_org_unit
                    where org_unit_id = :orgUnitId
                      and is_deleted = 0
                    """, params().addValue("orgUnitId", orgUnitId), String.class);
        } catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("ORG_UNIT_NOT_FOUND", "Không tìm thấy đơn vị tổ chức.");
        }
    }

    private RepReportScheduleConfig getSchedule(Long reportScheduleConfigId) {
        return reportScheduleConfigRepository.findByReportScheduleConfigIdAndDeletedFalse(reportScheduleConfigId)
                .orElseThrow(() -> new NotFoundException("REPORT_SCHEDULE_NOT_FOUND", "Không tìm thấy lịch báo cáo."));
    }

    private ReportScheduleConfigResponse toReportScheduleConfigResponse(RepReportScheduleConfig entity) {
        return new ReportScheduleConfigResponse(
                entity.getReportScheduleConfigId(),
                entity.getScheduleCode(),
                entity.getScheduleName(),
                entity.getReportCode().name(),
                entity.getFrequencyCode().name(),
                entity.getDayOfWeek(),
                entity.getDayOfMonth(),
                entity.getRunAtHour(),
                entity.getRunAtMinute(),
                parseRecipients(entity.getRecipientEmailsCsv()),
                entity.getParameterJson(),
                entity.getStatus().name(),
                entity.getNextRunAt(),
                entity.getLastRunAt(),
                entity.getLastRunStatus() == null ? null : entity.getLastRunStatus().name(),
                entity.getLastRunMessage(),
                entity.getDescription());
    }

    private ReportScheduleRunResponse toReportScheduleRunResponse(RepReportScheduleRun entity) {
        return new ReportScheduleRunResponse(
                entity.getReportScheduleRunId(),
                entity.getReportScheduleConfig().getReportScheduleConfigId(),
                entity.getReportScheduleConfig().getScheduleCode(),
                entity.getTriggerType().name(),
                entity.getRunStatus().name(),
                entity.getOutputFileKey(),
                entity.getOutputFileName(),
                entity.getOutputFileKey() == null ? null
                        : "/api/v1/storage/files/" + entity.getOutputFileKey() + "/download",
                entity.getOutputRowCount(),
                entity.getRunMessage(),
                entity.getStartedAt(),
                entity.getFinishedAt());
    }

    private List<String> parseRecipients(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String joinRecipients(List<String> emails) {
        return emails.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(","));
    }

    private JsonNode parseParameterJson(String parameterJson) {
        try {
            if (parameterJson == null || parameterJson.isBlank()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(parameterJson);
        } catch (Exception exception) {
            throw new BusinessException("REPORT_PARAMETER_JSON_INVALID", "parameterJson không phải JSON hợp lệ.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizeJson(String parameterJson) {
        if (parameterJson == null || parameterJson.isBlank()) {
            return null;
        }
        JsonNode node = parseParameterJson(parameterJson);
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception exception) {
            throw new BusinessException("REPORT_PARAMETER_JSON_INVALID", "parameterJson không thể chuẩn hóa.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private LocalDate readLocalDate(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull() || field.asText().isBlank()) {
            return null;
        }
        return LocalDate.parse(field.asText());
    }

    private LocalDateTime readLocalDateTime(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull() || field.asText().isBlank()) {
            return null;
        }
        return LocalDateTime.parse(field.asText());
    }

    private Long readLong(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull() || field.asText().isBlank()) {
            return null;
        }
        return field.asLong();
    }

    private Integer readInteger(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull() || field.asText().isBlank()) {
            return null;
        }
        return field.asInt();
    }

    private String readString(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        return trimToNull(field.asText());
    }

    private LocalDate defaultFromDate(LocalDate value) {
        return value == null ? LocalDate.now().withDayOfMonth(1) : value;
    }

    private LocalDate defaultToDate(LocalDate value) {
        return value == null ? LocalDate.now() : value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String trimToNullUpper(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String severity(Long value, Long mediumThreshold, Long highThreshold) {
        long resolved = value == null ? 0L : value;
        if (resolved >= highThreshold) {
            return "HIGH";
        }
        if (resolved >= mediumThreshold) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString().replace("\"", "\"\"");
        return "\"" + text + "\"";
    }

    private record ExportArtifact(
            String fileName,
            String csvContent,
            Integer rowCount) {
    }
}
