package com.company.hrm.module.contract.entity;

import com.company.hrm.common.constant.ContractStatus;
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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ct_contract_status_history")
public class CtContractStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_status_history_id", nullable = false, updatable = false)
    private Long contractStatusHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labor_contract_id", nullable = false)
    private CtLaborContract laborContract;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private ContractStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private ContractStatus toStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private SecUserAccount changedBy;

    @Column(name = "reason", length = 1000)
    private String reason;
}
