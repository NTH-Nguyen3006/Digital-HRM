package com.company.hrm.module.payroll.repository;

import com.company.hrm.module.payroll.entity.PayEmployeeCompensationItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayEmployeeCompensationItemRepository extends JpaRepository<PayEmployeeCompensationItem, Long> {

    List<PayEmployeeCompensationItem> findAllByEmployeeCompensationEmployeeCompensationIdAndDeletedFalseOrderBySalaryComponentDisplayOrderAscEmployeeCompensationItemIdAsc(Long employeeCompensationId);
}
