package com.company.hrm.module.offboarding.entity;

import com.company.hrm.common.constant.OffboardingChecklistItemType;
import com.company.hrm.common.constant.OffboardingChecklistStatus;
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
@Table(name = "off_offboarding_checklist_item")
public class OffOffboardingChecklistItem extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offboarding_checklist_item_id", nullable = false, updatable = false)
    private Long offboardingChecklistItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offboarding_case_id", nullable = false)
    private OffOffboardingCase offboardingCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 30)
    private OffboardingChecklistItemType itemType;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(name = "owner_role_code", length = 30)
    private String ownerRoleCode;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OffboardingChecklistStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    private SecUserAccount completedBy;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "note", length = 1000)
    private String note;
}
