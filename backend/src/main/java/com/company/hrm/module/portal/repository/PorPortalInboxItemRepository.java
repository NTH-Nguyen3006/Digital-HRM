package com.company.hrm.module.portal.repository;

import com.company.hrm.common.constant.PortalInboxItemType;
import com.company.hrm.common.constant.PortalTaskStatus;
import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.module.portal.entity.PorPortalInboxItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PorPortalInboxItemRepository extends JpaRepository<PorPortalInboxItem, Long>, JpaSpecificationExecutor<PorPortalInboxItem> {

    List<PorPortalInboxItem> findAllByUserUserIdAndDeletedFalseOrderByCreatedAtDescPortalInboxItemIdDesc(UUID userId);

    Optional<PorPortalInboxItem> findByPortalInboxItemIdAndDeletedFalse(Long portalInboxItemId);

    long countByUserUserIdAndDeletedFalseAndReadAtIsNullAndStatus(UUID userId, RecordStatus status);

    long countByUserUserIdAndDeletedFalseAndTaskStatusAndStatus(UUID userId, PortalTaskStatus taskStatus, RecordStatus status);

    long countByUserUserIdAndDeletedFalseAndItemTypeAndTaskStatusAndStatus(UUID userId, PortalInboxItemType itemType, PortalTaskStatus taskStatus, RecordStatus status);
}
