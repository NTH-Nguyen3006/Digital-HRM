package com.company.hrm.module.systemconfig.repository;

import com.company.hrm.module.systemconfig.entity.SysPlatformSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysPlatformSettingRepository extends JpaRepository<SysPlatformSetting, Long> {

    List<SysPlatformSetting> findAllByDeletedFalseOrderBySettingKeyAsc();

    Optional<SysPlatformSetting> findByPlatformSettingIdAndDeletedFalse(Long platformSettingId);

    Optional<SysPlatformSetting> findBySettingKeyIgnoreCaseAndDeletedFalse(String settingKey);
}
