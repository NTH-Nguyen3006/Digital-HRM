package com.company.hrm.module.employee.repository;

import com.company.hrm.module.employee.entity.HrEmployeeAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrEmployeeAddressRepository extends JpaRepository<HrEmployeeAddress, Long> {

    List<HrEmployeeAddress> findAllByEmployeeEmployeeIdAndDeletedFalseOrderByPrimaryDescEmployeeAddressIdAsc(Long employeeId);

    Optional<HrEmployeeAddress> findByEmployeeAddressIdAndDeletedFalse(Long employeeAddressId);
}
