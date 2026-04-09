package com.company.hrm.module.user.repository;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.module.user.entity.SecUserRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SecUserRoleRepository extends JpaRepository<SecUserRole, UUID> {

    @Query("""
            select ur
            from SecUserRole ur
            join fetch ur.role r
            where ur.user.userId = :userId
              and ur.primaryRole = true
              and ur.status = com.company.hrm.common.constant.RecordStatus.ACTIVE
              and ur.effectiveFrom <= :now
              and (ur.effectiveTo is null or ur.effectiveTo >= :now)
            order by ur.effectiveFrom desc
            """)
    Optional<SecUserRole> findActivePrimaryRole(UUID userId, LocalDateTime now);

    List<SecUserRole> findAllByUserUserIdOrderByEffectiveFromDesc(UUID userId);

    List<SecUserRole> findAllByRoleRoleIdAndStatus(UUID roleId, RecordStatus status);

    long countByRoleRoleIdAndStatus(UUID roleId, RecordStatus status);
}
