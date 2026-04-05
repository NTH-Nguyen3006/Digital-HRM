package com.company.hrm.module.contract.entity;

import com.company.hrm.common.constant.ContractStatus;
import com.company.hrm.common.constant.ContractWorkingType;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.jobtitle.entity.HrJobTitle;
import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ct_labor_contract")
public class CtLaborContract extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "labor_contract_id", nullable = false, updatable = false)
    private Long laborContractId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_type_id", nullable = false)
    private CtContractType contractType;

    @Column(name = "contract_number", nullable = false, length = 50)
    private String contractNumber;

    @Column(name = "sign_date", nullable = false)
    private LocalDate signDate;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_title_id", nullable = false)
    private HrJobTitle jobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id", nullable = false)
    private HrOrgUnit orgUnit;

    @Column(name = "work_location", length = 200)
    private String workLocation;

    @Column(name = "base_salary", nullable = false, precision = 18, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "salary_currency", nullable = false, length = 10)
    private String salaryCurrency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(name = "working_type", nullable = false, length = 20)
    private ContractWorkingType workingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", nullable = false, length = 20)
    private ContractStatus contractStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signed_by_company_user_id")
    private SecUserAccount signedByCompanyUser;

    @Column(name = "note", length = 1000)
    private String note;
}
