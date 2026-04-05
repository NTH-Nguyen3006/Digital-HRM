package com.company.hrm.module.attendance.entity;

import com.company.hrm.common.constant.AttendancePeriodStatus;
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
@Table(name = "att_attendance_period")
public class AttAttendancePeriod extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_period_id", nullable = false, updatable = false)
    private Long attendancePeriodId;

    @Column(name = "period_code", nullable = false, length = 20)
    private String periodCode;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_status", nullable = false, length = 20)
    private AttendancePeriodStatus periodStatus = AttendancePeriodStatus.DRAFT;

    @Column(name = "note", length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by")
    private SecUserAccount closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reopened_by")
    private SecUserAccount reopenedBy;

    @Column(name = "reopened_at")
    private LocalDateTime reopenedAt;

    @Column(name = "reopen_reason", length = 1000)
    private String reopenReason;

    @Column(name = "reopened_flag", nullable = false)
    private boolean reopenedFlag;

    @Column(name = "total_employee_count", nullable = false)
    private Integer totalEmployeeCount = 0;

    @Column(name = "total_anomaly_day_count", nullable = false)
    private Integer totalAnomalyDayCount = 0;
}
