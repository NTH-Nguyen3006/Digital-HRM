package com.company.hrm.module.onboarding.dto;

public record OnboardingDocumentResponse(
        Long onboardingDocumentId,
        String documentName,
        String documentCategory,
        String storagePath,
        String mimeType,
        Long fileSizeBytes,
        String status,
        String note
) {
}
