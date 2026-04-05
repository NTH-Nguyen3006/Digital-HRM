package com.company.hrm.module.offboarding.repository;

import com.company.hrm.common.constant.OffboardingChecklistStatus;
import com.company.hrm.module.offboarding.entity.OffOffboardingChecklistItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OffOffboardingChecklistItemRepository extends JpaRepository<OffOffboardingChecklistItem, Long> {

    List<OffOffboardingChecklistItem> findAllByOffboardingCaseOffboardingCaseIdAndDeletedFalseOrderByDueDateAscOffboardingChecklistItemIdAsc(Long offboardingCaseId);

    Optional<OffOffboardingChecklistItem> findByOffboardingChecklistItemIdAndDeletedFalse(Long offboardingChecklistItemId);

    long countByOffboardingCaseOffboardingCaseIdAndDeletedFalseAndStatusIn(Long offboardingCaseId, List<OffboardingChecklistStatus> statuses);
}
