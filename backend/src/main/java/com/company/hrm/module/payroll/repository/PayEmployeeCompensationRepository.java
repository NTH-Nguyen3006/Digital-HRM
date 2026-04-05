package com.company.hrm.module.payroll.repository;

import com.company.hrm.module.payroll.entity.PayEmployeeCompensation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PayEmployeeCompensationRepository extends JpaRepository<PayEmployeeCompensation, Long>, JpaSpecificationExecutor<PayEmployeeCompensation> {

    Optional<PayEmployeeCompensation> findByEmployeeCompensationIdAndDeletedFalse(Long employeeCompensationId);

    List<PayEmployeeCompensation> findAllByEmployeeEmployeeIdAndDeletedFalseOrderByEffectiveFromDescEmployeeCompensationIdDesc(Long employeeId);

    @Query("""
            select c from PayEmployeeCompensation c
            where c.deleted = false
              and c.employee.employeeId = :employeeId
              and c.status = com.company.hrm.common.constant.RecordStatus.ACTIVE
              and c.effectiveFrom <= :date
              and (c.effectiveTo is null or c.effectiveTo >= :date)
            order by c.effectiveFrom desc, c.employeeCompensationId desc
            """)
    List<PayEmployeeCompensation> findEffectiveByEmployeeAndDate(Long employeeId, LocalDate date);

    @Query("""
            select count(c) > 0 from PayEmployeeCompensation c
            where c.deleted = false
              and c.employee.employeeId = :employeeId
              and (:excludeId is null or c.employeeCompensationId <> :excludeId)
              and c.effectiveFrom <= :effectiveTo
              and (c.effectiveTo is null or c.effectiveTo >= :effectiveFrom)
            """)
    boolean existsOverlap(Long employeeId, Long excludeId, LocalDate effectiveFrom, LocalDate effectiveTo);
}
