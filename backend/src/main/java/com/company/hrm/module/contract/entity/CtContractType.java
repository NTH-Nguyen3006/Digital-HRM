package com.company.hrm.module.contract.entity;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ct_contract_type")
public class CtContractType extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_type_id", nullable = false, updatable = false)
    private Long contractTypeId;

    @Column(name = "contract_type_code", nullable = false, length = 30)
    private String contractTypeCode;

    @Column(name = "contract_type_name", nullable = false, length = 200)
    private String contractTypeName;

    @Column(name = "max_term_months")
    private Integer maxTermMonths;

    @Column(name = "requires_end_date", nullable = false)
    private boolean requiresEndDate = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;

    @Column(name = "description", length = 500)
    private String description;
}
