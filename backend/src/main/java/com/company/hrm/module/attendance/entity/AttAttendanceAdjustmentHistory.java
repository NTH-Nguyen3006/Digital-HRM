package com.company.hrm.module.attendance.entity;

import com.company.hrm.common.constant.AttendanceAdjustmentStatus;
import com.company.hrm.common.entity.CreatedAuditEntity;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "att_adjustment_history")
public class AttAttendanceAdjustmentHistory extends CreatedAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adjustment_history_id", nullable = false, updatable = false)
    private Long adjustmentHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjustment_request_id", nullable = false)
    private AttAttendanceAdjustmentRequest adjustmentRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private AttendanceAdjustmentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private AttendanceAdjustmentStatus toStatus;

    @Column(name = "action_code", nullable = false, length = 50)
    private String actionCode;

    @Column(name = "action_note", length = 1000)
    private String actionNote;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private SecUserAccount changedBy;

    @Column(name = "snapshot_json", columnDefinition = "NVARCHAR(MAX)")
    private String snapshotJson;
}
