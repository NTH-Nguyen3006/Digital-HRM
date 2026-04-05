package com.company.hrm.module.employee.repository;

import com.company.hrm.module.employee.entity.HrEmployeeProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrEmployeeProfileRepository extends JpaRepository<HrEmployeeProfile, Long> {

    Optional<HrEmployeeProfile> findByEmployeeEmployeeIdAndDeletedFalse(Long employeeId);
}
