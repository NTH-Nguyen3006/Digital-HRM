package com.company.hrm.module.reporting.entity;

import com.company.hrm.common.constant.ReportRunStatus;
import com.company.hrm.common.constant.ReportRunTriggerType;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rep_report_schedule_run")
public class RepReportScheduleRun extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_schedule_run_id", nullable = false, updatable = false)
    private Long reportScheduleRunId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_schedule_config_id", nullable = false)
    private RepReportScheduleConfig reportScheduleConfig;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 20)
    private ReportRunTriggerType triggerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_user_id")
    private SecUserAccount triggeredByUser;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_status", nullable = false, length = 20)
    private ReportRunStatus runStatus;

    @Column(name = "output_file_key", length = 120)
    private String outputFileKey;

    @Column(name = "output_file_name", length = 255)
    private String outputFileName;

    @Column(name = "output_row_count")
    private Integer outputRowCount;

    @Column(name = "run_message", length = 1000)
    private String runMessage;
}
