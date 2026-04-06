package com.company.hrm.module.payroll.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.hrm.common.constant.AttendanceDailyStatus;
import com.company.hrm.common.constant.AttendancePeriodStatus;
import com.company.hrm.common.constant.ContractStatus;
import com.company.hrm.common.constant.EmploymentStatus;
import com.company.hrm.common.constant.LeaveRequestStatus;
import com.company.hrm.common.constant.PayrollAmountType;
import com.company.hrm.common.constant.PayrollComponentCategory;
import com.company.hrm.common.constant.PayrollItemStatus;
import com.company.hrm.common.constant.PayrollLineSourceType;
import com.company.hrm.common.constant.PayrollPeriodStatus;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.TaxFinalizationMethod;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.ForbiddenException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.response.PageResponse;
import com.company.hrm.module.attendance.entity.AttAttendancePeriod;
import com.company.hrm.module.attendance.entity.AttDailyAttendance;
import com.company.hrm.module.attendance.repository.AttAttendancePeriodRepository;
import com.company.hrm.module.attendance.repository.AttDailyAttendanceRepository;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.contract.entity.CtLaborContract;
import com.company.hrm.module.contract.repository.CtLaborContractRepository;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.leave.repository.LeaLeaveRequestRepository;
import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import com.company.hrm.module.orgunit.repository.HrOrgUnitRepository;
import com.company.hrm.module.payroll.dto.AdjustPayrollItemRequest;
import com.company.hrm.module.payroll.dto.ApprovePayrollPeriodRequest;
import com.company.hrm.module.payroll.dto.DeactivateTaxDependentRequest;
import com.company.hrm.module.payroll.dto.EmployeeCompensationItemRequest;
import com.company.hrm.module.payroll.dto.EmployeeCompensationItemResponse;
import com.company.hrm.module.payroll.dto.EmployeeCompensationListItemResponse;
import com.company.hrm.module.payroll.dto.EmployeeCompensationResponse;
import com.company.hrm.module.payroll.dto.EmployeeCompensationUpsertRequest;
import com.company.hrm.module.payroll.dto.FormulaTaxBracketRequest;
import com.company.hrm.module.payroll.dto.FormulaTaxBracketResponse;
import com.company.hrm.module.payroll.dto.FormulaVersionResponse;
import com.company.hrm.module.payroll.dto.FormulaVersionUpsertRequest;
import com.company.hrm.module.payroll.dto.GeneratePayrollDraftRequest;
import com.company.hrm.module.payroll.dto.ManagerConfirmPayrollItemRequest;
import com.company.hrm.module.payroll.dto.PayrollItemAdjustmentLineRequest;
import com.company.hrm.module.payroll.dto.PayrollItemLineResponse;
import com.company.hrm.module.payroll.dto.PayrollItemResponse;
import com.company.hrm.module.payroll.dto.PayrollPeriodCreateRequest;
import com.company.hrm.module.payroll.dto.PayrollPeriodResponse;
import com.company.hrm.module.payroll.dto.PersonalTaxProfileResponse;
import com.company.hrm.module.payroll.dto.PersonalTaxProfileUpsertRequest;
import com.company.hrm.module.payroll.dto.PublishPayrollPeriodRequest;
import com.company.hrm.module.payroll.dto.SalaryComponentResponse;
import com.company.hrm.module.payroll.dto.SalaryComponentUpsertRequest;
import com.company.hrm.module.payroll.dto.SelfPayslipListItemResponse;
import com.company.hrm.module.payroll.dto.TaxDependentResponse;
import com.company.hrm.module.payroll.dto.TaxDependentUpsertRequest;
import com.company.hrm.module.payroll.entity.PayEmployeeCompensation;
import com.company.hrm.module.payroll.entity.PayEmployeeCompensationItem;
import com.company.hrm.module.payroll.entity.PayFormulaTaxBracket;
import com.company.hrm.module.payroll.entity.PayFormulaVersion;
import com.company.hrm.module.payroll.entity.PayPayrollItem;
import com.company.hrm.module.payroll.entity.PayPayrollItemLine;
import com.company.hrm.module.payroll.entity.PayPayrollPeriod;
import com.company.hrm.module.payroll.entity.PayPersonalTaxProfile;
import com.company.hrm.module.payroll.entity.PaySalaryComponent;
import com.company.hrm.module.payroll.entity.PayTaxDependent;
import com.company.hrm.module.payroll.repository.PayEmployeeCompensationItemRepository;
import com.company.hrm.module.payroll.repository.PayEmployeeCompensationRepository;
import com.company.hrm.module.payroll.repository.PayFormulaTaxBracketRepository;
import com.company.hrm.module.payroll.repository.PayFormulaVersionRepository;
import com.company.hrm.module.payroll.repository.PayPayrollItemLineRepository;
import com.company.hrm.module.payroll.repository.PayPayrollItemRepository;
import com.company.hrm.module.payroll.repository.PayPayrollPeriodRepository;
import com.company.hrm.module.payroll.repository.PayPersonalTaxProfileRepository;
import com.company.hrm.module.payroll.repository.PaySalaryComponentRepository;
import com.company.hrm.module.payroll.repository.PayTaxDependentRepository;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.security.SecurityUserContext;

