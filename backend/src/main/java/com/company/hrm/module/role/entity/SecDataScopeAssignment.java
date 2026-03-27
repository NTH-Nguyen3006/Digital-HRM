package com.company.hrm.module.role.entity;

import com.company.hrm.common.constant.DataScopeCode;
import com.company.hrm.common.constant.DataScopeSubjectType;
import com.company.hrm.common.constant.DataScopeTargetType;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.entity.CreatedAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sec_data_scope_assignment")
public class SecDataScopeAssignment extends CreatedAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "data_scope_assignment_id", nullable = false, updatable = false)
    private UUID dataScopeAssignmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 20)
    private DataScopeSubjectType subjectType;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_code", nullable = false, length = 30)
    private DataScopeCode scopeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 30)
    private DataScopeTargetType targetType;

    @Column(name = "target_ref_id", length = 50)
    private String targetRefId;

    @Column(name = "is_inclusive", nullable = false)
    private boolean inclusive;

    @Column(name = "priority_order", nullable = false)
    private int priorityOrder;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;
}
