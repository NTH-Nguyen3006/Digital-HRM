package com.company.hrm.module.offboarding.repository;

import com.company.hrm.common.constant.OffboardingAssetReturnStatus;
import com.company.hrm.module.offboarding.entity.OffOffboardingAssetReturn;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OffOffboardingAssetReturnRepository extends JpaRepository<OffOffboardingAssetReturn, Long> {

    List<OffOffboardingAssetReturn> findAllByOffboardingCaseOffboardingCaseIdAndDeletedFalseOrderByExpectedReturnDateAscOffboardingAssetReturnIdAsc(Long offboardingCaseId);

    Optional<OffOffboardingAssetReturn> findByOffboardingAssetReturnIdAndDeletedFalse(Long offboardingAssetReturnId);

    long countByOffboardingCaseOffboardingCaseIdAndDeletedFalseAndStatus(Long offboardingCaseId, OffboardingAssetReturnStatus status);
}