@Service
public class PayrollService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int SCALE_MONEY = 2;
    private static final String LINE_BASE = "BASE_SALARY";
    private static final String LINE_OT = "OT_WEEKDAY";
    private static final String LINE_SOCIAL = "INS_SOCIAL";
    private static final String LINE_HEALTH = "INS_HEALTH";
    private static final String LINE_UNEMP = "INS_UNEMP";
    private static final String LINE_PIT = "PIT";

    private final PaySalaryComponentRepository salaryComponentRepository;
    private final PayFormulaVersionRepository formulaVersionRepository;
    private final PayFormulaTaxBracketRepository formulaTaxBracketRepository;
    private final PayEmployeeCompensationRepository compensationRepository;
    private final PayEmployeeCompensationItemRepository compensationItemRepository;
    private final PayPayrollPeriodRepository payrollPeriodRepository;
    private final PayPayrollItemRepository payrollItemRepository;
    private final PayPayrollItemLineRepository payrollItemLineRepository;
    private final PayPersonalTaxProfileRepository personalTaxProfileRepository;
    private final PayTaxDependentRepository taxDependentRepository;
    private final HrEmployeeRepository employeeRepository;
    private final HrOrgUnitRepository orgUnitRepository;
    private final CtLaborContractRepository laborContractRepository;
    private final LeaLeaveRequestRepository leaveRequestRepository;
    private final AttAttendancePeriodRepository attendancePeriodRepository;
    private final AttDailyAttendanceRepository dailyAttendanceRepository;
    private final SecUserAccountRepository userAccountRepository;
    private final AuditLogService auditLogService;
    private final PayrollAccessScopeService accessScopeService;

    public PayrollService(
            PaySalaryComponentRepository salaryComponentRepository,
            PayFormulaVersionRepository formulaVersionRepository,
            PayFormulaTaxBracketRepository formulaTaxBracketRepository,
            PayEmployeeCompensationRepository compensationRepository,
            PayEmployeeCompensationItemRepository compensationItemRepository,
            PayPayrollPeriodRepository payrollPeriodRepository,
            PayPayrollItemRepository payrollItemRepository,
            PayPayrollItemLineRepository payrollItemLineRepository,
            PayPersonalTaxProfileRepository personalTaxProfileRepository,
            PayTaxDependentRepository taxDependentRepository,
            HrEmployeeRepository employeeRepository,
            HrOrgUnitRepository orgUnitRepository,
            CtLaborContractRepository laborContractRepository,
            LeaLeaveRequestRepository leaveRequestRepository,
            AttAttendancePeriodRepository attendancePeriodRepository,
            AttDailyAttendanceRepository dailyAttendanceRepository,
            SecUserAccountRepository userAccountRepository,
            AuditLogService auditLogService,
            PayrollAccessScopeService accessScopeService) {
        this.salaryComponentRepository = salaryComponentRepository;
        this.formulaVersionRepository = formulaVersionRepository;
        this.formulaTaxBracketRepository = formulaTaxBracketRepository;
        this.compensationRepository = compensationRepository;
        this.compensationItemRepository = compensationItemRepository;
        this.payrollPeriodRepository = payrollPeriodRepository;
        this.payrollItemRepository = payrollItemRepository;
        this.payrollItemLineRepository = payrollItemLineRepository;
        this.personalTaxProfileRepository = personalTaxProfileRepository;
        this.taxDependentRepository = taxDependentRepository;
        this.employeeRepository = employeeRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.laborContractRepository = laborContractRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.attendancePeriodRepository = attendancePeriodRepository;
        this.dailyAttendanceRepository = dailyAttendanceRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditLogService = auditLogService;
        this.accessScopeService = accessScopeService;
    }

    @Transactional(readOnly = true)
    public PageResponse<SalaryComponentResponse> listComponents(String keyword, RecordStatus status, int page,
            int size) {
        Specification<PaySalaryComponent> specification = (root, query, builder) -> builder
                .isFalse(root.get("deleted"));
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("componentCode")), like),
                    builder.like(builder.lower(root.get("componentName")), like)));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }
        Page<PaySalaryComponent> result = salaryComponentRepository.findAll(specification,
                PageRequest.of(page, size,
                        Sort.by("displayOrder").ascending().and(Sort.by("componentCode").ascending())));
        return toPageResponse(result, result.getContent().stream().map(this::toSalaryComponentResponse).toList(), page,
                size);
    }

    @Transactional(readOnly = true)
    public SalaryComponentResponse getComponent(Long salaryComponentId) {
        return toSalaryComponentResponse(getSalaryComponent(salaryComponentId));
    }

    @Transactional
    public SalaryComponentResponse upsertComponent(Long salaryComponentId, SalaryComponentUpsertRequest request) {
        PaySalaryComponent entity;
        SalaryComponentResponse oldSnapshot = null;
        if (salaryComponentId == null) {
            if (salaryComponentRepository
                    .existsByComponentCodeIgnoreCaseAndDeletedFalse(request.componentCode().trim())) {
                throw new BusinessException("PAYROLL_COMPONENT_CODE_EXISTS", "Mã thành phần lương đã tồn tại.",
                        HttpStatus.CONFLICT);
            }
            entity = new PaySalaryComponent();
        } else {
            entity = getSalaryComponent(salaryComponentId);
            oldSnapshot = toSalaryComponentResponse(entity);
            if (salaryComponentRepository.existsByComponentCodeIgnoreCaseAndDeletedFalseAndSalaryComponentIdNot(
                    request.componentCode().trim(), salaryComponentId)) {
                throw new BusinessException("PAYROLL_COMPONENT_CODE_EXISTS", "Mã thành phần lương đã tồn tại.",
                        HttpStatus.CONFLICT);
            }
        }
        entity.setComponentCode(request.componentCode().trim().toUpperCase(Locale.ROOT));
        entity.setComponentName(request.componentName().trim());
        entity.setComponentCategory(request.componentCategory());
        entity.setAmountType(request.amountType());
        entity.setTaxable(request.taxable());
        entity.setInsuranceBaseIncluded(request.insuranceBaseIncluded());
        entity.setPayslipVisible(request.payslipVisible());
        entity.setDisplayOrder(request.displayOrder());
        entity.setDescription(trimToNull(request.description()));
        entity.setStatus(RecordStatus.ACTIVE);
        entity = salaryComponentRepository.save(entity);
        SalaryComponentResponse response = toSalaryComponentResponse(entity);
        auditLogService.logSuccess(salaryComponentId == null ? "CREATE" : "UPDATE", "PAYROLL_COMPONENT",
                "pay_salary_component",
                entity.getSalaryComponentId().toString(), oldSnapshot, response,
                salaryComponentId == null ? "Tạo thành phần lương." : "Cập nhật thành phần lương.");
        return response;
    }

    @Transactional(readOnly = true)
    public List<FormulaVersionResponse> listFormulaVersions() {
        return formulaVersionRepository.findAll((root, query, builder) -> builder.isFalse(root.get("deleted")),
                Sort.by("effectiveFrom").descending().and(Sort.by("formulaVersionId").descending()))
                .stream().map(this::toFormulaVersionResponse).toList();
    }

    @Transactional(readOnly = true)
    public FormulaVersionResponse getFormulaVersion(Long formulaVersionId) {
        return toFormulaVersionResponse(getFormulaVersionEntity(formulaVersionId));
    }

    @Transactional
    public FormulaVersionResponse upsertFormulaVersion(Long formulaVersionId, FormulaVersionUpsertRequest request) {
        validateFormulaRequest(request);
        PayFormulaVersion entity;
        FormulaVersionResponse oldSnapshot = null;
        if (formulaVersionId == null) {
            if (formulaVersionRepository.existsByFormulaCodeIgnoreCaseAndDeletedFalse(request.formulaCode().trim())) {
                throw new BusinessException("PAYROLL_FORMULA_CODE_EXISTS", "Mã cấu hình công thức lương đã tồn tại.",
                        HttpStatus.CONFLICT);
            }
            entity = new PayFormulaVersion();
        } else {
            entity = getFormulaVersionEntity(formulaVersionId);
            oldSnapshot = toFormulaVersionResponse(entity);
            if (formulaVersionRepository.existsByFormulaCodeIgnoreCaseAndDeletedFalseAndFormulaVersionIdNot(
                    request.formulaCode().trim(), formulaVersionId)) {
                throw new BusinessException("PAYROLL_FORMULA_CODE_EXISTS", "Mã cấu hình công thức lương đã tồn tại.",
                        HttpStatus.CONFLICT);
            }
        }

        entity.setFormulaCode(request.formulaCode().trim().toUpperCase(Locale.ROOT));
        entity.setFormulaName(request.formulaName().trim());
        entity.setEffectiveFrom(request.effectiveFrom());
        entity.setEffectiveTo(request.effectiveTo());
        entity.setPersonalDeductionMonthly(scale(request.personalDeductionMonthly()));
        entity.setDependentDeductionMonthly(scale(request.dependentDeductionMonthly()));
        entity.setSocialInsuranceEmployeeRate(request.socialInsuranceEmployeeRate());
        entity.setHealthInsuranceEmployeeRate(request.healthInsuranceEmployeeRate());
        entity.setUnemploymentInsuranceEmployeeRate(request.unemploymentInsuranceEmployeeRate());
        entity.setInsuranceSalaryCapAmount(scaleNullable(request.insuranceSalaryCapAmount()));
        entity.setUnemploymentSalaryCapAmount(scaleNullable(request.unemploymentSalaryCapAmount()));
        entity.setStandardWorkHoursPerDay(request.standardWorkHoursPerDay());
        entity.setOvertimeMultiplierWeekday(request.overtimeMultiplierWeekday());
        entity.setNote(trimToNull(request.note()));
        entity.setStatus(RecordStatus.ACTIVE);
        entity = formulaVersionRepository.save(entity);

        List<PayFormulaTaxBracket> oldBrackets = formulaTaxBracketRepository
                .findAllByFormulaVersionFormulaVersionIdAndDeletedFalseOrderBySequenceNoAsc(
                        entity.getFormulaVersionId());
        for (PayFormulaTaxBracket bracket : oldBrackets) {
            bracket.setDeleted(true);
        }
        formulaTaxBracketRepository.saveAll(oldBrackets);

        List<PayFormulaTaxBracket> newBrackets = new ArrayList<>();
        for (FormulaTaxBracketRequest bracketRequest : request.taxBrackets()) {
            PayFormulaTaxBracket bracket = new PayFormulaTaxBracket();
            bracket.setFormulaVersion(entity);
            bracket.setSequenceNo(bracketRequest.sequenceNo());
            bracket.setIncomeFrom(scale(bracketRequest.incomeFrom()));
            bracket.setIncomeTo(scaleNullable(bracketRequest.incomeTo()));
            bracket.setTaxRate(bracketRequest.taxRate());
            bracket.setQuickDeduction(scale(bracketRequest.quickDeduction()));
            newBrackets.add(bracket);
        }
        formulaTaxBracketRepository.saveAll(newBrackets);

        FormulaVersionResponse response = toFormulaVersionResponse(entity);
        auditLogService.logSuccess(formulaVersionId == null ? "CREATE" : "UPDATE", "PAYROLL_FORMULA",
                "pay_formula_version",
                entity.getFormulaVersionId().toString(), oldSnapshot, response,
                formulaVersionId == null ? "Tạo cấu hình công thức lương." : "Cập nhật cấu hình công thức lương.");
        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeCompensationListItemResponse> listCompensations(String keyword, Long orgUnitId,
            int page, int size) {
        Specification<PayEmployeeCompensation> specification = (root, query, builder) -> builder
                .isFalse(root.get("deleted"));
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.join("employee").get("employeeCode")), like),
                    builder.like(builder.lower(root.join("employee").get("fullName")), like)));
        }
        if (orgUnitId != null) {
            HrOrgUnit orgUnit = getOrgUnit(orgUnitId);
            specification = specification.and((root, query, builder) -> builder
                    .like(root.join("employee").join("orgUnit").get("pathCode"), orgUnit.getPathCode() + "%"));
        }
        Page<PayEmployeeCompensation> result = compensationRepository.findAll(specification,
                PageRequest.of(page, size,
                        Sort.by("effectiveFrom").descending().and(Sort.by("employeeCompensationId").descending())));
        List<EmployeeCompensationListItemResponse> items = result.getContent().stream()
                .map(this::toCompensationListItem).toList();
        return toPageResponse(result, items, page, size);
    }

    @Transactional(readOnly = true)
    public EmployeeCompensationResponse getEmployeeCompensation(Long employeeId) {
        PayEmployeeCompensation compensation = compensationRepository
                .findAllByEmployeeEmployeeIdAndDeletedFalseOrderByEffectiveFromDescEmployeeCompensationIdDesc(
                        employeeId)
                .stream().findFirst().orElse(null);
        HrEmployee employee = getEmployee(employeeId);
        if (compensation == null) {
            CtLaborContract activeContract = findActiveContract(employeeId, LocalDate.now())
                    .orElseThrow(() -> new NotFoundException("PAYROLL_COMPENSATION_NOT_FOUND",
                            "Nhân viên chưa có cấu hình lương và cũng không có hợp đồng hiệu lực."));
            return new EmployeeCompensationResponse(
                    null,
                    employee.getEmployeeId(),
                    employee.getEmployeeCode(),
                    employee.getFullName(),
                    activeContract.getEffectiveDate(),
                    activeContract.getEndDate(),
                    scale(activeContract.getBaseSalary()),
                    scale(activeContract.getBaseSalary()),
                    activeContract.getSalaryCurrency(),
                    null,
                    null,
                    null,
                    null,
                    RecordStatus.ACTIVE.name(),
                    List.of());
        }
        return toEmployeeCompensationResponse(compensation);
    }

    @Transactional
    public EmployeeCompensationResponse upsertEmployeeCompensation(Long employeeId,
            EmployeeCompensationUpsertRequest request) {
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new BusinessException("PAYROLL_COMPENSATION_DATE_INVALID",
                    "effectiveTo không được nhỏ hơn effectiveFrom.", HttpStatus.BAD_REQUEST);
        }
        LocalDate effectiveTo = request.effectiveTo() == null ? LocalDate.of(9999, 12, 31) : request.effectiveTo();
        if (compensationRepository.existsOverlap(employeeId, null, request.effectiveFrom(), effectiveTo)) {
            List<PayEmployeeCompensation> current = compensationRepository.findEffectiveByEmployeeAndDate(employeeId,
                    request.effectiveFrom());
            if (!current.isEmpty()) {
                for (PayEmployeeCompensation compensation : current) {
                    compensation.setDeleted(true);
                }
                compensationRepository.saveAll(current);
            }
        }

        HrEmployee employee = getEmployee(employeeId);
        PayEmployeeCompensation entity = new PayEmployeeCompensation();
        entity.setEmployee(employee);
        entity.setEffectiveFrom(request.effectiveFrom());
        entity.setEffectiveTo(request.effectiveTo());
        entity.setBaseSalary(scale(request.baseSalary()));
        entity.setInsuranceSalaryBase(scaleNullable(request.insuranceSalaryBase()));
        entity.setSalaryCurrency(blankToDefault(request.salaryCurrency(), "VND").toUpperCase(Locale.ROOT));
        entity.setBankAccountName(trimToNull(request.bankAccountName()));
        entity.setBankAccountNo(trimToNull(request.bankAccountNo()));
        entity.setBankName(trimToNull(request.bankName()));
        entity.setPaymentNote(trimToNull(request.paymentNote()));
        entity.setStatus(RecordStatus.ACTIVE);
        entity = compensationRepository.save(entity);

        if (request.items() != null) {
            validateCompensationItems(request.items());
            List<PayEmployeeCompensationItem> items = new ArrayList<>();
            for (EmployeeCompensationItemRequest itemRequest : request.items()) {
                PaySalaryComponent component = getSalaryComponent(itemRequest.salaryComponentId());
                PayEmployeeCompensationItem item = new PayEmployeeCompensationItem();
                item.setEmployeeCompensation(entity);
                item.setSalaryComponent(component);
                item.setAmountValue(scaleNullable(itemRequest.amountValue()));
                item.setPercentageValue(itemRequest.percentageValue());
                item.setNote(trimToNull(itemRequest.note()));
                items.add(item);
            }
            compensationItemRepository.saveAll(items);
        }

        EmployeeCompensationResponse response = toEmployeeCompensationResponse(entity);
        auditLogService.logSuccess("UPSERT", "PAYROLL_COMPENSATION", "pay_employee_compensation",
                employeeId.toString(), null, response, "Thiết lập lương cho nhân viên.");
        return response;
    }

    @Transactional(readOnly = true)
    public PersonalTaxProfileResponse getPersonalTaxProfile(Long employeeId) {
        HrEmployee employee = getEmployee(employeeId);
        PayPersonalTaxProfile profile = personalTaxProfileRepository.findByEmployeeEmployeeIdAndDeletedFalse(employeeId)
                .orElse(null);
        if (profile == null) {
            return new PersonalTaxProfileResponse(
                    null,
                    employee.getEmployeeId(),
                    employee.getEmployeeCode(),
                    employee.getFullName(),
                    true,
                    true,
                    null,
                    TaxFinalizationMethod.MONTHLY_WITHHOLDING.name(),
                    RecordStatus.ACTIVE.name(),
                    null,
                    List.of());
        }
        return toPersonalTaxProfileResponse(profile);
    }

    @Transactional
    public PersonalTaxProfileResponse upsertPersonalTaxProfile(Long employeeId,
            PersonalTaxProfileUpsertRequest request) {
        HrEmployee employee = getEmployee(employeeId);
        PayPersonalTaxProfile profile = personalTaxProfileRepository.findByEmployeeEmployeeIdAndDeletedFalse(employeeId)
                .orElseGet(PayPersonalTaxProfile::new);
        boolean creating = profile.getPersonalTaxProfileId() == null;
        PersonalTaxProfileResponse oldSnapshot = creating ? null : toPersonalTaxProfileResponse(profile);
        profile.setEmployee(employee);
        profile.setResidentTaxpayer(request.residentTaxpayer());
        profile.setFamilyDeductionEnabled(request.familyDeductionEnabled());
        profile.setTaxRegistrationDate(request.taxRegistrationDate());
        profile.setTaxFinalizationMethod(request.taxFinalizationMethod());
        profile.setNote(trimToNull(request.note()));
        profile.setStatus(RecordStatus.ACTIVE);
        profile = personalTaxProfileRepository.save(profile);
        PersonalTaxProfileResponse response = toPersonalTaxProfileResponse(profile);
        auditLogService.logSuccess(creating ? "CREATE" : "UPDATE", "PAYROLL_TAX_PROFILE", "pay_personal_tax_profile",
                employeeId.toString(), oldSnapshot, response, "Thiết lập hồ sơ thuế cá nhân.");
        return response;
    }

    @Transactional
    public TaxDependentResponse addDependent(Long employeeId, TaxDependentUpsertRequest request) {
        validateDependentDates(request.deductionStartMonth(), request.deductionEndMonth());
        HrEmployee employee = getEmployee(employeeId);
        PayTaxDependent entity = new PayTaxDependent();
        entity.setEmployee(employee);
        entity.setFullName(request.fullName().trim());
        entity.setRelationshipCode(request.relationshipCode());
        entity.setIdentificationNo(trimToNull(request.identificationNo()));
        entity.setDateOfBirth(request.dateOfBirth());
        entity.setDeductionStartMonth(request.deductionStartMonth().withDayOfMonth(1));
        entity.setDeductionEndMonth(
                request.deductionEndMonth() == null ? null : request.deductionEndMonth().withDayOfMonth(1));
        entity.setStatus(RecordStatus.ACTIVE);
        entity.setNote(trimToNull(request.note()));
        entity = taxDependentRepository.save(entity);
        TaxDependentResponse response = toTaxDependentResponse(entity);
        auditLogService.logSuccess("CREATE", "PAYROLL_TAX_DEPENDENT", "pay_tax_dependent",
                entity.getTaxDependentId().toString(), null, response, "Thêm người phụ thuộc.");
        return response;
    }

    @Transactional
    public TaxDependentResponse updateDependent(Long taxDependentId, TaxDependentUpsertRequest request) {
        validateDependentDates(request.deductionStartMonth(), request.deductionEndMonth());
        PayTaxDependent entity = getTaxDependent(taxDependentId);
        TaxDependentResponse oldSnapshot = toTaxDependentResponse(entity);
        entity.setFullName(request.fullName().trim());
        entity.setRelationshipCode(request.relationshipCode());
        entity.setIdentificationNo(trimToNull(request.identificationNo()));
        entity.setDateOfBirth(request.dateOfBirth());
        entity.setDeductionStartMonth(request.deductionStartMonth().withDayOfMonth(1));
        entity.setDeductionEndMonth(
                request.deductionEndMonth() == null ? null : request.deductionEndMonth().withDayOfMonth(1));
        entity.setNote(trimToNull(request.note()));
        entity = taxDependentRepository.save(entity);
        TaxDependentResponse response = toTaxDependentResponse(entity);
        auditLogService.logSuccess("UPDATE", "PAYROLL_TAX_DEPENDENT", "pay_tax_dependent",
                entity.getTaxDependentId().toString(), oldSnapshot, response, "Cập nhật người phụ thuộc.");
        return response;
    }

    @Transactional
    public TaxDependentResponse deactivateDependent(Long taxDependentId, DeactivateTaxDependentRequest request) {
        PayTaxDependent entity = getTaxDependent(taxDependentId);
        TaxDependentResponse oldSnapshot = toTaxDependentResponse(entity);
        if (request.deductionEndMonth().isBefore(entity.getDeductionStartMonth())) {
            throw new BusinessException("PAYROLL_DEPENDENT_END_MONTH_INVALID",
                    "deductionEndMonth không được nhỏ hơn deductionStartMonth.", HttpStatus.BAD_REQUEST);
        }
        entity.setDeductionEndMonth(request.deductionEndMonth().withDayOfMonth(1));
        entity.setStatus(RecordStatus.INACTIVE);
        entity.setNote(trimToNull(request.note()));
        entity = taxDependentRepository.save(entity);
        TaxDependentResponse response = toTaxDependentResponse(entity);
        auditLogService.logSuccess("DEACTIVATE", "PAYROLL_TAX_DEPENDENT", "pay_tax_dependent",
                entity.getTaxDependentId().toString(), oldSnapshot, response,
                "Ngừng hiệu lực giảm trừ người phụ thuộc.");
        return response;
    }

    @Transactional(readOnly = true)
    public List<PayrollPeriodResponse> listPayrollPeriods() {
        return payrollPeriodRepository.findAllByDeletedFalseOrderByPeriodYearDescPeriodMonthDesc().stream()
                .map(this::toPayrollPeriodResponse)
                .toList();
    }

    @Transactional
    public PayrollPeriodResponse createPayrollPeriod(PayrollPeriodCreateRequest request) {
        YearMonth ym = YearMonth.of(request.periodYear(), request.periodMonth());
        AttAttendancePeriod attendancePeriod = attendancePeriodRepository
                .findByPeriodYearAndPeriodMonthAndDeletedFalse(request.periodYear(), request.periodMonth())
                .orElseThrow(() -> new NotFoundException("PAYROLL_ATTENDANCE_PERIOD_NOT_FOUND",
                        "Không tìm thấy kỳ công tương ứng để tạo kỳ lương."));
        if (attendancePeriod.getPeriodStatus() != AttendancePeriodStatus.CLOSED) {
            throw new BusinessException("PAYROLL_ATTENDANCE_PERIOD_NOT_CLOSED",
                    "Chỉ được tạo kỳ lương từ kỳ công đã chốt.", HttpStatus.CONFLICT);
        }
        PayFormulaVersion formulaVersion = resolveFormulaVersion(ym.atEndOfMonth());
        PayPayrollPeriod period = payrollPeriodRepository
                .findByPeriodYearAndPeriodMonthAndDeletedFalse(request.periodYear(), request.periodMonth())
                .orElseGet(PayPayrollPeriod::new);
        period.setPeriodCode(String.format("PAY-%04d%02d", request.periodYear(), request.periodMonth()));
        period.setPeriodYear(request.periodYear());
        period.setPeriodMonth(request.periodMonth());
        period.setPeriodStartDate(ym.atDay(1));
        period.setPeriodEndDate(ym.atEndOfMonth());
        period.setAttendancePeriod(attendancePeriod);
        period.setFormulaVersion(formulaVersion);
        period.setNote(trimToNull(request.note()));
        if (period.getPayrollPeriodId() == null) {
            period.setPeriodStatus(PayrollPeriodStatus.DRAFT);
        }
        period = payrollPeriodRepository.save(period);
        PayrollPeriodResponse response = toPayrollPeriodResponse(period);
        auditLogService.logSuccess("CREATE", "PAYROLL_PERIOD", "pay_payroll_period",
                period.getPayrollPeriodId().toString(), null, response, "Tạo kỳ lương.");
        return response;
    }

    @Transactional
    public PayrollPeriodResponse generatePayrollDraft(Long payrollPeriodId, GeneratePayrollDraftRequest request) {
        PayPayrollPeriod period = getPayrollPeriod(payrollPeriodId);
        if (period.getPeriodStatus() == PayrollPeriodStatus.APPROVED
                || period.getPeriodStatus() == PayrollPeriodStatus.PUBLISHED) {
            throw new BusinessException("PAYROLL_PERIOD_GENERATE_STATUS_INVALID",
                    "Không thể tạo nháp lại cho kỳ lương đã duyệt hoặc đã phát hành.", HttpStatus.CONFLICT);
        }
        List<PayPayrollItem> existingItems = payrollItemRepository
                .findAllByPayrollPeriodPayrollPeriodIdAndDeletedFalseOrderByEmployeeEmployeeCodeAscPayrollItemIdAsc(
                        payrollPeriodId);
        if (!existingItems.isEmpty() && !request.regenerate()) {
            throw new BusinessException("PAYROLL_DRAFT_ALREADY_EXISTS",
                    "Kỳ lương đã có bảng lương nháp. Dùng regenerate=true nếu muốn tính lại.", HttpStatus.CONFLICT);
        }
        if (!existingItems.isEmpty()) {
            markPayrollItemsDeleted(existingItems);
        }

        PayFormulaVersion formulaVersion = resolveFormulaVersion(period.getPeriodEndDate());
        period.setFormulaVersion(formulaVersion);

        List<HrEmployee> employees = findEligibleEmployees(period.getPeriodEndDate());
        validateEmployeesHaveContracts(employees, period.getPeriodEndDate());

        int totalEmployeeCount = 0;
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        for (HrEmployee employee : employees) {
            PayrollComputationResult computation = computePayrollForEmployee(period, employee, formulaVersion);
            if (computation == null) {
                continue;
            }
            totalEmployeeCount++;
            totalGross = totalGross.add(computation.item().getGrossIncome());
            totalNet = totalNet.add(computation.item().getNetPay());
        }

        long managerConfirmedCount = payrollItemRepository
                .countByPayrollPeriodPayrollPeriodIdAndItemStatusAndDeletedFalse(payrollPeriodId,
                        PayrollItemStatus.MANAGER_CONFIRMED);
        period.setGeneratedAt(LocalDateTime.now());
        period.setGeneratedBy(getCurrentUserOrNull());
        period.setPeriodStatus(PayrollPeriodStatus.DRAFT);
        period.setTotalEmployeeCount(totalEmployeeCount);
        period.setManagerConfirmedCount((int) managerConfirmedCount);
        period.setTotalGrossAmount(scale(totalGross));
        period.setTotalNetAmount(scale(totalNet));
        if (trimToNull(request.note()) != null) {
            period.setNote(trimToNull(request.note()));
        }
        period = payrollPeriodRepository.save(period);

        PayrollPeriodResponse response = toPayrollPeriodResponse(period);
        auditLogService.logSuccess("GENERATE_DRAFT", "PAYROLL_PERIOD", "pay_payroll_period",
                period.getPayrollPeriodId().toString(), null, response, "Tạo bảng lương nháp.");
        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<PayrollItemResponse> listPayrollItemsForHr(Long payrollPeriodId, String keyword, Long orgUnitId,
            Long employeeId, int page, int size) {
        PayPayrollPeriod period = getPayrollPeriod(payrollPeriodId);
        Specification<PayPayrollItem> specification = (root, query, builder) -> builder.and(
                builder.isFalse(root.get("deleted")),
                builder.equal(root.join("payrollPeriod").get("payrollPeriodId"), period.getPayrollPeriodId()));
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.join("employee").get("employeeCode")), like),
                    builder.like(builder.lower(root.join("employee").get("fullName")), like)));
        }
        if (orgUnitId != null) {
            HrOrgUnit orgUnit = getOrgUnit(orgUnitId);
            specification = specification.and((root, query, builder) -> builder
                    .like(root.join("employee").join("orgUnit").get("pathCode"), orgUnit.getPathCode() + "%"));
        }
        if (employeeId != null) {
            specification = specification
                    .and((root, query, builder) -> builder.equal(root.join("employee").get("employeeId"), employeeId));
        }
        Page<PayPayrollItem> result = payrollItemRepository.findAll(specification,
                PageRequest.of(page, size,
                        Sort.by("employee.employeeCode").ascending().and(Sort.by("payrollItemId").ascending())));
        List<PayrollItemResponse> items = result.getContent().stream().map(this::toPayrollItemResponse).toList();
        return toPageResponse(result, items, page, size);
    }

    @Transactional(readOnly = true)
    public PayrollItemResponse getPayrollItem(Long payrollItemId) {
        return toPayrollItemResponse(getPayrollItemEntity(payrollItemId));
    }

    @Transactional
    public PayrollItemResponse adjustPayrollItem(Long payrollItemId, AdjustPayrollItemRequest request) {
        PayPayrollItem item = getPayrollItemEntity(payrollItemId);
        if (item.getPayrollPeriod().getPeriodStatus() == PayrollPeriodStatus.APPROVED
                || item.getPayrollPeriod().getPeriodStatus() == PayrollPeriodStatus.PUBLISHED) {
            throw new BusinessException("PAYROLL_ITEM_ADJUST_STATUS_INVALID",
                    "Không thể điều chỉnh dòng lương khi kỳ lương đã duyệt hoặc đã phát hành.", HttpStatus.CONFLICT);
        }
        PayrollItemResponse oldSnapshot = toPayrollItemResponse(item);

        List<PayPayrollItemLine> lines = payrollItemLineRepository
                .findAllByPayrollItemPayrollItemIdAndDeletedFalseOrderByDisplayOrderAscPayrollItemLineIdAsc(
                        item.getPayrollItemId());
        for (PayPayrollItemLine line : lines) {
            if (line.getLineSourceType() == PayrollLineSourceType.MANUAL_ADJUSTMENT) {
                line.setDeleted(true);
            }
        }
        payrollItemLineRepository.saveAll(lines);

        if (request.manualLines() != null) {
            for (PayrollItemAdjustmentLineRequest manualLine : request.manualLines()) {
                PayrollComponentCategory category = parseCategory(manualLine.componentCategory());
                PayPayrollItemLine line = new PayPayrollItemLine();
                line.setPayrollItem(item);
                line.setComponentCode(manualLine.componentCode().trim().toUpperCase(Locale.ROOT));
                line.setComponentName(manualLine.componentName().trim());
                line.setComponentCategory(category);
                line.setLineSourceType(PayrollLineSourceType.MANUAL_ADJUSTMENT);
                line.setLineAmount(scale(manualLine.lineAmount()));
                line.setTaxable(manualLine.taxable());
                line.setDisplayOrder(manualLine.displayOrder());
                line.setLineNote(trimToNull(manualLine.lineNote()));
                payrollItemLineRepository.save(line);
            }
        }

        item.setAdjustmentNote(request.adjustmentNote().trim());
        recalculateItemTotalsFromLines(item);
        item.setItemStatus(item.isManagerConfirmationRequired() ? PayrollItemStatus.DRAFT : PayrollItemStatus.DRAFT);
        item = payrollItemRepository.save(item);
        refreshPeriodAggregates(item.getPayrollPeriod());

        PayrollItemResponse response = toPayrollItemResponse(item);
        auditLogService.logSuccess("ADJUST", "PAYROLL_ITEM", "pay_payroll_item", item.getPayrollItemId().toString(),
                oldSnapshot, response, "Điều chỉnh bảng lương nhân sự.");
        return response;
    }

    @Transactional(readOnly = true)
    public List<PayrollItemResponse> listPayrollItemsForManager(Long payrollPeriodId) {
        PayPayrollPeriod period = getPayrollPeriod(payrollPeriodId);
        String orgPathPrefix = accessScopeService.getManagerOrgPathPrefix()
                .orElseThrow(() -> new ForbiddenException("PAYROLL_MANAGER_SCOPE_REQUIRED",
                        "Bạn không thuộc phạm vi quản lý để xem bảng lương team."));
        return payrollItemRepository.findByPayrollPeriodAndManagerScope(period.getPayrollPeriodId(), orgPathPrefix)
                .stream().map(this::toPayrollItemResponse).toList();
    }

    @Transactional
    public PayrollItemResponse confirmPayrollItemByManager(Long payrollItemId,
            ManagerConfirmPayrollItemRequest request) {
        PayPayrollItem item = getPayrollItemEntity(payrollItemId);
        accessScopeService.assertManagerCanAccessEmployee(item.getEmployee());
        if (!item.isManagerConfirmationRequired()) {
            throw new BusinessException("PAYROLL_MANAGER_CONFIRM_NOT_REQUIRED",
                    "Dòng lương này không yêu cầu quản lý xác nhận.", HttpStatus.CONFLICT);
        }
        if (item.getPayrollPeriod().getPeriodStatus() == PayrollPeriodStatus.APPROVED
                || item.getPayrollPeriod().getPeriodStatus() == PayrollPeriodStatus.PUBLISHED) {
            throw new BusinessException("PAYROLL_MANAGER_CONFIRM_STATUS_INVALID",
                    "Không thể xác nhận khi kỳ lương đã duyệt hoặc đã phát hành.", HttpStatus.CONFLICT);
        }
        PayrollItemResponse oldSnapshot = toPayrollItemResponse(item);
        item.setItemStatus(PayrollItemStatus.MANAGER_CONFIRMED);
        item.setManagerConfirmedAt(LocalDateTime.now());
        item.setManagerConfirmedBy(getCurrentUserOrNull());
        item.setManagerConfirmNote(trimToNull(request.note()));
        item = payrollItemRepository.save(item);

        PayPayrollPeriod period = item.getPayrollPeriod();
        period.setManagerConfirmedCount(
                (int) payrollItemRepository.countByPayrollPeriodPayrollPeriodIdAndItemStatusAndDeletedFalse(
                        period.getPayrollPeriodId(), PayrollItemStatus.MANAGER_CONFIRMED));
        period.setPeriodStatus(PayrollPeriodStatus.TEAM_REVIEW);
        payrollPeriodRepository.save(period);

        PayrollItemResponse response = toPayrollItemResponse(item);
        auditLogService.logSuccess("MANAGER_CONFIRM", "PAYROLL_ITEM", "pay_payroll_item",
                item.getPayrollItemId().toString(), oldSnapshot, response, "Quản lý xác nhận bảng lương team.");
        return response;
    }

    @Transactional
    public PayrollPeriodResponse approvePayrollPeriod(Long payrollPeriodId, ApprovePayrollPeriodRequest request) {
        PayPayrollPeriod period = getPayrollPeriod(payrollPeriodId);
        if (period.getPeriodStatus() == PayrollPeriodStatus.APPROVED
                || period.getPeriodStatus() == PayrollPeriodStatus.PUBLISHED) {
            throw new BusinessException("PAYROLL_PERIOD_APPROVE_STATUS_INVALID",
                    "Kỳ lương không còn ở trạng thái có thể duyệt.", HttpStatus.CONFLICT);
        }
        List<PayPayrollItem> items = payrollItemRepository
                .findAllByPayrollPeriodPayrollPeriodIdAndDeletedFalseOrderByEmployeeEmployeeCodeAscPayrollItemIdAsc(
                        period.getPayrollPeriodId());
        if (items.isEmpty()) {
            throw new BusinessException("PAYROLL_PERIOD_EMPTY", "Kỳ lương chưa có dòng lương để duyệt.",
                    HttpStatus.CONFLICT);
        }
        long missingManagerConfirm = items.stream()
                .filter(PayPayrollItem::isManagerConfirmationRequired)
                .filter(item -> item.getItemStatus() != PayrollItemStatus.MANAGER_CONFIRMED)
                .count();
        if (missingManagerConfirm > 0) {
            throw new BusinessException("PAYROLL_MANAGER_CONFIRM_PENDING",
                    "Vẫn còn dòng lương của team chưa được quản lý xác nhận.", HttpStatus.CONFLICT);
        }

        SecUserAccount currentUser = getCurrentUserOrNull();
        LocalDateTime now = LocalDateTime.now();
        for (PayPayrollItem item : items) {
            item.setItemStatus(PayrollItemStatus.HR_APPROVED);
            item.setHrApprovedAt(now);
            item.setHrApprovedBy(currentUser);
        }
        payrollItemRepository.saveAll(items);

        period.setApprovedAt(now);
        period.setApprovedBy(currentUser);
        period.setPeriodStatus(PayrollPeriodStatus.APPROVED);
        period.setNote(trimToNull(request.note()));
        period = payrollPeriodRepository.save(period);

        PayrollPeriodResponse response = toPayrollPeriodResponse(period);
        auditLogService.logSuccess("APPROVE", "PAYROLL_PERIOD", "pay_payroll_period",
                period.getPayrollPeriodId().toString(), null, response, "HR phê duyệt bảng lương.");
        return response;
    }

    @Transactional
    public PayrollPeriodResponse publishPayrollPeriod(Long payrollPeriodId, PublishPayrollPeriodRequest request) {
        PayPayrollPeriod period = getPayrollPeriod(payrollPeriodId);
        if (period.getPeriodStatus() != PayrollPeriodStatus.APPROVED) {
            throw new BusinessException("PAYROLL_PERIOD_PUBLISH_STATUS_INVALID",
                    "Chỉ được phát hành phiếu lương khi kỳ lương đã được duyệt.", HttpStatus.CONFLICT);
        }
        List<PayPayrollItem> items = payrollItemRepository
                .findAllByPayrollPeriodPayrollPeriodIdAndDeletedFalseOrderByEmployeeEmployeeCodeAscPayrollItemIdAsc(
                        period.getPayrollPeriodId());
        LocalDateTime now = LocalDateTime.now();
        SecUserAccount currentUser = getCurrentUserOrNull();
        for (PayPayrollItem item : items) {
            item.setItemStatus(PayrollItemStatus.PUBLISHED);
            item.setPublishedAt(now);
            item.setPublishedBy(currentUser);
        }
        payrollItemRepository.saveAll(items);

        period.setPublishedAt(now);
        period.setPublishedBy(currentUser);
        period.setPeriodStatus(PayrollPeriodStatus.PUBLISHED);
        period.setNote(trimToNull(request.note()));
        period = payrollPeriodRepository.save(period);

        PayrollPeriodResponse response = toPayrollPeriodResponse(period);
        auditLogService.logSuccess("PUBLISH", "PAYROLL_PERIOD", "pay_payroll_period",
                period.getPayrollPeriodId().toString(), null, response, "Phát hành phiếu lương.");
        return response;
    }

    @Transactional(readOnly = true)
    public List<SelfPayslipListItemResponse> listMyPayslips() {
        HrEmployee currentEmployee = accessScopeService.getCurrentEmployeeRequired();
        return payrollItemRepository.findAll((root, query, builder) -> builder.and(
                builder.isFalse(root.get("deleted")),
                builder.equal(root.join("employee").get("employeeId"), currentEmployee.getEmployeeId()),
                builder.equal(root.get("itemStatus"), PayrollItemStatus.PUBLISHED)),
                Sort.by("payrollPeriod.periodYear").descending().and(Sort.by("payrollPeriod.periodMonth").descending()))
                .stream()
                .map(item -> new SelfPayslipListItemResponse(
                        item.getPayrollPeriod().getPayrollPeriodId(),
                        item.getPayrollPeriod().getPeriodCode(),
                        item.getPayrollPeriod().getPeriodYear(),
                        item.getPayrollPeriod().getPeriodMonth(),
                        item.getItemStatus().name(),
                        scale(item.getGrossIncome()),
                        scale(item.getPitAmount()),
                        scale(item.getNetPay()),
                        item.getPublishedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PayrollItemResponse getMyPayslip(Long payrollPeriodId) {
        HrEmployee currentEmployee = accessScopeService.getCurrentEmployeeRequired();
        PayPayrollItem item = payrollItemRepository
                .findByPayrollPeriodPayrollPeriodIdAndEmployeeEmployeeIdAndDeletedFalse(payrollPeriodId,
                        currentEmployee.getEmployeeId())
                .orElseThrow(() -> new NotFoundException("PAYROLL_PAYSLIP_NOT_FOUND",
                        "Không tìm thấy phiếu lương cá nhân."));
        if (item.getItemStatus() != PayrollItemStatus.PUBLISHED) {
            throw new ForbiddenException("PAYROLL_PAYSLIP_NOT_PUBLISHED", "Phiếu lương này chưa được phát hành.");
        }
        return toPayrollItemResponse(item);
    }

    @Transactional(readOnly = true)
    public String exportBankTransferCsv(Long payrollPeriodId) {
        PayPayrollPeriod period = getPayrollPeriod(payrollPeriodId);
        if (period.getPeriodStatus() != PayrollPeriodStatus.APPROVED
                && period.getPeriodStatus() != PayrollPeriodStatus.PUBLISHED) {
            throw new BusinessException("PAYROLL_BANK_EXPORT_STATUS_INVALID",
                    "Chỉ được xuất file chi lương khi kỳ lương đã được duyệt.", HttpStatus.CONFLICT);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("employeeCode,employeeName,bankAccountName,bankAccountNo,bankName,netPay,periodCode\n");
        List<PayPayrollItem> items = payrollItemRepository
                .findAllByPayrollPeriodPayrollPeriodIdAndDeletedFalseOrderByEmployeeEmployeeCodeAscPayrollItemIdAsc(
                        payrollPeriodId);
        for (PayPayrollItem item : items) {
            PayEmployeeCompensation compensation = resolveCompensation(item.getEmployee().getEmployeeId(),
                    period.getPeriodEndDate(),
                    findActiveContract(item.getEmployee().getEmployeeId(), period.getPeriodEndDate()).orElse(null));
            builder.append(csv(item.getEmployee().getEmployeeCode())).append(',')
                    .append(csv(item.getEmployee().getFullName())).append(',')
                    .append(csv(compensation == null ? null : compensation.getBankAccountName())).append(',')
                    .append(csv(compensation == null ? null : compensation.getBankAccountNo())).append(',')
                    .append(csv(compensation == null ? null : compensation.getBankName())).append(',')
                    .append(item.getNetPay()).append(',')
                    .append(period.getPeriodCode()).append('\n');
        }
        return builder.toString();
    }

    @Transactional(readOnly = true)
    public String exportPitReportCsv(Long payrollPeriodId) {
        PayPayrollPeriod period = getPayrollPeriod(payrollPeriodId);
        if (period.getPeriodStatus() != PayrollPeriodStatus.APPROVED
                && period.getPeriodStatus() != PayrollPeriodStatus.PUBLISHED) {
            throw new BusinessException("PAYROLL_TAX_EXPORT_STATUS_INVALID",
                    "Chỉ được xuất báo cáo thuế khi kỳ lương đã được duyệt.", HttpStatus.CONFLICT);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(
                "employeeCode,employeeName,taxCode,grossIncome,employeeInsurance,personalDeduction,dependentDeduction,taxableIncome,pitAmount,periodCode\n");
        List<PayPayrollItem> items = payrollItemRepository
                .findAllByPayrollPeriodPayrollPeriodIdAndDeletedFalseOrderByEmployeeEmployeeCodeAscPayrollItemIdAsc(
                        payrollPeriodId);
        for (PayPayrollItem item : items) {
            builder.append(csv(item.getEmployee().getEmployeeCode())).append(',')
                    .append(csv(item.getEmployee().getFullName())).append(',')
                    .append(csv(item.getEmployee().getTaxCode())).append(',')
                    .append(item.getGrossIncome()).append(',')
                    .append(item.getEmployeeInsuranceAmount()).append(',')
                    .append(item.getPersonalDeductionAmount()).append(',')
                    .append(item.getDependentDeductionAmount()).append(',')
                    .append(item.getTaxableIncome()).append(',')
                    .append(item.getPitAmount()).append(',')
                    .append(period.getPeriodCode()).append('\n');
        }
        return builder.toString();
    }

    private PayrollComputationResult computePayrollForEmployee(PayPayrollPeriod period, HrEmployee employee,
            PayFormulaVersion formulaVersion) {
        CtLaborContract contract = findActiveContract(employee.getEmployeeId(), period.getPeriodEndDate())
                .orElseThrow(() -> new BusinessException("PAYROLL_CONTRACT_REQUIRED",
                        "Nhân viên " + employee.getEmployeeCode() + " chưa có hợp đồng hiệu lực để tính lương.",
                        HttpStatus.CONFLICT));
        PayEmployeeCompensation compensation = resolveCompensation(employee.getEmployeeId(), period.getPeriodEndDate(),
                contract);

        List<AttDailyAttendance> dailyList = dailyAttendanceRepository.findAll((root, query, builder) -> builder.and(
                builder.isFalse(root.get("deleted")),
                builder.equal(root.join("employee").get("employeeId"), employee.getEmployeeId()),
                builder.between(root.get("attendanceDate"), period.getPeriodStartDate(), period.getPeriodEndDate())),
                Sort.by("attendanceDate").ascending());

        int scheduledDayCount = 0;
        int presentDayCount = 0;
        int paidLeaveDayCount = 0;
        int unpaidLeaveDayCount = 0;
        int absentDayCount = 0;
        int approvedOtMinutes = 0;
        for (AttDailyAttendance daily : dailyList) {
            if (daily.getShiftAssignment() != null) {
                scheduledDayCount++;
            }
            if (daily.getDailyStatus() == AttendanceDailyStatus.PRESENT
                    || daily.getDailyStatus() == AttendanceDailyStatus.INCOMPLETE) {
                presentDayCount++;
            }
            if (Boolean.TRUE.equals(daily.isOnLeave()) && daily.getAttendanceDate() != null) {
                if (isPaidLeave(employee.getEmployeeId(), daily.getAttendanceDate())) {
                    paidLeaveDayCount++;
                } else {
                    unpaidLeaveDayCount++;
                }
            } else if (daily.getDailyStatus() == AttendanceDailyStatus.ABSENT && daily.getShiftAssignment() != null) {
                absentDayCount++;
            }
            approvedOtMinutes += zeroIfNull(daily.getApprovedOtMinutes());
        }

        int payableDays = Math.max(scheduledDayCount - absentDayCount - unpaidLeaveDayCount, 0);
        BigDecimal baseSalaryMonthly = scale(compensation.getBaseSalary());
        BigDecimal baseSalaryProrated = scheduledDayCount > 0
                ? scale(baseSalaryMonthly.multiply(BigDecimal.valueOf(payableDays))
                        .divide(BigDecimal.valueOf(scheduledDayCount), SCALE_MONEY, RoundingMode.HALF_UP))
                : baseSalaryMonthly;

        PayPayrollItem item = new PayPayrollItem();
        item.setPayrollPeriod(period);
        item.setEmployee(employee);
        item.setScheduledDayCount(scheduledDayCount);
        item.setPresentDayCount(presentDayCount);
        item.setPaidLeaveDayCount(paidLeaveDayCount);
        item.setUnpaidLeaveDayCount(unpaidLeaveDayCount);
        item.setAbsentDayCount(absentDayCount);
        item.setApprovedOtMinutes(approvedOtMinutes);
        item.setBaseSalaryMonthly(baseSalaryMonthly);
        item.setBaseSalaryProrated(baseSalaryProrated);
        item.setManagerConfirmationRequired(employee.getManagerEmployee() != null);
        item.setItemStatus(PayrollItemStatus.DRAFT);
        item = payrollItemRepository.save(item);

        List<PayPayrollItemLine> lines = new ArrayList<>();
        lines.add(newLine(item, LINE_BASE, "Lương cơ bản quy đổi", PayrollComponentCategory.EARNING,
                PayrollLineSourceType.SYSTEM, baseSalaryProrated, true, 10, null));

        BigDecimal hourlyRate = BigDecimal.ZERO;
        if (scheduledDayCount > 0 && formulaVersion.getStandardWorkHoursPerDay() != null
                && formulaVersion.getStandardWorkHoursPerDay().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal denominator = BigDecimal.valueOf(scheduledDayCount)
                    .multiply(formulaVersion.getStandardWorkHoursPerDay());
            hourlyRate = baseSalaryMonthly.divide(denominator, 8, RoundingMode.HALF_UP);
        }
        BigDecimal approvedHours = BigDecimal.valueOf(approvedOtMinutes).divide(BigDecimal.valueOf(60), 8,
                RoundingMode.HALF_UP);
        BigDecimal otAmount = scale(
                hourlyRate.multiply(approvedHours).multiply(zeroIfNull(formulaVersion.getOvertimeMultiplierWeekday())));
        if (otAmount.compareTo(BigDecimal.ZERO) > 0) {
            lines.add(newLine(item, LINE_OT, "Tiền OT được duyệt", PayrollComponentCategory.EARNING,
                    PayrollLineSourceType.SYSTEM, otAmount, true, 20, null));
        }

        List<PayEmployeeCompensationItem> configuredItems = compensation.getEmployeeCompensationId() == null
                ? List.of()
                : compensationItemRepository
                        .findAllByEmployeeCompensationEmployeeCompensationIdAndDeletedFalseOrderBySalaryComponentDisplayOrderAscEmployeeCompensationItemIdAsc(
                                compensation.getEmployeeCompensationId());
        for (PayEmployeeCompensationItem configItem : configuredItems) {
            PaySalaryComponent component = configItem.getSalaryComponent();
            BigDecimal amount = computeConfiguredAmount(baseSalaryMonthly, configItem);
            if (amount.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            lines.add(newLine(item,
                    component.getComponentCode(),
                    component.getComponentName(),
                    component.getComponentCategory(),
                    PayrollLineSourceType.CONFIGURED,
                    amount,
                    component.isTaxable(),
                    component.getDisplayOrder(),
                    configItem.getNote()));
        }

        BigDecimal insuranceBase = compensation.getInsuranceSalaryBase() != null ? compensation.getInsuranceSalaryBase()
                : baseSalaryMonthly;
        if (formulaVersion.getInsuranceSalaryCapAmount() != null
                && formulaVersion.getInsuranceSalaryCapAmount().compareTo(BigDecimal.ZERO) > 0) {
            insuranceBase = insuranceBase.min(formulaVersion.getInsuranceSalaryCapAmount());
        }
        BigDecimal unemploymentBase = compensation.getInsuranceSalaryBase() != null
                ? compensation.getInsuranceSalaryBase()
                : baseSalaryMonthly;
        if (formulaVersion.getUnemploymentSalaryCapAmount() != null
                && formulaVersion.getUnemploymentSalaryCapAmount().compareTo(BigDecimal.ZERO) > 0) {
            unemploymentBase = unemploymentBase.min(formulaVersion.getUnemploymentSalaryCapAmount());
        }

        BigDecimal socialAmount = scale(
                insuranceBase.multiply(zeroIfNull(formulaVersion.getSocialInsuranceEmployeeRate())).divide(ONE_HUNDRED,
                        8, RoundingMode.HALF_UP));
        BigDecimal healthAmount = scale(
                insuranceBase.multiply(zeroIfNull(formulaVersion.getHealthInsuranceEmployeeRate())).divide(ONE_HUNDRED,
                        8, RoundingMode.HALF_UP));
        BigDecimal unempAmount = scale(
                unemploymentBase.multiply(zeroIfNull(formulaVersion.getUnemploymentInsuranceEmployeeRate()))
                        .divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP));
        if (socialAmount.compareTo(BigDecimal.ZERO) > 0) {
            lines.add(newLine(item, LINE_SOCIAL, "BHXH người lao động", PayrollComponentCategory.DEDUCTION,
                    PayrollLineSourceType.SYSTEM, socialAmount, false, 900, null));
        }
        if (healthAmount.compareTo(BigDecimal.ZERO) > 0) {
            lines.add(newLine(item, LINE_HEALTH, "BHYT người lao động", PayrollComponentCategory.DEDUCTION,
                    PayrollLineSourceType.SYSTEM, healthAmount, false, 901, null));
        }
        if (unempAmount.compareTo(BigDecimal.ZERO) > 0) {
            lines.add(newLine(item, LINE_UNEMP, "BHTN người lao động", PayrollComponentCategory.DEDUCTION,
                    PayrollLineSourceType.SYSTEM, unempAmount, false, 902, null));
        }
        payrollItemLineRepository.saveAll(lines);

        PersonalTaxContext taxContext = resolvePersonalTaxContext(employee.getEmployeeId(), period.getPeriodEndDate(),
                formulaVersion);
        item.setPersonalDeductionAmount(taxContext.personalDeductionAmount());
        item.setDependentDeductionAmount(taxContext.dependentDeductionAmount());
        recalculateItemTotalsFromLines(item);
        item = payrollItemRepository.save(item);

        return new PayrollComputationResult(item,
                payrollItemLineRepository
                        .findAllByPayrollItemPayrollItemIdAndDeletedFalseOrderByDisplayOrderAscPayrollItemLineIdAsc(
                                item.getPayrollItemId()));
    }

    private void recalculateItemTotalsFromLines(PayPayrollItem item) {
        List<PayPayrollItemLine> lines = payrollItemLineRepository
                .findAllByPayrollItemPayrollItemIdAndDeletedFalseOrderByDisplayOrderAscPayrollItemLineIdAsc(
                        item.getPayrollItemId());
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal fixedEarnings = BigDecimal.ZERO;
        BigDecimal fixedDeductions = BigDecimal.ZERO;
        BigDecimal insuranceAmount = BigDecimal.ZERO;
        BigDecimal taxableAddition = BigDecimal.ZERO;

        for (PayPayrollItemLine line : lines) {
            if (line.getComponentCategory() == PayrollComponentCategory.EARNING) {
                gross = gross.add(zeroIfNull(line.getLineAmount()));
                taxableAddition = taxableAddition
                        .add(line.isTaxable() ? zeroIfNull(line.getLineAmount()) : BigDecimal.ZERO);
                if (!LINE_BASE.equals(line.getComponentCode()) && !LINE_OT.equals(line.getComponentCode())) {
                    fixedEarnings = fixedEarnings.add(zeroIfNull(line.getLineAmount()));
                }
            } else if (line.getComponentCategory() == PayrollComponentCategory.DEDUCTION) {
                if (Set.of(LINE_SOCIAL, LINE_HEALTH, LINE_UNEMP).contains(line.getComponentCode())) {
                    insuranceAmount = insuranceAmount.add(zeroIfNull(line.getLineAmount()));
                } else if (!LINE_PIT.equals(line.getComponentCode())) {
                    fixedDeductions = fixedDeductions.add(zeroIfNull(line.getLineAmount()));
                }
            }
        }

        item.setFixedEarningTotal(scale(fixedEarnings));
        item.setFixedDeductionTotal(scale(fixedDeductions));
        item.setEmployeeInsuranceAmount(scale(insuranceAmount));
        item.setGrossIncome(scale(gross));

        PayFormulaVersion formulaVersion = item.getPayrollPeriod().getFormulaVersion();
        BigDecimal taxableIncome = taxableAddition
                .subtract(zeroIfNull(item.getEmployeeInsuranceAmount()))
                .subtract(zeroIfNull(item.getPersonalDeductionAmount()))
                .subtract(zeroIfNull(item.getDependentDeductionAmount()));
        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            taxableIncome = BigDecimal.ZERO;
        }
        taxableIncome = scale(taxableIncome);
        item.setTaxableIncome(taxableIncome);

        BigDecimal pitAmount = computePitAmount(taxableIncome, formulaVersion);
        item.setPitAmount(pitAmount);

        PayPayrollItemLine currentPitLine = lines.stream()
                .filter(line -> LINE_PIT.equals(line.getComponentCode()) && !line.isDeleted()).findFirst().orElse(null);
        if (currentPitLine == null && pitAmount.compareTo(BigDecimal.ZERO) > 0) {
            currentPitLine = newLine(item, LINE_PIT, "Thuế TNCN tạm khấu trừ", PayrollComponentCategory.DEDUCTION,
                    PayrollLineSourceType.SYSTEM, pitAmount, false, 990, null);
            payrollItemLineRepository.save(currentPitLine);
        } else if (currentPitLine != null) {
            currentPitLine.setLineAmount(pitAmount);
            payrollItemLineRepository.save(currentPitLine);
        }

        BigDecimal netPay = zeroIfNull(item.getGrossIncome())
                .subtract(zeroIfNull(item.getFixedDeductionTotal()))
                .subtract(zeroIfNull(item.getEmployeeInsuranceAmount()))
                .subtract(zeroIfNull(item.getPitAmount()));
        item.setNetPay(scale(netPay));
    }

    private PersonalTaxContext resolvePersonalTaxContext(Long employeeId, LocalDate periodEndDate,
            PayFormulaVersion formulaVersion) {
        PayPersonalTaxProfile profile = personalTaxProfileRepository.findByEmployeeEmployeeIdAndDeletedFalse(employeeId)
                .orElse(null);
        boolean residentTaxpayer = profile == null || profile.isResidentTaxpayer();
        boolean familyDeductionEnabled = profile == null || profile.isFamilyDeductionEnabled();
        BigDecimal personalDeduction = residentTaxpayer && familyDeductionEnabled
                ? scale(formulaVersion.getPersonalDeductionMonthly())
                : BigDecimal.ZERO;

        long activeDependentCount = 0;
        if (residentTaxpayer && familyDeductionEnabled) {
            List<PayTaxDependent> dependents = taxDependentRepository
                    .findAllByEmployeeEmployeeIdAndStatusAndDeletedFalseOrderByDeductionStartMonthAscTaxDependentIdAsc(
                            employeeId, RecordStatus.ACTIVE);
            LocalDate monthStart = periodEndDate.withDayOfMonth(1);
            activeDependentCount = dependents.stream()
                    .filter(dep -> !dep.getDeductionStartMonth().isAfter(monthStart))
                    .filter(dep -> dep.getDeductionEndMonth() == null
                            || !dep.getDeductionEndMonth().isBefore(monthStart))
                    .count();
        }
        BigDecimal dependentDeduction = residentTaxpayer && familyDeductionEnabled
                ? scale(formulaVersion.getDependentDeductionMonthly()
                        .multiply(BigDecimal.valueOf(activeDependentCount)))
                : BigDecimal.ZERO;
        return new PersonalTaxContext(personalDeduction, dependentDeduction);
    }

    private BigDecimal computePitAmount(BigDecimal taxableIncome, PayFormulaVersion formulaVersion) {
        if (taxableIncome == null || taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(SCALE_MONEY, RoundingMode.HALF_UP);
        }
        List<PayFormulaTaxBracket> brackets = formulaTaxBracketRepository
                .findAllByFormulaVersionFormulaVersionIdAndDeletedFalseOrderBySequenceNoAsc(
                        formulaVersion.getFormulaVersionId());
        for (PayFormulaTaxBracket bracket : brackets) {
            boolean lowerBoundMatched = taxableIncome.compareTo(zeroIfNull(bracket.getIncomeFrom())) >= 0;
            boolean upperBoundMatched = bracket.getIncomeTo() == null
                    || taxableIncome.compareTo(bracket.getIncomeTo()) <= 0;
            if (lowerBoundMatched && upperBoundMatched) {
                BigDecimal tax = taxableIncome.multiply(zeroIfNull(bracket.getTaxRate()))
                        .divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP)
                        .subtract(zeroIfNull(bracket.getQuickDeduction()));
                if (tax.compareTo(BigDecimal.ZERO) < 0) {
                    tax = BigDecimal.ZERO;
                }
                return scale(tax);
            }
        }
        PayFormulaTaxBracket lastBracket = brackets.isEmpty() ? null : brackets.get(brackets.size() - 1);
        if (lastBracket == null) {
            return BigDecimal.ZERO.setScale(SCALE_MONEY, RoundingMode.HALF_UP);
        }
        BigDecimal tax = taxableIncome.multiply(zeroIfNull(lastBracket.getTaxRate()))
                .divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP)
                .subtract(zeroIfNull(lastBracket.getQuickDeduction()));
        if (tax.compareTo(BigDecimal.ZERO) < 0) {
            tax = BigDecimal.ZERO;
        }
        return scale(tax);
    }

    private boolean isPaidLeave(Long employeeId, LocalDate date) {
        return leaveRequestRepository.existsFinalizedLeaveOnDateAndPaid(employeeId, date, LeaveRequestStatus.FINALIZED,
                true);
    }

    private List<HrEmployee> findEligibleEmployees(LocalDate periodEndDate) {
        return employeeRepository.findAll((root, query, builder) -> builder.and(
                builder.isFalse(root.get("deleted")),
                root.get("employmentStatus").in(EmploymentStatus.ACTIVE, EmploymentStatus.PROBATION),
                builder.lessThanOrEqualTo(root.get("hireDate"), periodEndDate)), Sort.by("employeeCode").ascending());
    }

    private void validateEmployeesHaveContracts(List<HrEmployee> employees, LocalDate date) {
        List<String> missing = employees.stream()
                .filter(employee -> findActiveContract(employee.getEmployeeId(), date).isEmpty())
                .map(HrEmployee::getEmployeeCode)
                .limit(20)
                .toList();
        if (!missing.isEmpty()) {
            throw new BusinessException("PAYROLL_ACTIVE_CONTRACT_REQUIRED",
                    "Có nhân viên chưa có hợp đồng hiệu lực để tính lương.", HttpStatus.CONFLICT, missing);
        }
    }

    private Optional<CtLaborContract> findActiveContract(Long employeeId, LocalDate date) {
        return laborContractRepository.findAll((root, query, builder) -> builder.and(
                builder.isFalse(root.get("deleted")),
                builder.equal(root.join("employee").get("employeeId"), employeeId),
                builder.equal(root.get("contractStatus"), ContractStatus.ACTIVE),
                builder.lessThanOrEqualTo(root.get("effectiveDate"), date),
                builder.or(builder.isNull(root.get("endDate")),
                        builder.greaterThanOrEqualTo(root.get("endDate"), date))),
                Sort.by("effectiveDate").descending().and(Sort.by("laborContractId").descending()))
                .stream().findFirst();
    }

    private PayEmployeeCompensation resolveCompensation(Long employeeId, LocalDate date, CtLaborContract contract) {
        PayEmployeeCompensation compensation = compensationRepository.findEffectiveByEmployeeAndDate(employeeId, date)
                .stream().findFirst().orElse(null);
        if (compensation != null) {
            return compensation;
        }
        if (contract == null) {
            return null;
        }
        PayEmployeeCompensation transientCompensation = new PayEmployeeCompensation();
        transientCompensation.setEmployee(contract.getEmployee());
        transientCompensation.setEffectiveFrom(contract.getEffectiveDate());
        transientCompensation.setEffectiveTo(contract.getEndDate());
        transientCompensation.setBaseSalary(scale(contract.getBaseSalary()));
        transientCompensation.setInsuranceSalaryBase(scale(contract.getBaseSalary()));
        transientCompensation.setSalaryCurrency(contract.getSalaryCurrency());
        transientCompensation.setStatus(RecordStatus.ACTIVE);
        return transientCompensation;
    }

    private void refreshPeriodAggregates(PayPayrollPeriod period) {
        List<PayPayrollItem> items = payrollItemRepository
                .findAllByPayrollPeriodPayrollPeriodIdAndDeletedFalseOrderByEmployeeEmployeeCodeAscPayrollItemIdAsc(
                        period.getPayrollPeriodId());
        period.setTotalEmployeeCount(items.size());
        period.setManagerConfirmedCount((int) items.stream()
                .filter(item -> item.getItemStatus() == PayrollItemStatus.MANAGER_CONFIRMED).count());
        period.setTotalGrossAmount(scale(items.stream().map(PayPayrollItem::getGrossIncome).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        period.setTotalNetAmount(scale(items.stream().map(PayPayrollItem::getNetPay).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        payrollPeriodRepository.save(period);
    }

    private void markPayrollItemsDeleted(List<PayPayrollItem> items) {
        for (PayPayrollItem item : items) {
            List<PayPayrollItemLine> lines = payrollItemLineRepository
                    .findAllByPayrollItemPayrollItemIdAndDeletedFalseOrderByDisplayOrderAscPayrollItemLineIdAsc(
                            item.getPayrollItemId());
            for (PayPayrollItemLine line : lines) {
                line.setDeleted(true);
            }
            payrollItemLineRepository.saveAll(lines);
            item.setDeleted(true);
        }
        payrollItemRepository.saveAll(items);
    }

    private PayPayrollItemLine newLine(
            PayPayrollItem item,
            String componentCode,
            String componentName,
            PayrollComponentCategory componentCategory,
            PayrollLineSourceType sourceType,
            BigDecimal amount,
            boolean taxable,
            Integer displayOrder,
            String note) {
        PayPayrollItemLine line = new PayPayrollItemLine();
        line.setPayrollItem(item);
        line.setComponentCode(componentCode);
        line.setComponentName(componentName);
        line.setComponentCategory(componentCategory);
        line.setLineSourceType(sourceType);
        line.setLineAmount(scale(amount));
        line.setTaxable(taxable);
        line.setDisplayOrder(displayOrder);
        line.setLineNote(trimToNull(note));
        return line;
    }

    private BigDecimal computeConfiguredAmount(BigDecimal baseSalary, PayEmployeeCompensationItem item) {
        PayrollAmountType amountType = item.getSalaryComponent().getAmountType();
        if (amountType == PayrollAmountType.FIXED_AMOUNT || amountType == PayrollAmountType.SYSTEM) {
            return scale(zeroIfNull(item.getAmountValue()));
        }
        if (amountType == PayrollAmountType.PERCENT_BASE) {
            return scale(baseSalary.multiply(zeroIfNull(item.getPercentageValue())).divide(ONE_HUNDRED, 8,
                    RoundingMode.HALF_UP));
        }
        return BigDecimal.ZERO.setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }

    private void validateFormulaRequest(FormulaVersionUpsertRequest request) {
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new BusinessException("PAYROLL_FORMULA_DATE_INVALID", "effectiveTo không được nhỏ hơn effectiveFrom.",
                    HttpStatus.BAD_REQUEST);
        }
        List<FormulaTaxBracketRequest> brackets = new ArrayList<>(request.taxBrackets());
        brackets.sort(Comparator.comparing(FormulaTaxBracketRequest::sequenceNo));
        BigDecimal previousTo = null;
        for (FormulaTaxBracketRequest bracket : brackets) {
            if (bracket.incomeTo() != null && bracket.incomeTo().compareTo(bracket.incomeFrom()) < 0) {
                throw new BusinessException("PAYROLL_FORMULA_BRACKET_INVALID",
                        "incomeTo không được nhỏ hơn incomeFrom.", HttpStatus.BAD_REQUEST);
            }
            if (previousTo != null && bracket.incomeFrom().compareTo(previousTo) <= 0) {
                throw new BusinessException("PAYROLL_FORMULA_BRACKET_INVALID",
                        "Các bậc thuế đang bị chồng lấn hoặc chạm biên không hợp lệ.", HttpStatus.BAD_REQUEST);
            }
            previousTo = bracket.incomeTo();
        }
    }

    private void validateCompensationItems(List<EmployeeCompensationItemRequest> items) {
        Set<Long> seen = new HashSet<>();
        for (EmployeeCompensationItemRequest item : items) {
            if (!seen.add(item.salaryComponentId())) {
                throw new BusinessException("PAYROLL_COMPENSATION_ITEM_DUPLICATE",
                        "Không được lặp lại salaryComponentId trong cùng một cấu hình lương.", HttpStatus.BAD_REQUEST);
            }
            PaySalaryComponent component = getSalaryComponent(item.salaryComponentId());
            if (component.getAmountType() == PayrollAmountType.FIXED_AMOUNT && item.amountValue() == null) {
                throw new BusinessException("PAYROLL_COMPENSATION_ITEM_AMOUNT_REQUIRED",
                        "Component FIXED_AMOUNT bắt buộc có amountValue.", HttpStatus.BAD_REQUEST);
            }
            if (component.getAmountType() == PayrollAmountType.PERCENT_BASE && item.percentageValue() == null) {
                throw new BusinessException("PAYROLL_COMPENSATION_ITEM_PERCENT_REQUIRED",
                        "Component PERCENT_BASE bắt buộc có percentageValue.", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void validateDependentDates(LocalDate startMonth, LocalDate endMonth) {
        if (endMonth != null && endMonth.isBefore(startMonth)) {
            throw new BusinessException("PAYROLL_DEPENDENT_DATE_INVALID",
                    "deductionEndMonth không được nhỏ hơn deductionStartMonth.", HttpStatus.BAD_REQUEST);
        }
    }

    private PayrollComponentCategory parseCategory(String value) {
        try {
            return PayrollComponentCategory.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception exception) {
            throw new BusinessException("PAYROLL_LINE_CATEGORY_INVALID", "componentCategory không hợp lệ.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private PaySalaryComponent getSalaryComponent(Long salaryComponentId) {
        return salaryComponentRepository.findBySalaryComponentIdAndDeletedFalse(salaryComponentId)
                .orElseThrow(
                        () -> new NotFoundException("PAYROLL_COMPONENT_NOT_FOUND", "Không tìm thấy thành phần lương."));
    }

    private PayFormulaVersion getFormulaVersionEntity(Long formulaVersionId) {
        return formulaVersionRepository.findByFormulaVersionIdAndDeletedFalse(formulaVersionId)
                .orElseThrow(() -> new NotFoundException("PAYROLL_FORMULA_NOT_FOUND",
                        "Không tìm thấy cấu hình công thức lương."));
    }

    private PayFormulaVersion resolveFormulaVersion(LocalDate date) {
        return formulaVersionRepository.findEffectiveVersions(date).stream().findFirst()
                .orElseThrow(() -> new NotFoundException("PAYROLL_EFFECTIVE_FORMULA_NOT_FOUND",
                        "Không tìm thấy cấu hình công thức lương hiệu lực cho kỳ này."));
    }

    private PayPayrollPeriod getPayrollPeriod(Long payrollPeriodId) {
        return payrollPeriodRepository.findByPayrollPeriodIdAndDeletedFalse(payrollPeriodId)
                .orElseThrow(() -> new NotFoundException("PAYROLL_PERIOD_NOT_FOUND", "Không tìm thấy kỳ lương."));
    }

    private PayPayrollItem getPayrollItemEntity(Long payrollItemId) {
        return payrollItemRepository.findByPayrollItemIdAndDeletedFalse(payrollItemId)
                .orElseThrow(() -> new NotFoundException("PAYROLL_ITEM_NOT_FOUND", "Không tìm thấy dòng lương."));
    }

    private HrEmployee getEmployee(Long employeeId) {
        return employeeRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new NotFoundException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên."));
    }

    private HrOrgUnit getOrgUnit(Long orgUnitId) {
        return orgUnitRepository.findByOrgUnitIdAndDeletedFalse(orgUnitId)
                .orElseThrow(() -> new NotFoundException("ORG_UNIT_NOT_FOUND", "Không tìm thấy phòng ban/đơn vị."));
    }

    private PayTaxDependent getTaxDependent(Long taxDependentId) {
        return taxDependentRepository.findByTaxDependentIdAndDeletedFalse(taxDependentId)
                .orElseThrow(
                        () -> new NotFoundException("PAYROLL_DEPENDENT_NOT_FOUND", "Không tìm thấy người phụ thuộc."));
    }

    private SalaryComponentResponse toSalaryComponentResponse(PaySalaryComponent entity) {
        return new SalaryComponentResponse(
                entity.getSalaryComponentId(),
                entity.getComponentCode(),
                entity.getComponentName(),
                entity.getComponentCategory().name(),
                entity.getAmountType().name(),
                entity.isTaxable(),
                entity.isInsuranceBaseIncluded(),
                entity.isPayslipVisible(),
                entity.getDisplayOrder(),
                entity.getStatus().name(),
                entity.getDescription());
    }

    private FormulaVersionResponse toFormulaVersionResponse(PayFormulaVersion entity) {
        List<FormulaTaxBracketResponse> taxBrackets = formulaTaxBracketRepository
                .findAllByFormulaVersionFormulaVersionIdAndDeletedFalseOrderBySequenceNoAsc(
                        entity.getFormulaVersionId())
                .stream()
                .map(bracket -> new FormulaTaxBracketResponse(
                        bracket.getFormulaTaxBracketId(),
                        bracket.getSequenceNo(),
                        scale(bracket.getIncomeFrom()),
                        scaleNullable(bracket.getIncomeTo()),
                        bracket.getTaxRate(),
                        scale(bracket.getQuickDeduction())))
                .toList();
        return new FormulaVersionResponse(
                entity.getFormulaVersionId(),
                entity.getFormulaCode(),
                entity.getFormulaName(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                scale(entity.getPersonalDeductionMonthly()),
                scale(entity.getDependentDeductionMonthly()),
                entity.getSocialInsuranceEmployeeRate(),
                entity.getHealthInsuranceEmployeeRate(),
                entity.getUnemploymentInsuranceEmployeeRate(),
                scaleNullable(entity.getInsuranceSalaryCapAmount()),
                scaleNullable(entity.getUnemploymentSalaryCapAmount()),
                entity.getStandardWorkHoursPerDay(),
                entity.getOvertimeMultiplierWeekday(),
                entity.getStatus().name(),
                entity.getNote(),
                taxBrackets);
    }

    private EmployeeCompensationListItemResponse toCompensationListItem(PayEmployeeCompensation entity) {
        return new EmployeeCompensationListItemResponse(
                entity.getEmployeeCompensationId(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getEmployee().getOrgUnit() == null ? null : entity.getEmployee().getOrgUnit().getOrgUnitName(),
                scale(entity.getBaseSalary()),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getStatus().name());
    }

    private EmployeeCompensationResponse toEmployeeCompensationResponse(PayEmployeeCompensation entity) {
        List<EmployeeCompensationItemResponse> items = entity.getEmployeeCompensationId() == null ? List.of()
                : compensationItemRepository
                        .findAllByEmployeeCompensationEmployeeCompensationIdAndDeletedFalseOrderBySalaryComponentDisplayOrderAscEmployeeCompensationItemIdAsc(
                                entity.getEmployeeCompensationId())
                        .stream()
                        .map(item -> new EmployeeCompensationItemResponse(
                                item.getEmployeeCompensationItemId(),
                                item.getSalaryComponent().getSalaryComponentId(),
                                item.getSalaryComponent().getComponentCode(),
                                item.getSalaryComponent().getComponentName(),
                                item.getSalaryComponent().getComponentCategory().name(),
                                item.getSalaryComponent().getAmountType().name(),
                                scaleNullable(item.getAmountValue()),
                                item.getPercentageValue(),
                                item.getNote()))
                        .toList();
        return new EmployeeCompensationResponse(
                entity.getEmployeeCompensationId(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                scale(entity.getBaseSalary()),
                scaleNullable(entity.getInsuranceSalaryBase()),
                entity.getSalaryCurrency(),
                entity.getBankAccountName(),
                entity.getBankAccountNo(),
                entity.getBankName(),
                entity.getPaymentNote(),
                entity.getStatus().name(),
                items);
    }

    private PersonalTaxProfileResponse toPersonalTaxProfileResponse(PayPersonalTaxProfile profile) {
        List<TaxDependentResponse> dependents = taxDependentRepository
                .findAllByEmployeeEmployeeIdAndDeletedFalseOrderByDeductionStartMonthAscTaxDependentIdAsc(
                        profile.getEmployee().getEmployeeId())
                .stream().map(this::toTaxDependentResponse).toList();
        return new PersonalTaxProfileResponse(
                profile.getPersonalTaxProfileId(),
                profile.getEmployee().getEmployeeId(),
                profile.getEmployee().getEmployeeCode(),
                profile.getEmployee().getFullName(),
                profile.isResidentTaxpayer(),
                profile.isFamilyDeductionEnabled(),
                profile.getTaxRegistrationDate(),
                profile.getTaxFinalizationMethod().name(),
                profile.getStatus().name(),
                profile.getNote(),
                dependents);
    }

    private TaxDependentResponse toTaxDependentResponse(PayTaxDependent entity) {
        return new TaxDependentResponse(
                entity.getTaxDependentId(),
                entity.getFullName(),
                entity.getRelationshipCode().name(),
                entity.getIdentificationNo(),
                entity.getDateOfBirth(),
                entity.getDeductionStartMonth(),
                entity.getDeductionEndMonth(),
                entity.getStatus().name(),
                entity.getNote());
    }

    private PayrollPeriodResponse toPayrollPeriodResponse(PayPayrollPeriod entity) {
        return new PayrollPeriodResponse(
                entity.getPayrollPeriodId(),
                entity.getPeriodCode(),
                entity.getPeriodYear(),
                entity.getPeriodMonth(),
                entity.getPeriodStartDate(),
                entity.getPeriodEndDate(),
                entity.getAttendancePeriod() == null ? null : entity.getAttendancePeriod().getAttendancePeriodId(),
                entity.getAttendancePeriod() == null ? null : entity.getAttendancePeriod().getPeriodCode(),
                entity.getFormulaVersion() == null ? null : entity.getFormulaVersion().getFormulaVersionId(),
                entity.getFormulaVersion() == null ? null : entity.getFormulaVersion().getFormulaCode(),
                entity.getFormulaVersion() == null ? null : entity.getFormulaVersion().getFormulaName(),
                entity.getPeriodStatus().name(),
                entity.getGeneratedAt(),
                entity.getGeneratedBy() == null ? null : entity.getGeneratedBy().getUsername(),
                entity.getApprovedAt(),
                entity.getApprovedBy() == null ? null : entity.getApprovedBy().getUsername(),
                entity.getPublishedAt(),
                entity.getPublishedBy() == null ? null : entity.getPublishedBy().getUsername(),
                entity.getTotalEmployeeCount(),
                entity.getManagerConfirmedCount(),
                scale(entity.getTotalGrossAmount()),
                scale(entity.getTotalNetAmount()),
                entity.getNote());
    }

    private PayrollItemResponse toPayrollItemResponse(PayPayrollItem entity) {
        List<PayrollItemLineResponse> lines = payrollItemLineRepository
                .findAllByPayrollItemPayrollItemIdAndDeletedFalseOrderByDisplayOrderAscPayrollItemLineIdAsc(
                        entity.getPayrollItemId())
                .stream()
                .map(line -> new PayrollItemLineResponse(
                        line.getPayrollItemLineId(),
                        line.getComponentCode(),
                        line.getComponentName(),
                        line.getComponentCategory().name(),
                        line.getLineSourceType().name(),
                        scale(line.getLineAmount()),
                        line.isTaxable(),
                        line.getDisplayOrder(),
                        line.getLineNote()))
                .toList();
        return new PayrollItemResponse(
                entity.getPayrollItemId(),
                entity.getPayrollPeriod().getPayrollPeriodId(),
                entity.getEmployee().getEmployeeId(),
                entity.getEmployee().getEmployeeCode(),
                entity.getEmployee().getFullName(),
                entity.getEmployee().getOrgUnit() == null ? null : entity.getEmployee().getOrgUnit().getOrgUnitName(),
                entity.getScheduledDayCount(),
                entity.getPresentDayCount(),
                entity.getPaidLeaveDayCount(),
                entity.getUnpaidLeaveDayCount(),
                entity.getAbsentDayCount(),
                entity.getApprovedOtMinutes(),
                scale(entity.getBaseSalaryMonthly()),
                scale(entity.getBaseSalaryProrated()),
                scale(entity.getFixedEarningTotal()),
                scale(entity.getFixedDeductionTotal()),
                scale(entity.getEmployeeInsuranceAmount()),
                scale(entity.getPersonalDeductionAmount()),
                scale(entity.getDependentDeductionAmount()),
                scale(entity.getTaxableIncome()),
                scale(entity.getPitAmount()),
                scale(entity.getGrossIncome()),
                scale(entity.getNetPay()),
                entity.getItemStatus().name(),
                entity.isManagerConfirmationRequired(),
                entity.getManagerConfirmedBy() == null ? null : entity.getManagerConfirmedBy().getUsername(),
                entity.getManagerConfirmedAt(),
                entity.getManagerConfirmNote(),
                entity.getHrApprovedBy() == null ? null : entity.getHrApprovedBy().getUsername(),
                entity.getHrApprovedAt(),
                entity.getPublishedAt(),
                entity.getAdjustmentNote(),
                lines);
    }

    private <T> PageResponse<T> toPageResponse(Page<?> page, List<T> items, int currentPage, int size) {
        return new PageResponse<>(
                items,
                currentPage,
                size,
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious());
    }

    private SecUserAccount getCurrentUserOrNull() {
        return SecurityUserContext.getCurrentUserId()
                .flatMap(userAccountRepository::findById)
                .orElse(null);
    }

    private BigDecimal scale(BigDecimal value) {
        return zeroIfNull(value).setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleNullable(BigDecimal value) {
        return value == null ? null : value.setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Integer zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private record PersonalTaxContext(BigDecimal personalDeductionAmount, BigDecimal dependentDeductionAmount) {
    }

    private record PayrollComputationResult(PayPayrollItem item, List<PayPayrollItemLine> lines) {
    }
}
