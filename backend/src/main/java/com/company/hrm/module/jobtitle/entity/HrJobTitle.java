package com.company.hrm.module.jobtitle.entity;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hr_job_title")
public class HrJobTitle extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_title_id", nullable = false, updatable = false)
    private Long jobTitleId;

    @Column(name = "job_title_code", nullable = false, length = 30)
    private String jobTitleCode;

    @Column(name = "job_title_name", nullable = false, length = 200)
    private String jobTitleName;

    @Column(name = "job_level_code", length = 30)
    private String jobLevelCode;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
