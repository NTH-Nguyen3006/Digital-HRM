package com.company.hrm.module.employee.entity;

import com.company.hrm.common.constant.DocumentCategory;
import com.company.hrm.common.constant.DocumentStatus;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hr_employee_document")
public class HrEmployeeDocument extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_document_id", nullable = false, updatable = false)
    private Long employeeDocumentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private HrEmployee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_category", nullable = false, length = 30)
    private DocumentCategory documentCategory;

    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "uploaded_by")
    private UUID uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DocumentStatus status;

    @Column(name = "note", length = 500)
    private String note;
}
