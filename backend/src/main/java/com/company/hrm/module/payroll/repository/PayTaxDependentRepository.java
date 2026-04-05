package com.company.hrm.module.payroll.repository;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.module.payroll.entity.PayTaxDependent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayTaxDependentRepository extends JpaRepository<PayTaxDependent, Long> {

    Optional<PayTaxDependent> findByTaxDependentIdAndDeletedFalse(Long taxDependentId);

    List<PayTaxDependent> findAllByEmployeeEmployeeIdAndDeletedFalseOrderByDeductionStartMonthAscTaxDependentIdAsc(Long employeeId);

    List<PayTaxDependent> findAllByEmployeeEmployeeIdAndStatusAndDeletedFalseOrderByDeductionStartMonthAscTaxDependentIdAsc(
            Long employeeId,
            RecordStatus status
    );
}
