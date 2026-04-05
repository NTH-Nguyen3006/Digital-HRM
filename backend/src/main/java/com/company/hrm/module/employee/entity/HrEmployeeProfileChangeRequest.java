package com.company.hrm.module.employee.entity;

import com.company.hrm.common.constant.ProfileChangeRequestStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hr_employee_profile_change_request")
public class HrEmployeeProfileChangeRequest extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_change_request_id", nullable = false, updatable = false)
    private Long profileChangeRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id", nullable = false)
    private SecUserAccount requesterUser;

    @Column(name = "request_type", nullable = false, length = 30)
    private String requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false, length = 20)
    private ProfileChangeRequestStatus requestStatus;

    @Column(name = "payload_json", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String payloadJson;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_user_id")
    private SecUserAccount reviewerUser;

    @Column(name = "review_note", length = 1000)
    private String reviewNote;
}
