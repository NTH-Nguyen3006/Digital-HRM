package com.company.hrm.module.payroll.repository;

import com.company.hrm.module.payroll.entity.PayPayrollItemLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayPayrollItemLineRepository extends JpaRepository<PayPayrollItemLine, Long> {

    List<PayPayrollItemLine> findAllByPayrollItemPayrollItemIdAndDeletedFalseOrderByDisplayOrderAscPayrollItemLineIdAsc(Long payrollItemId);
}
