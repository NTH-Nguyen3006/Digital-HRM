package com.company.hrm.module.employee.repository;

import com.company.hrm.module.employee.entity.HrEmployeeEmergencyContact;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrEmployeeEmergencyContactRepository extends JpaRepository<HrEmployeeEmergencyContact, Long> {

    List<HrEmployeeEmergencyContact> findAllByEmployeeEmployeeIdAndDeletedFalseOrderByPrimaryDescEmergencyContactIdAsc(Long employeeId);

    Optional<HrEmployeeEmergencyContact> findByEmergencyContactIdAndDeletedFalse(Long emergencyContactId);
}
