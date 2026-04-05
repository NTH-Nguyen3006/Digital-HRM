package com.company.hrm.module.leave.repository;

import com.company.hrm.module.leave.entity.LeaLeaveTypeRule;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LeaLeaveTypeRuleRepository extends JpaRepository<LeaLeaveTypeRule, Long> {

    List<LeaLeaveTypeRule> findAllByLeaveTypeLeaveTypeIdAndDeletedFalseOrderByVersionNoDesc(Long leaveTypeId);

    @Query("""
            select r from LeaLeaveTypeRule r
            where r.deleted = false
              and r.leaveType.leaveTypeId = :leaveTypeId
              and r.effectiveFrom <= :referenceDate
              and (r.effectiveTo is null or r.effectiveTo >= :referenceDate)
            order by r.versionNo desc
            """)
    List<LeaLeaveTypeRule> findEffectiveRules(Long leaveTypeId, LocalDate referenceDate);

    @Query("""
            select max(r.versionNo) from LeaLeaveTypeRule r
            where r.deleted = false and r.leaveType.leaveTypeId = :leaveTypeId
            """)
    Integer findMaxVersionNo(Long leaveTypeId);

    Optional<LeaLeaveTypeRule> findByLeaveTypeRuleIdAndDeletedFalse(Long leaveTypeRuleId);
}
