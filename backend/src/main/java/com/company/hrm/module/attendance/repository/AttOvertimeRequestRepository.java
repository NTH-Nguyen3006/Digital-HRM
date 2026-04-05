package com.company.hrm.module.attendance.repository;

import com.company.hrm.common.constant.AttendanceOvertimeStatus;
import com.company.hrm.module.attendance.entity.AttOvertimeRequest;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface AttOvertimeRequestRepository extends JpaRepository<AttOvertimeRequest, Long>, JpaSpecificationExecutor<AttOvertimeRequest> {

    Optional<AttOvertimeRequest> findByOvertimeRequestIdAndDeletedFalse(Long overtimeRequestId);

    boolean existsByRequestCodeIgnoreCaseAndDeletedFalse(String requestCode);

    List<AttOvertimeRequest> findAllByEmployeeEmployeeIdAndDeletedFalseOrderByCreatedAtDescOvertimeRequestIdDesc(Long employeeId);

    @Query("""
            select r from AttOvertimeRequest r
            where r.deleted = false
              and r.requestStatus = :requestStatus
              and r.employee.orgUnit.pathCode like concat(:orgPathPrefix, '%')
            order by r.attendanceDate asc, r.overtimeRequestId asc
            """)
    List<AttOvertimeRequest> findPendingByManagerScope(AttendanceOvertimeStatus requestStatus, String orgPathPrefix);

    @Query("""
            select coalesce(sum(r.requestedMinutes), 0) from AttOvertimeRequest r
            where r.deleted = false
              and r.employee.employeeId = :employeeId
              and r.attendanceDate = :attendanceDate
              and r.requestStatus = :requestStatus
              and (:excludeOvertimeRequestId is null or r.overtimeRequestId <> :excludeOvertimeRequestId)
            """)
    Integer sumApprovedMinutesByEmployeeAndDate(Long employeeId, LocalDate attendanceDate, AttendanceOvertimeStatus requestStatus, Long excludeOvertimeRequestId);

    @Query("""
            select coalesce(sum(r.requestedMinutes), 0) from AttOvertimeRequest r
            where r.deleted = false
              and r.employee.employeeId = :employeeId
              and year(r.attendanceDate) = :periodYear
              and month(r.attendanceDate) = :periodMonth
              and r.requestStatus = :requestStatus
              and (:excludeOvertimeRequestId is null or r.overtimeRequestId <> :excludeOvertimeRequestId)
            """)
    Integer sumApprovedMinutesByEmployeeAndMonth(Long employeeId, Integer periodYear, Integer periodMonth, AttendanceOvertimeStatus requestStatus, Long excludeOvertimeRequestId);

    @Query("""
            select count(r) from AttOvertimeRequest r
            where r.deleted = false
              and r.attendanceDate between :fromDate and :toDate
              and r.requestStatus in :statuses
            """)
    long countByAttendanceDateBetweenAndRequestStatusIn(LocalDate fromDate, LocalDate toDate, Collection<AttendanceOvertimeStatus> statuses);

    @Query("""
            select r from AttOvertimeRequest r
            where r.deleted = false
              and r.employee.employeeId = :employeeId
              and r.attendanceDate = :attendanceDate
              and r.requestStatus = :requestStatus
            order by r.overtimeStartAt asc, r.overtimeRequestId asc
            """)
    List<AttOvertimeRequest> findApprovedByEmployeeAndDate(Long employeeId, LocalDate attendanceDate, AttendanceOvertimeStatus requestStatus);
}
