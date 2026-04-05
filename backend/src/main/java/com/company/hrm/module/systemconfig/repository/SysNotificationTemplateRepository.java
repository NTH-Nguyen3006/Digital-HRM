package com.company.hrm.module.systemconfig.repository;

import com.company.hrm.module.systemconfig.entity.SysNotificationTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysNotificationTemplateRepository extends JpaRepository<SysNotificationTemplate, Long> {

    List<SysNotificationTemplate> findAllByDeletedFalseOrderByTemplateCodeAsc();

    Optional<SysNotificationTemplate> findByNotificationTemplateIdAndDeletedFalse(Long templateId);

    Optional<SysNotificationTemplate> findByTemplateCodeIgnoreCaseAndDeletedFalse(String templateCode);

    boolean existsByTemplateCodeIgnoreCaseAndDeletedFalse(String templateCode);

    boolean existsByTemplateCodeIgnoreCaseAndDeletedFalseAndNotificationTemplateIdNot(String templateCode, Long templateId);
}
