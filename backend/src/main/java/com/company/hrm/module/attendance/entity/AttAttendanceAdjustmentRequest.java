package com.company.hrm.module.attendance.entity;

import com.company.hrm.common.constant.AttendanceAdjustmentIssueType;
import com.company.hrm.common.constant.AttendanceAdjustmentStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "att_adjustment_request")
public class AttAttendanceAdjustmentRequest extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adjustment_request_id", nullable = false, updatable = false)
    private Long adjustmentRequestId;

    @Column(name = "request_code", nullable = false, length = 30)
    private String requestCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false, length = 30)
    private AttendanceAdjustmentIssueType issueType;

    @Column(name = "proposed_check_in_at")
    private LocalDateTime proposedCheckInAt;

    @Column(name = "proposed_check_out_at")
    private LocalDateTime proposedCheckOutAt;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "evidence_file_key", length = 120)
    private String evidenceFileKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false, length = 20)
    private AttendanceAdjustmentStatus requestStatus = AttendanceAdjustmentStatus.SUBMITTED;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private SecUserAccount approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by")
    private SecUserAccount rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finalized_by")
    private SecUserAccount finalizedBy;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canceled_by")
    private SecUserAccount canceledBy;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "manager_note", length = 1000)
    private String managerNote;

    @Column(name = "rejection_note", length = 1000)
    private String rejectionNote;

    @Column(name = "finalize_note", length = 1000)
    private String finalizeNote;

    @Column(name = "cancel_note", length = 1000)
    private String cancelNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "copied_from_adjustment_request_id")
    private AttAttendanceAdjustmentRequest copiedFromAdjustmentRequest;
}
