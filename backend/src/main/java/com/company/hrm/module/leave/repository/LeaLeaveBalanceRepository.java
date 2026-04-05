package com.company.hrm.module.leave.repository;

import com.company.hrm.common.constant.LeaveBalanceStatus;
import com.company.hrm.module.leave.entity.LeaLeaveBalance;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LeaLeaveBalanceRepository extends JpaRepository<LeaLeaveBalance, Long>, JpaSpecificationExecutor<LeaLeaveBalance> {

    Optional<LeaLeaveBalance> findByLeaveBalanceIdAndDeletedFalse(Long leaveBalanceId);

    Optional<LeaLeaveBalance> findByEmployeeEmployeeIdAndLeaveTypeLeaveTypeIdAndLeaveYearAndDeletedFalse(
            Long employeeId,
            Long leaveTypeId,
            Integer leaveYear
    );

    List<LeaLeaveBalance> findAllByEmployeeEmployeeIdAndDeletedFalseOrderByLeaveYearDescLeaveTypeLeaveTypeCodeAsc(Long employeeId);

    List<LeaLeaveBalance> findAllByLeaveYearAndDeletedFalseAndBalanceStatusIn(Integer leaveYear, Collection<LeaveBalanceStatus> statuses);

    List<LeaLeaveBalance> findAllByEmployeeEmployeeIdAndLeaveYearAndDeletedFalse(Long employeeId, Integer leaveYear);
}
