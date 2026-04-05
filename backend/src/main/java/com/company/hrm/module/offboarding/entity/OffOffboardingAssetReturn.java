package com.company.hrm.module.offboarding.entity;

import com.company.hrm.common.constant.OffboardingAssetReturnStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import com.company.hrm.module.user.entity.SecUserAccount;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "off_offboarding_asset_return")
public class OffOffboardingAssetReturn extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offboarding_asset_return_id", nullable = false, updatable = false)
    private Long offboardingAssetReturnId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offboarding_case_id", nullable = false)
    private OffOffboardingCase offboardingCase;

    @Column(name = "asset_code", length = 50)
    private String assetCode;

    @Column(name = "asset_name", nullable = false, length = 255)
    private String assetName;

    @Column(name = "asset_type", length = 50)
    private String assetType;

    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;

    @Column(name = "returned_date")
    private LocalDate returnedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OffboardingAssetReturnStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id")
    private SecUserAccount updatedByUser;

    @Column(name = "updated_at_action")
    private LocalDateTime updatedAtAction;

    @Column(name = "note", length = 1000)
    private String note;
}
