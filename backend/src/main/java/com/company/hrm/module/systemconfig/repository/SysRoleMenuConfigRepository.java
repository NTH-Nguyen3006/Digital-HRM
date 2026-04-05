package com.company.hrm.module.systemconfig.repository;

import com.company.hrm.module.systemconfig.entity.SysRoleMenuConfig;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysRoleMenuConfigRepository extends JpaRepository<SysRoleMenuConfig, Long> {

    List<SysRoleMenuConfig> findAllByRoleRoleIdAndDeletedFalseOrderBySortOrderAscMenuNameAsc(UUID roleId);

    void deleteAllByRoleRoleId(UUID roleId);
}
