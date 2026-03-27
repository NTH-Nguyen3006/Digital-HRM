package com.company.hrm.module.orgunit.repository;

import com.company.hrm.module.orgunit.entity.HrOrgUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HrOrgUnitRepository extends JpaRepository<HrOrgUnit, Long>, JpaSpecificationExecutor<HrOrgUnit> {

    Optional<HrOrgUnit> findByOrgUnitIdAndDeletedFalse(Long orgUnitId);

    boolean existsByOrgUnitCodeIgnoreCaseAndDeletedFalse(String orgUnitCode);

    boolean existsByOrgUnitCodeIgnoreCaseAndDeletedFalseAndOrgUnitIdNot(String orgUnitCode, Long orgUnitId);

    List<HrOrgUnit> findAllByDeletedFalseOrderByHierarchyLevelAscSortOrderAscOrgUnitNameAsc();

    List<HrOrgUnit> findAllByPathCodeStartingWithAndDeletedFalseOrderByHierarchyLevelAscSortOrderAscOrgUnitNameAsc(String pathCodePrefix);

    List<HrOrgUnit> findAllByOrgUnitIdInAndDeletedFalseOrderByHierarchyLevelAscSortOrderAscOrgUnitNameAsc(Collection<Long> orgUnitIds);
}
