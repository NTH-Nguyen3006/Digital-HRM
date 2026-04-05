package com.company.hrm.module.leave.entity;

import com.company.hrm.common.constant.LeaveRequestStatus;
import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "lea_leave_request")
public class LeaLeaveRequest extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_request_id", nullable = false, updatable = false)
    private Long leaveRequestId;

    @Column(name = "request_code", nullable = false, length = 30)
    private String requestCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaLeaveType leaveType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_rule_id", nullable = false)
    private LeaLeaveTypeRule leaveTypeRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_role_code", nullable = false, length = 20)
    private RoleCode approvalRoleCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false, length = 20)
    private LeaveRequestStatus requestStatus;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "requested_units", nullable = false, precision = 9, scale = 2)
    private BigDecimal requestedUnits;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private SecUserAccount approvedBy;

    @Column(name = "approval_note", length = 1000)
    private String approvalNote;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by")
    private SecUserAccount rejectedBy;

    @Column(name = "rejection_note", length = 1000)
    private String rejectionNote;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finalized_by")
    private SecUserAccount finalizedBy;

    @Column(name = "finalize_note", length = 1000)
    private String finalizeNote;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canceled_by")
    private SecUserAccount canceledBy;

    @Column(name = "cancel_note", length = 1000)
    private String cancelNote;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "attachment_ref", length = 500)
    private String attachmentRef;
}
