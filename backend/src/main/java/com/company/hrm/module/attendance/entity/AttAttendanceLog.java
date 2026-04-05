package com.company.hrm.module.attendance.entity;

import com.company.hrm.common.constant.AttendanceLogEventType;
import com.company.hrm.common.constant.AttendanceLogSourceType;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.employee.entity.HrEmployee;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "att_attendance_log")
public class AttAttendanceLog extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_log_id", nullable = false, updatable = false)
    private Long attendanceLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private AttendanceLogEventType eventType;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private AttendanceLogSourceType sourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_assignment_id")
    private AttShiftAssignment shiftAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjustment_request_id")
    private AttAttendanceAdjustmentRequest adjustmentRequest;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "device_ref", length = 100)
    private String deviceRef;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "raw_payload_json", columnDefinition = "NVARCHAR(MAX)")
    private String rawPayloadJson;
}
