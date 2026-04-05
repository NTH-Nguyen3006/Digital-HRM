package com.company.hrm.module.contract.entity;

import com.company.hrm.common.constant.ContractAppendixStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ct_contract_appendix")
public class CtContractAppendix extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_appendix_id", nullable = false, updatable = false)
    private Long contractAppendixId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labor_contract_id", nullable = false)
    private CtLaborContract laborContract;

    @Column(name = "appendix_number", nullable = false, length = 50)
    private String appendixNumber;

    @Column(name = "appendix_name", nullable = false, length = 255)
    private String appendixName;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Lob
    @Column(name = "changed_fields_json", columnDefinition = "NVARCHAR(MAX)")
    private String changedFieldsJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContractAppendixStatus status;

    @Column(name = "note", length = 1000)
    private String note;
}
