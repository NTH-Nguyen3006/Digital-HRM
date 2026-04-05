package com.company.hrm.module.storage.entity;

import com.company.hrm.common.constant.StorageVisibilityScope;
import com.company.hrm.common.entity.SoftDeleteAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_stored_file")
public class SysStoredFile extends SoftDeleteAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stored_file_id", nullable = false, updatable = false)
    private Long storedFileId;

    @Column(name = "file_key", nullable = false, length = 120)
    private String fileKey;

    @Column(name = "module_code", nullable = false, length = 50)
    private String moduleCode;

    @Column(name = "business_category", nullable = false, length = 50)
    private String businessCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_scope", nullable = false, length = 20)
    private StorageVisibilityScope visibilityScope;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    @Column(name = "note", length = 500)
    private String note;
}
