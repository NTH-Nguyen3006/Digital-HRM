package com.company.hrm.module.role.repository;

import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.module.role.entity.SecRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecRoleRepository extends JpaRepository<SecRole, UUID> {

    List<SecRole> findAllByDeletedFalseOrderBySortOrderAscRoleNameAsc();

    Optional<SecRole> findByRoleCodeAndDeletedFalse(RoleCode roleCode);

    Optional<SecRole> findByRoleIdAndDeletedFalse(UUID roleId);

    boolean existsByRoleCodeAndDeletedFalse(RoleCode roleCode);
}
