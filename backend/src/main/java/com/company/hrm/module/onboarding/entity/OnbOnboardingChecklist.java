package com.company.hrm.module.onboarding.entity;

import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "onb_onboarding_checklist")
public class OnbOnboardingChecklist extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "onboarding_checklist_id", nullable = false, updatable = false)
    private Long onboardingChecklistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onboarding_id", nullable = false)
    private OnbOnboarding onboarding;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    @Column(name = "is_completed", nullable = false)
    private boolean completed;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    private SecUserAccount completedBy;

    @Column(name = "note", length = 500)
    private String note;
}
