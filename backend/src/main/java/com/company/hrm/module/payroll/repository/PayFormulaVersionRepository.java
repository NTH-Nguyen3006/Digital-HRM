package com.company.hrm.module.payroll.repository;

import com.company.hrm.module.payroll.entity.PayFormulaVersion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PayFormulaVersionRepository extends JpaRepository<PayFormulaVersion, Long>, JpaSpecificationExecutor<PayFormulaVersion> {

    Optional<PayFormulaVersion> findByFormulaVersionIdAndDeletedFalse(Long formulaVersionId);

    boolean existsByFormulaCodeIgnoreCaseAndDeletedFalse(String formulaCode);

    boolean existsByFormulaCodeIgnoreCaseAndDeletedFalseAndFormulaVersionIdNot(String formulaCode, Long formulaVersionId);

    @Query("""
            select f from PayFormulaVersion f
            where f.deleted = false
              and f.status = com.company.hrm.common.constant.RecordStatus.ACTIVE
              and f.effectiveFrom <= :date
              and (f.effectiveTo is null or f.effectiveTo >= :date)
            order by f.effectiveFrom desc, f.formulaVersionId desc
            """)
    List<PayFormulaVersion> findEffectiveVersions(LocalDate date);
}
