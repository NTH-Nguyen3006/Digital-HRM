package com.company.hrm.module.storage.controller;

import com.company.hrm.common.constant.StorageVisibilityScope;
import com.company.hrm.common.response.ApiResponse;
import com.company.hrm.module.audit.support.RequestTraceContext;
import com.company.hrm.module.storage.dto.StoredFileDownload;
import com.company.hrm.module.storage.dto.StoredFileResponse;
import com.company.hrm.module.storage.service.StorageFileService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/storage/files")
public class StorageController {

    private final StorageFileService storageFileService;

    public StorageController(StorageFileService storageFileService) {
        this.storageFileService = storageFileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('storage.file.upload')")
    public ApiResponse<StoredFileResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("moduleCode") String moduleCode,
            @RequestParam("businessCategory") String businessCategory,
            @RequestParam(value = "visibilityScope", required = false) StorageVisibilityScope visibilityScope,
            @RequestParam(value = "note", required = false) String note
    ) {
        return ApiResponse.success(
                "STORAGE_UPLOAD_SUCCESS",
                "Upload file vật lý thành công.",
                storageFileService.upload(file, moduleCode, businessCategory, visibilityScope, note),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @GetMapping("/{fileKey}")
    @PreAuthorize("hasAuthority('storage.file.view')")
    public ApiResponse<StoredFileResponse> getMetadata(@PathVariable String fileKey) {
        return ApiResponse.success(
                "STORAGE_METADATA_SUCCESS",
                "Lấy metadata file storage thành công.",
                storageFileService.getMetadata(fileKey),
                null,
                RequestTraceContext.getTraceId()
        );
    }

    @GetMapping("/{fileKey}/download")
    @PreAuthorize("hasAuthority('storage.file.download')")
    public ResponseEntity<InputStreamResource> download(@PathVariable String fileKey) throws IOException {
        StoredFileDownload file = storageFileService.prepareDownload(fileKey);
        MediaType mediaType = file.mimeType() == null || file.mimeType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(file.mimeType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(file.fileSizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.originalFileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(new InputStreamResource(java.nio.file.Files.newInputStream(file.absolutePath())));
    }
}
