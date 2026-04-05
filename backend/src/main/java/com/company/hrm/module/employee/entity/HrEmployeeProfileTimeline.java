package com.company.hrm.module.employee.entity;

import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hr_employee_profile_timeline")
public class HrEmployeeProfileTimeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_timeline_id", nullable = false, updatable = false)
    private Long profileTimelineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "summary", nullable = false, length = 255)
    private String summary;

    @Column(name = "detail_json", columnDefinition = "NVARCHAR(MAX)")
    private String detailJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private SecUserAccount actorUser;

    @Column(name = "event_at", nullable = false)
    private LocalDateTime eventAt;
}
