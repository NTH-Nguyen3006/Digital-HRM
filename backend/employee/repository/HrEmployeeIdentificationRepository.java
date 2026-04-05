package com.company.hrm.module.employee.repository;

import com.company.hrm.module.employee.entity.HrEmployeeIdentification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrEmployeeIdentificationRepository extends JpaRepository<HrEmployeeIdentification, Long> {

    List<HrEmployeeIdentification> findAllByEmployeeEmployeeIdAndDeletedFalseOrderByPrimaryDescEmployeeIdentificationIdAsc(Long employeeId);

    Optional<HrEmployeeIdentification> findByEmployeeIdentificationIdAndDeletedFalse(Long employeeIdentificationId);

    boolean existsByDocumentTypeAndDocumentNumberAndDeletedFalse(com.company.hrm.common.constant.IdentificationDocumentType documentType, String documentNumber);

    boolean existsByDocumentTypeAndDocumentNumberAndDeletedFalseAndEmployeeIdentificationIdNot(com.company.hrm.common.constant.IdentificationDocumentType documentType, String documentNumber, Long employeeIdentificationId);
}
