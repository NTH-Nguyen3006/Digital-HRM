package com.company.hrm.module.onboarding.repository;

import com.company.hrm.module.onboarding.entity.OnbOnboarding;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OnbOnboardingRepository extends JpaRepository<OnbOnboarding, Long>, JpaSpecificationExecutor<OnbOnboarding> {

    Optional<OnbOnboarding> findByOnboardingIdAndDeletedFalse(Long onboardingId);

    boolean existsByOnboardingCodeIgnoreCaseAndDeletedFalse(String onboardingCode);

    boolean existsByOnboardingCodeIgnoreCaseAndDeletedFalseAndOnboardingIdNot(String onboardingCode, Long onboardingId);
}
