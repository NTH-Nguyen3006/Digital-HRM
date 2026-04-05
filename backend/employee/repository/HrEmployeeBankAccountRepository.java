package com.company.hrm.module.employee.repository;

import com.company.hrm.module.employee.entity.HrEmployeeBankAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrEmployeeBankAccountRepository extends JpaRepository<HrEmployeeBankAccount, Long> {

    List<HrEmployeeBankAccount> findAllByEmployeeEmployeeIdAndDeletedFalseOrderByPrimaryDescEmployeeBankAccountIdAsc(Long employeeId);

    Optional<HrEmployeeBankAccount> findByEmployeeBankAccountIdAndDeletedFalse(Long employeeBankAccountId);
}
