package com.company.hrm.module.attendance.entity;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "att_shift_version")
public class AttShiftVersion extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shift_version_id", nullable = false, updatable = false)
    private Long shiftVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private AttShift shift;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "crosses_midnight", nullable = false)
    private boolean crossesMidnight;

    @Column(name = "break_minutes", nullable = false)
    private Integer breakMinutes = 0;

    @Column(name = "late_grace_minutes", nullable = false)
    private Integer lateGraceMinutes = 0;

    @Column(name = "early_leave_grace_minutes", nullable = false)
    private Integer earlyLeaveGraceMinutes = 0;

    @Column(name = "ot_allowed", nullable = false)
    private boolean otAllowed;

    @Column(name = "night_shift", nullable = false)
    private boolean nightShift;

    @Column(name = "min_work_minutes_for_present")
    private Integer minWorkMinutesForPresent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(name = "note", length = 500)
    private String note;
}
