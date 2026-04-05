package com.company.hrm.module.storage.service;

import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.constant.StorageVisibilityScope;
import com.company.hrm.common.exception.BusinessException;
import com.company.hrm.common.exception.ForbiddenException;
import com.company.hrm.common.exception.NotFoundException;
import com.company.hrm.common.exception.UnauthorizedException;
import com.company.hrm.common.util.HashUtils;
import com.company.hrm.config.AppProperties;
import com.company.hrm.module.audit.service.AuditLogService;
import com.company.hrm.module.storage.dto.StoredFileDownload;
import com.company.hrm.module.storage.dto.StoredFileReference;
import com.company.hrm.module.storage.dto.StoredFileResponse;
import com.company.hrm.module.storage.entity.SysStoredFile;
import com.company.hrm.module.storage.repository.SysStoredFileRepository;
import com.company.hrm.security.SecurityUserContext;
import com.company.hrm.security.SecurityUserPrincipal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageFileService {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9_]{2,50}$");

    private final SysStoredFileRepository storedFileRepository;
    private final AuditLogService auditLogService;
    private final AppProperties appProperties;

    public StorageFileService(
            SysStoredFileRepository storedFileRepository,
            AuditLogService auditLogService,
            AppProperties appProperties
    ) {
        this.storedFileRepository = storedFileRepository;
        this.auditLogService = auditLogService;
        this.appProperties = appProperties;
    }

    @Transactional
    public StoredFileResponse upload(MultipartFile file, String moduleCode, String businessCategory, StorageVisibilityScope visibilityScope, String note) {
        SecurityUserPrincipal principal = SecurityUserContext.currentPrincipal()
                .orElseThrow(() -> new UnauthorizedException("AUTH_UNAUTHORIZED", "Phiên đăng nhập không hợp lệ hoặc đã hết hạn."));

        if (file == null || file.isEmpty()) {
            throw new BusinessException("STORAGE_FILE_EMPTY", "File upload không được để trống.", HttpStatus.BAD_REQUEST);
        }
        String normalizedModuleCode = normalizeCode(moduleCode, "moduleCode");
        String normalizedBusinessCategory = normalizeCode(businessCategory, "businessCategory");
        StorageVisibilityScope resolvedVisibilityScope = visibilityScope == null ? StorageVisibilityScope.HR_ADMIN : visibilityScope;
        validateVisibilityByRole(principal.getRoleCode(), resolvedVisibilityScope);
        validateFileSize(file.getSize());

        try {
            byte[] content = file.getBytes();
            return persistBinary(
                    sanitizeOriginalFileName(file.getOriginalFilename()),
                    normalizedModuleCode,
                    normalizedBusinessCategory,
                    resolvedVisibilityScope,
                    note,
                    content,
                    file.getContentType(),
                    "UPLOAD",
                    "Upload file vật lý thành công."
            );
        } catch (IOException exception) {
            throw new BusinessException("STORAGE_WRITE_FAILED", "Không thể ghi file vào storage nội bộ.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public StoredFileResponse storeInternalFile(
            String originalFileName,
            String moduleCode,
            String businessCategory,
            StorageVisibilityScope visibilityScope,
            String note,
            byte[] content,
            String mimeType
    ) {
        if (content == null || content.length == 0) {
            throw new BusinessException("STORAGE_FILE_EMPTY", "Nội dung file nội bộ không được để trống.", HttpStatus.BAD_REQUEST);
        }
        validateFileSize(content.length);
        return persistBinary(
                sanitizeOriginalFileName(originalFileName),
                normalizeCode(moduleCode, "moduleCode"),
                normalizeCode(businessCategory, "businessCategory"),
                visibilityScope == null ? StorageVisibilityScope.INTERNAL : visibilityScope,
                note,
                content,
                mimeType,
                "GENERATE",
                "Lưu file nội bộ thành công."
        );
    }

    @Transactional(readOnly = true)
    public StoredFileResponse getMetadata(String fileKey) {
        SysStoredFile entity = getActiveFile(fileKey);
        assertCanRead(entity);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public StoredFileDownload prepareDownload(String fileKey) {
        SysStoredFile entity = getActiveFile(fileKey);
        assertCanRead(entity);
        Path absolutePath = resolveAbsolutePath(entity.getStoragePath());
        if (!Files.exists(absolutePath) || !Files.isRegularFile(absolutePath)) {
            throw new NotFoundException("STORAGE_BINARY_NOT_FOUND", "Không tìm thấy file vật lý trên storage.");
        }
        return new StoredFileDownload(entity.getFileKey(), entity.getOriginalFileName(), entity.getMimeType(), entity.getFileSizeBytes(), absolutePath);
    }

    @Transactional(readOnly = true)
    public StoredFileReference requireActiveReference(String fileKey, String code, String message) {
        if (fileKey == null || fileKey.isBlank()) {
            throw new BusinessException(code, message, HttpStatus.BAD_REQUEST);
        }
        SysStoredFile entity = getActiveFile(fileKey.trim());
        return new StoredFileReference(entity.getFileKey(), entity.getMimeType(), entity.getFileSizeBytes());
    }

    private StoredFileResponse persistBinary(
            String originalFileName,
            String normalizedModuleCode,
            String normalizedBusinessCategory,
            StorageVisibilityScope visibilityScope,
            String note,
            byte[] content,
            String mimeType,
            String auditActionCode,
            String auditMessage
    ) {
        ensureBaseDirectory();
        String extension = extractExtension(originalFileName);
        String fileKey = UUID.randomUUID().toString().replace("-", "");
        LocalDate currentDate = LocalDate.now();
        String relativePath = normalizedModuleCode.toLowerCase(Locale.ROOT)
                + "/" + currentDate.getYear()
                + "/" + String.format("%02d", currentDate.getMonthValue())
                + "/" + String.format("%02d", currentDate.getDayOfMonth())
                + "/" + fileKey + extension;

        Path absolutePath = resolveAbsolutePath(relativePath);
        try {
            Files.createDirectories(absolutePath.getParent());
            Files.write(absolutePath, content);

            SysStoredFile entity = new SysStoredFile();
            entity.setFileKey(fileKey);
            entity.setModuleCode(normalizedModuleCode);
            entity.setBusinessCategory(normalizedBusinessCategory);
            entity.setVisibilityScope(visibilityScope);
            entity.setOriginalFileName(originalFileName);
            entity.setStoragePath(relativePath);
            entity.setMimeType(resolveMimeType(mimeType, absolutePath));
            entity.setFileSizeBytes((long) content.length);
            entity.setChecksumSha256(HashUtils.sha256(content));
            entity.setNote(blankToNull(note));
            entity = storedFileRepository.save(entity);

            StoredFileResponse response = toResponse(entity);
            auditLogService.logSuccess(auditActionCode, "STORAGE_FILE", "sys_stored_file", entity.getStoredFileId().toString(), null, response, auditMessage);
            return response;
        } catch (IOException exception) {
            throw new BusinessException("STORAGE_WRITE_FAILED", "Không thể ghi file vào storage nội bộ.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private SysStoredFile getActiveFile(String fileKey) {
        return storedFileRepository.findByFileKeyAndDeletedFalse(fileKey)
                .orElseThrow(() -> new NotFoundException("STORAGE_FILE_NOT_FOUND", "Không tìm thấy metadata file storage."));
    }

    private void assertCanRead(SysStoredFile entity) {
        SecurityUserPrincipal principal = SecurityUserContext.currentPrincipal()
                .orElseThrow(() -> new UnauthorizedException("AUTH_UNAUTHORIZED", "Phiên đăng nhập không hợp lệ hoặc đã hết hạn."));
        UUID currentUserId = principal.getUserId();
        if (currentUserId != null && currentUserId.equals(entity.getCreatedBy())) {
            return;
        }
        RoleCode roleCode = principal.getRoleCode();
        if (roleCode == RoleCode.ADMIN || roleCode == RoleCode.HR) {
            return;
        }
        if (roleCode == RoleCode.MANAGER && entity.getVisibilityScope() == StorageVisibilityScope.REVIEW_FLOW) {
            return;
        }
        throw new ForbiddenException("STORAGE_FILE_ACCESS_DENIED", "Bạn không có quyền truy cập file này.");
    }

    private void validateVisibilityByRole(RoleCode roleCode, StorageVisibilityScope visibilityScope) {
        if (roleCode == RoleCode.ADMIN || roleCode == RoleCode.HR) {
            return;
        }
        if (roleCode == RoleCode.MANAGER && visibilityScope == StorageVisibilityScope.INTERNAL) {
            throw new ForbiddenException("STORAGE_VISIBILITY_INVALID", "Manager không được upload file với visibility INTERNAL.");
        }
        if (roleCode == RoleCode.EMPLOYEE && visibilityScope == StorageVisibilityScope.INTERNAL) {
            throw new ForbiddenException("STORAGE_VISIBILITY_INVALID", "Employee không được upload file với visibility INTERNAL.");
        }
    }

    private void validateFileSize(long fileSize) {
        long maxBytes = (long) appProperties.getStorage().getMaxFileSizeMb() * 1024 * 1024;
        if (fileSize > maxBytes) {
            throw new BusinessException("STORAGE_FILE_TOO_LARGE", "Kích thước file vượt quá giới hạn cho phép.", HttpStatus.BAD_REQUEST);
        }
    }

    private void ensureBaseDirectory() {
        Path baseDir = baseDir();
        try {
            if (Files.exists(baseDir)) {
                return;
            }
            if (!appProperties.getStorage().isAutoCreateBaseDir()) {
                throw new BusinessException("STORAGE_BASE_DIR_MISSING", "Base directory của storage chưa tồn tại.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Files.createDirectories(baseDir);
        } catch (IOException exception) {
            throw new BusinessException("STORAGE_BASE_DIR_CREATE_FAILED", "Không thể khởi tạo thư mục storage gốc.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Path resolveAbsolutePath(String relativePath) {
        Path resolved = baseDir().resolve(relativePath).normalize();
        Path baseDir = baseDir().toAbsolutePath().normalize();
        Path absolute = resolved.toAbsolutePath().normalize();
        if (!absolute.startsWith(baseDir)) {
            throw new BusinessException("STORAGE_PATH_INVALID", "Đường dẫn storage không hợp lệ.", HttpStatus.BAD_REQUEST);
        }
        return absolute;
    }

    private Path baseDir() {
        return Paths.get(appProperties.getStorage().getBaseDir()).toAbsolutePath().normalize();
    }

    private String normalizeCode(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("STORAGE_PARAM_INVALID", fieldName + " là bắt buộc.", HttpStatus.BAD_REQUEST);
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!CODE_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException("STORAGE_PARAM_INVALID", fieldName + " chỉ cho phép A-Z, 0-9 và dấu gạch dưới, tối đa 50 ký tự.", HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private String sanitizeOriginalFileName(String originalFileName) {
        String safeName = originalFileName == null ? "file" : Paths.get(originalFileName).getFileName().toString().replaceAll("[\r\n]", "_").trim();
        return safeName.isBlank() ? "file" : safeName;
    }

    private String extractExtension(String originalFileName) {
        int lastDot = originalFileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == originalFileName.length() - 1) {
            return "";
        }
        String ext = originalFileName.substring(lastDot).toLowerCase(Locale.ROOT);
        return ext.length() > 20 ? "" : ext;
    }

    private String resolveMimeType(String providedMimeType, Path absolutePath) {
        String contentType = blankToNull(providedMimeType);
        if (contentType != null) {
            return contentType;
        }
        try {
            return blankToNull(Files.probeContentType(absolutePath));
        } catch (IOException exception) {
            return null;
        }
    }

    private StoredFileResponse toResponse(SysStoredFile entity) {
        return new StoredFileResponse(
                entity.getStoredFileId(),
                entity.getFileKey(),
                entity.getModuleCode(),
                entity.getBusinessCategory(),
                entity.getVisibilityScope().name(),
                entity.getOriginalFileName(),
                entity.getFileKey(),
                entity.getMimeType(),
                entity.getFileSizeBytes(),
                entity.getChecksumSha256(),
                entity.getNote(),
                "/api/v1/storage/files/" + entity.getFileKey() + "/download",
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
