package com.company.hrm.module.payroll.repository;

import com.company.hrm.module.payroll.entity.PayPayrollPeriod;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PayPayrollPeriodRepository extends JpaRepository<PayPayrollPeriod, Long>, JpaSpecificationExecutor<PayPayrollPeriod> {

    Optional<PayPayrollPeriod> findByPayrollPeriodIdAndDeletedFalse(Long payrollPeriodId);

    Optional<PayPayrollPeriod> findByPeriodYearAndPeriodMonthAndDeletedFalse(Integer periodYear, Integer periodMonth);

    List<PayPayrollPeriod> findAllByDeletedFalseOrderByPeriodYearDescPeriodMonthDesc();
}
