package com.company.hrm.module.reporting.entity;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.constant.ReportCode;
import com.company.hrm.common.constant.ReportRunStatus;
import com.company.hrm.common.constant.ReportScheduleFrequency;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rep_report_schedule_config")
public class RepReportScheduleConfig extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_schedule_config_id", nullable = false, updatable = false)
    private Long reportScheduleConfigId;

    @Column(name = "schedule_code", nullable = false, length = 50)
    private String scheduleCode;

    @Column(name = "schedule_name", nullable = false, length = 200)
    private String scheduleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_code", nullable = false, length = 50)
    private ReportCode reportCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_code", nullable = false, length = 20)
    private ReportScheduleFrequency frequencyCode;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "run_at_hour", nullable = false)
    private Integer runAtHour;

    @Column(name = "run_at_minute", nullable = false)
    private Integer runAtMinute;

    @Column(name = "recipient_emails_csv", nullable = false, length = 1000)
    private String recipientEmailsCsv;

    @Column(name = "parameter_json", columnDefinition = "NVARCHAR(MAX)")
    private String parameterJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(name = "next_run_at")
    private LocalDateTime nextRunAt;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_run_status", length = 20)
    private ReportRunStatus lastRunStatus;

    @Column(name = "last_run_message", length = 1000)
    private String lastRunMessage;

    @Column(name = "description", length = 500)
    private String description;
}
