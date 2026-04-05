package com.company.hrm.module.employee.repository;

import com.company.hrm.module.employee.entity.HrEmployee;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HrEmployeeRepository extends JpaRepository<HrEmployee, Long>, JpaSpecificationExecutor<HrEmployee> {

    Optional<HrEmployee> findByEmployeeIdAndDeletedFalse(Long employeeId);

    boolean existsByEmployeeCodeIgnoreCaseAndDeletedFalse(String employeeCode);

    boolean existsByEmployeeCodeIgnoreCaseAndDeletedFalseAndEmployeeIdNot(String employeeCode, Long employeeId);

    boolean existsByWorkEmailIgnoreCaseAndDeletedFalse(String workEmail);

    boolean existsByWorkEmailIgnoreCaseAndDeletedFalseAndEmployeeIdNot(String workEmail, Long employeeId);

    List<HrEmployee> findAllByEmployeeIdInAndDeletedFalse(Collection<Long> employeeIds);
}
