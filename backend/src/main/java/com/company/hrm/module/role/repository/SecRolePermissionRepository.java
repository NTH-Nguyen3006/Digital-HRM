package com.company.hrm.module.role.repository;

import com.company.hrm.module.role.entity.SecRolePermission;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SecRolePermissionRepository extends JpaRepository<SecRolePermission, UUID> {

    List<SecRolePermission> findAllByRoleRoleId(UUID roleId);

    void deleteAllByRoleRoleId(UUID roleId);

    @Query("""
            select rp.permission.permissionCode
            from SecRolePermission rp
            where rp.role.roleId = :roleId
              and rp.allowed = true
            order by rp.permission.permissionCode asc
            """)
    List<String> findAllowedPermissionCodes(UUID roleId);
}
