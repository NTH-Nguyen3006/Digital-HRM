package com.company.hrm.module.payroll.repository;

import com.company.hrm.module.payroll.entity.PayPersonalTaxProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayPersonalTaxProfileRepository extends JpaRepository<PayPersonalTaxProfile, Long> {

    Optional<PayPersonalTaxProfile> findByEmployeeEmployeeIdAndDeletedFalse(Long employeeId);
}
