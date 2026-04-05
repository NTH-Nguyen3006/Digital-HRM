package com.company.hrm.module.attendance.repository;

import com.company.hrm.module.attendance.entity.AttShiftAssignment;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface AttShiftAssignmentRepository extends JpaRepository<AttShiftAssignment, Long>, JpaSpecificationExecutor<AttShiftAssignment> {

    Optional<AttShiftAssignment> findByShiftAssignmentIdAndDeletedFalse(Long shiftAssignmentId);

    @Query("""
            select a from AttShiftAssignment a
            where a.deleted = false
              and a.employee.employeeId = :employeeId
              and a.effectiveFrom <= :attendanceDate
              and (a.effectiveTo is null or a.effectiveTo >= :attendanceDate)
            order by a.effectiveFrom desc, a.shiftAssignmentId desc
            """)
    List<AttShiftAssignment> findEffectiveAssignments(Long employeeId, LocalDate attendanceDate);

    default Optional<AttShiftAssignment> findEffectiveAssignment(Long employeeId, LocalDate attendanceDate) {
        return findEffectiveAssignments(employeeId, attendanceDate).stream().findFirst();
    }

    @Query("""
            select count(a) > 0 from AttShiftAssignment a
            where a.deleted = false
              and a.employee.employeeId = :employeeId
              and (:excludeAssignmentId is null or a.shiftAssignmentId <> :excludeAssignmentId)
              and a.effectiveFrom <= :effectiveTo
              and (a.effectiveTo is null or a.effectiveTo >= :effectiveFrom)
            """)
    boolean existsOverlappingAssignment(Long employeeId, Long excludeAssignmentId, LocalDate effectiveFrom, LocalDate effectiveTo);

    List<AttShiftAssignment> findAllByEmployeeEmployeeIdInAndDeletedFalse(Collection<Long> employeeIds);
}
