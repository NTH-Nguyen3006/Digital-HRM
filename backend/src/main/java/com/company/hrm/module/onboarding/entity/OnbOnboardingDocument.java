package com.company.hrm.module.onboarding.entity;

import com.company.hrm.common.constant.DocumentCategory;
import com.company.hrm.common.constant.DocumentStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "onb_onboarding_document")
public class OnbOnboardingDocument extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "onboarding_document_id", nullable = false, updatable = false)
    private Long onboardingDocumentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onboarding_id", nullable = false)
    private OnbOnboarding onboarding;

    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_category", nullable = false, length = 30)
    private DocumentCategory documentCategory;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DocumentStatus status;

    @Column(name = "note", length = 500)
    private String note;
}
