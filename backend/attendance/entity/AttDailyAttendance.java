package com.company.hrm.module.attendance.entity;

import com.company.hrm.common.constant.AttendanceDailyStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.employee.entity.HrEmployee;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "att_daily_attendance")
public class AttDailyAttendance extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_attendance_id", nullable = false, updatable = false)
    private Long dailyAttendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_assignment_id")
    private AttShiftAssignment shiftAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_version_id")
    private AttShiftVersion shiftVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_period_id")
    private AttAttendancePeriod attendancePeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finalized_adjustment_request_id")
    private AttAttendanceAdjustmentRequest finalizedAdjustmentRequest;

    @Column(name = "planned_start_at")
    private LocalDateTime plannedStartAt;

    @Column(name = "planned_end_at")
    private LocalDateTime plannedEndAt;

    @Column(name = "actual_check_in_at")
    private LocalDateTime actualCheckInAt;

    @Column(name = "actual_check_out_at")
    private LocalDateTime actualCheckOutAt;

    @Column(name = "worked_minutes", nullable = false)
    private Integer workedMinutes = 0;

    @Column(name = "late_minutes", nullable = false)
    private Integer lateMinutes = 0;

    @Column(name = "early_leave_minutes", nullable = false)
    private Integer earlyLeaveMinutes = 0;

    @Column(name = "approved_ot_minutes", nullable = false)
    private Integer approvedOtMinutes = 0;

    @Column(name = "missing_check_in", nullable = false)
    private boolean missingCheckIn;

    @Column(name = "missing_check_out", nullable = false)
    private boolean missingCheckOut;

    @Column(name = "anomaly_count", nullable = false)
    private Integer anomalyCount = 0;

    @Column(name = "anomaly_codes", length = 500)
    private String anomalyCodes;

    @Enumerated(EnumType.STRING)
    @Column(name = "daily_status", nullable = false, length = 20)
    private AttendanceDailyStatus dailyStatus = AttendanceDailyStatus.ABSENT;

    @Column(name = "on_leave", nullable = false)
    private boolean onLeave;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
}
