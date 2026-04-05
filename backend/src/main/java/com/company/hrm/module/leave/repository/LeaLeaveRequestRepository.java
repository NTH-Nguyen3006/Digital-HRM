package com.company.hrm.module.leave.repository;

import com.company.hrm.common.constant.LeaveRequestStatus;
import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.module.leave.entity.LeaLeaveRequest;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface LeaLeaveRequestRepository extends JpaRepository<LeaLeaveRequest, Long>, JpaSpecificationExecutor<LeaLeaveRequest> {

    Optional<LeaLeaveRequest> findByLeaveRequestIdAndDeletedFalse(Long leaveRequestId);

    boolean existsByRequestCodeIgnoreCaseAndDeletedFalse(String requestCode);

    List<LeaLeaveRequest> findAllByEmployeeEmployeeIdAndDeletedFalseOrderByCreatedAtDescLeaveRequestIdDesc(Long employeeId);

    @Query("""
            select count(r) > 0 from LeaLeaveRequest r
            where r.deleted = false
              and r.employee.employeeId = :employeeId
              and (:excludeLeaveRequestId is null or r.leaveRequestId <> :excludeLeaveRequestId)
              and r.requestStatus in :activeStatuses
              and r.startDate <= :endDate
              and r.endDate >= :startDate
            """)
    boolean existsOverlappingRequest(
            Long employeeId,
            Long excludeLeaveRequestId,
            Collection<LeaveRequestStatus> activeStatuses,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
            select r from LeaLeaveRequest r
            where r.deleted = false
              and r.requestStatus = :requestStatus
              and r.approvalRoleCode = :approvalRoleCode
              and r.employee.orgUnit.pathCode like concat(:orgPathPrefix, '%')
            order by r.startDate asc, r.leaveRequestId asc
            """)
    List<LeaLeaveRequest> findPendingByApprovalScope(
            LeaveRequestStatus requestStatus,
            RoleCode approvalRoleCode,
            String orgPathPrefix
    );

    @Query("""
            select r from LeaLeaveRequest r
            where r.deleted = false
              and r.requestStatus = :requestStatus
              and r.employee.orgUnit.pathCode like concat(:orgPathPrefix, '%')
              and r.startDate <= :toDate
              and r.endDate >= :fromDate
            order by r.startDate asc, r.leaveRequestId asc
            """)
    List<LeaLeaveRequest> findTeamCalendar(String orgPathPrefix, LocalDate fromDate, LocalDate toDate, LeaveRequestStatus requestStatus);

    @Query("""
            select count(r) > 0 from LeaLeaveRequest r
            where r.deleted = false
              and r.employee.employeeId = :employeeId
              and r.requestStatus = :requestStatus
              and r.startDate <= :attendanceDate
              and r.endDate >= :attendanceDate
            """)
    boolean existsFinalizedLeaveOnDate(Long employeeId, LocalDate attendanceDate, LeaveRequestStatus requestStatus);

    @Query("""
            select count(r) > 0 from LeaLeaveRequest r
            where r.deleted = false
              and r.employee.employeeId = :employeeId
              and r.requestStatus = :requestStatus
              and r.leaveTypeRule.paid = :paid
              and r.startDate <= :attendanceDate
              and r.endDate >= :attendanceDate
            """)
    boolean existsFinalizedLeaveOnDateAndPaid(Long employeeId, LocalDate attendanceDate, LeaveRequestStatus requestStatus, boolean paid);


}
