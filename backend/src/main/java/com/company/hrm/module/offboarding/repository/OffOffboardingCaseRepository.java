package com.company.hrm.module.offboarding.repository;

import com.company.hrm.common.constant.OffboardingStatus;
import com.company.hrm.module.offboarding.entity.OffOffboardingCase;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OffOffboardingCaseRepository extends JpaRepository<OffOffboardingCase, Long>, JpaSpecificationExecutor<OffOffboardingCase> {

    Optional<OffOffboardingCase> findByOffboardingCaseIdAndDeletedFalse(Long offboardingCaseId);

    Optional<OffOffboardingCase> findFirstByEmployeeEmployeeIdAndDeletedFalseAndStatusInOrderByCreatedAtDescOffboardingCaseIdDesc(
            Long employeeId,
            Collection<OffboardingStatus> statuses
    );

    List<OffOffboardingCase> findAllByEmployeeEmployeeIdAndDeletedFalseOrderByCreatedAtDescOffboardingCaseIdDesc(Long employeeId);
}
