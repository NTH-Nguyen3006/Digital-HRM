package com.company.hrm.module.payroll.repository;

import com.company.hrm.common.constant.PayrollItemStatus;
import com.company.hrm.module.payroll.entity.PayPayrollItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PayPayrollItemRepository extends JpaRepository<PayPayrollItem, Long>, JpaSpecificationExecutor<PayPayrollItem> {

    Optional<PayPayrollItem> findByPayrollItemIdAndDeletedFalse(Long payrollItemId);

    List<PayPayrollItem> findAllByPayrollPeriodPayrollPeriodIdAndDeletedFalseOrderByEmployeeEmployeeCodeAscPayrollItemIdAsc(Long payrollPeriodId);

    Optional<PayPayrollItem> findByPayrollPeriodPayrollPeriodIdAndEmployeeEmployeeIdAndDeletedFalse(Long payrollPeriodId, Long employeeId);

    long countByPayrollPeriodPayrollPeriodIdAndDeletedFalse(Long payrollPeriodId);

    long countByPayrollPeriodPayrollPeriodIdAndItemStatusAndDeletedFalse(Long payrollPeriodId, PayrollItemStatus itemStatus);

    @Query("""
            select i from PayPayrollItem i
            where i.deleted = false
              and i.payrollPeriod.payrollPeriodId = :payrollPeriodId
              and i.employee.orgUnit.pathCode like concat(:orgPathPrefix, '%')
            order by i.employee.employeeCode asc, i.payrollItemId asc
            """)
    List<PayPayrollItem> findByPayrollPeriodAndManagerScope(Long payrollPeriodId, String orgPathPrefix);
}
