package com.company.hrm.module.portal.entity;

import com.company.hrm.common.constant.PortalInboxItemType;
import com.company.hrm.common.constant.PortalTaskStatus;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "por_portal_inbox_item")
public class PorPortalInboxItem extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portal_inbox_item_id", nullable = false, updatable = false)
    private Long portalInboxItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private SecUserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private HrEmployee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private PortalInboxItemType itemType;

    @Column(name = "category_code", length = 50)
    private String categoryCode;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @Column(name = "related_module", length = 50)
    private String relatedModule;

    @Column(name = "related_entity_id", length = 100)
    private String relatedEntityId;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", nullable = false, length = 20)
    private PortalTaskStatus taskStatus;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;
}
