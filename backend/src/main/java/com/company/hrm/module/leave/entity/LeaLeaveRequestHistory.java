package com.company.hrm.module.leave.entity;

import com.company.hrm.common.constant.LeaveRequestStatus;
import com.company.hrm.common.entity.CreatedAuditEntity;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "lea_leave_request_history")
public class LeaLeaveRequestHistory extends CreatedAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_request_history_id", nullable = false, updatable = false)
    private Long leaveRequestHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_request_id", nullable = false)
    private LeaLeaveRequest leaveRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private LeaveRequestStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private LeaveRequestStatus toStatus;

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
