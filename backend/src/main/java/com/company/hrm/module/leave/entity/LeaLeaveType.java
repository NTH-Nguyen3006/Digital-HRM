package com.company.hrm.module.leave.entity;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "lea_leave_type")
public class LeaLeaveType extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_type_id", nullable = false, updatable = false)
    private Long leaveTypeId;

    @Column(name = "leave_type_code", nullable = false, length = 30)
    private String leaveTypeCode;

    @Column(name = "leave_type_name", nullable = false, length = 200)
    private String leaveTypeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status;

    @Column(name = "description", length = 500)
    private String description;
}
