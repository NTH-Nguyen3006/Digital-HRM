package com.company.hrm.module.offboarding.entity;

import com.company.hrm.common.constant.OffboardingStatus;
import com.company.hrm.common.entity.CreatedAuditEntity;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "off_offboarding_history")
public class OffOffboardingHistory extends CreatedAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offboarding_history_id", nullable = false, updatable = false)
    private Long offboardingHistoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offboarding_case_id", nullable = false)
    private OffOffboardingCase offboardingCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private OffboardingStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private OffboardingStatus toStatus;

    @Column(name = "action_code", nullable = false, length = 50)
    private String actionCode;

    @Column(name = "action_note", length = 1000)
    private String actionNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private SecUserAccount changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}
