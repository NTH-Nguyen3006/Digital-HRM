package com.company.hrm.module.onboarding.repository;

import com.company.hrm.module.onboarding.entity.OnbOnboardingNotificationLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnbOnboardingNotificationLogRepository extends JpaRepository<OnbOnboardingNotificationLog, Long> {

    List<OnbOnboardingNotificationLog> findAllByOnboardingOnboardingIdOrderBySentAtDescOnboardingNotificationLogIdDesc(Long onboardingId);
}
