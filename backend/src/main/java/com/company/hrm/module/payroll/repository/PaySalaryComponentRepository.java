package com.company.hrm.module.payroll.repository;

import com.company.hrm.module.payroll.entity.PaySalaryComponent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaySalaryComponentRepository extends JpaRepository<PaySalaryComponent, Long>, JpaSpecificationExecutor<PaySalaryComponent> {

    Optional<PaySalaryComponent> findBySalaryComponentIdAndDeletedFalse(Long salaryComponentId);

    boolean existsByComponentCodeIgnoreCaseAndDeletedFalse(String componentCode);

    boolean existsByComponentCodeIgnoreCaseAndDeletedFalseAndSalaryComponentIdNot(String componentCode, Long salaryComponentId);
}
