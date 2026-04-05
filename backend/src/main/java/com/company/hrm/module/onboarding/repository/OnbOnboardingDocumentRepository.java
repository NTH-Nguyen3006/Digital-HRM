package com.company.hrm.module.onboarding.repository;

import com.company.hrm.module.onboarding.entity.OnbOnboardingDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnbOnboardingDocumentRepository extends JpaRepository<OnbOnboardingDocument, Long> {

    List<OnbOnboardingDocument> findAllByOnboardingOnboardingIdAndDeletedFalseOrderByOnboardingDocumentIdAsc(Long onboardingId);

    Optional<OnbOnboardingDocument> findByOnboardingDocumentIdAndDeletedFalse(Long documentId);
}
