package com.company.hrm.module.attendance.entity;

import com.company.hrm.common.constant.AttendanceOvertimeStatus;
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
@Table(name = "att_overtime_request")
public class AttOvertimeRequest extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "overtime_request_id", nullable = false, updatable = false)
    private Long overtimeRequestId;

    @Column(name = "request_code", nullable = false, length = 30)
    private String requestCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "overtime_start_at", nullable = false)
    private LocalDateTime overtimeStartAt;

    @Column(name = "overtime_end_at", nullable = false)
    private LocalDateTime overtimeEndAt;

    @Column(name = "requested_minutes", nullable = false)
    private Integer requestedMinutes;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "evidence_file_key", length = 120)
    private String evidenceFileKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false, length = 20)
    private AttendanceOvertimeStatus requestStatus = AttendanceOvertimeStatus.SUBMITTED;

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

    @Column(name = "manager_note", length = 1000)
    private String managerNote;

    @Column(name = "rejection_note", length = 1000)
    private String rejectionNote;
}
