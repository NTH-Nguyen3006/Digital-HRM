package com.company.hrm.module.offboarding.repository;

import com.company.hrm.module.offboarding.entity.OffOffboardingHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OffOffboardingHistoryRepository extends JpaRepository<OffOffboardingHistory, Long> {

    List<OffOffboardingHistory> findAllByOffboardingCaseOffboardingCaseIdOrderByChangedAtDescOffboardingHistoryIdDesc(Long offboardingCaseId);
}
