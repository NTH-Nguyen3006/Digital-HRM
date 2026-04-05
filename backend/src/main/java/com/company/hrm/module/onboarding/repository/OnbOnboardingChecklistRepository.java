package com.company.hrm.module.onboarding.repository;

import com.company.hrm.module.onboarding.entity.OnbOnboardingChecklist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnbOnboardingChecklistRepository extends JpaRepository<OnbOnboardingChecklist, Long> {

    List<OnbOnboardingChecklist> findAllByOnboardingOnboardingIdAndDeletedFalseOrderByDueDateAscOnboardingChecklistIdAsc(Long onboardingId);

    Optional<OnbOnboardingChecklist> findByOnboardingChecklistIdAndDeletedFalse(Long checklistId);
}
