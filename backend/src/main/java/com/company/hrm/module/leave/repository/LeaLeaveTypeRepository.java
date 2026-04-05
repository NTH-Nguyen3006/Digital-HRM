package com.company.hrm.module.leave.repository;

import com.company.hrm.module.leave.entity.LeaLeaveType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LeaLeaveTypeRepository extends JpaRepository<LeaLeaveType, Long>, JpaSpecificationExecutor<LeaLeaveType> {

    Optional<LeaLeaveType> findByLeaveTypeIdAndDeletedFalse(Long leaveTypeId);

    boolean existsByLeaveTypeCodeIgnoreCaseAndDeletedFalse(String leaveTypeCode);

    boolean existsByLeaveTypeCodeIgnoreCaseAndDeletedFalseAndLeaveTypeIdNot(String leaveTypeCode, Long leaveTypeId);
}
