package com.company.hrm.module.permission.repository;

import com.company.hrm.module.permission.entity.SecPermission;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecPermissionRepository extends JpaRepository<SecPermission, UUID> {

    List<SecPermission> findAllByDeletedFalseOrderByModuleCodeAscActionCodeAsc();

    List<SecPermission> findByPermissionCodeInAndDeletedFalse(Collection<String> permissionCodes);

    Optional<SecPermission> findByPermissionCodeAndDeletedFalse(String permissionCode);
}
