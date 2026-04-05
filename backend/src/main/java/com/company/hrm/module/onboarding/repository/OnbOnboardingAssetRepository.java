package com.company.hrm.module.onboarding.repository;

import com.company.hrm.module.onboarding.entity.OnbOnboardingAsset;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnbOnboardingAssetRepository extends JpaRepository<OnbOnboardingAsset, Long> {

    List<OnbOnboardingAsset> findAllByOnboardingOnboardingIdAndDeletedFalseOrderByOnboardingAssetIdAsc(Long onboardingId);

    Optional<OnbOnboardingAsset> findByOnboardingAssetIdAndDeletedFalse(Long assetId);
}
