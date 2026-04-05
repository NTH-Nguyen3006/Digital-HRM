package com.company.hrm.module.attendance.repository;

import com.company.hrm.common.constant.AttendanceAdjustmentStatus;
import com.company.hrm.module.attendance.entity.AttAttendanceAdjustmentRequest;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface AttAttendanceAdjustmentRequestRepository extends JpaRepository<AttAttendanceAdjustmentRequest, Long>, JpaSpecificationExecutor<AttAttendanceAdjustmentRequest> {

    Optional<AttAttendanceAdjustmentRequest> findByAdjustmentRequestIdAndDeletedFalse(Long adjustmentRequestId);

    boolean existsByRequestCodeIgnoreCaseAndDeletedFalse(String requestCode);

    List<AttAttendanceAdjustmentRequest> findAllByEmployeeEmployeeIdAndDeletedFalseOrderByCreatedAtDescAdjustmentRequestIdDesc(Long employeeId);

    @Query("""
            select r from AttAttendanceAdjustmentRequest r
            where r.deleted = false
              and r.requestStatus = :requestStatus
              and r.employee.orgUnit.pathCode like concat(:orgPathPrefix, '%')
            order by r.attendanceDate asc, r.adjustmentRequestId asc
            """)
    List<AttAttendanceAdjustmentRequest> findPendingByManagerScope(AttendanceAdjustmentStatus requestStatus, String orgPathPrefix);

    @Query("""
            select count(r) from AttAttendanceAdjustmentRequest r
            where r.deleted = false
              and r.attendanceDate between :fromDate and :toDate
              and r.requestStatus in :statuses
            """)
    long countByAttendanceDateBetweenAndRequestStatusIn(LocalDate fromDate, LocalDate toDate, Collection<AttendanceAdjustmentStatus> statuses);

    @Query("""
            select r from AttAttendanceAdjustmentRequest r
            where r.deleted = false
              and r.employee.employeeId = :employeeId
              and r.attendanceDate = :attendanceDate
              and r.requestStatus = :requestStatus
            order by r.finalizedAt desc, r.adjustmentRequestId desc
            """)
    List<AttAttendanceAdjustmentRequest> findByEmployeeDateAndStatus(Long employeeId, LocalDate attendanceDate, AttendanceAdjustmentStatus requestStatus);
}
