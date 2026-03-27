package com.company.hrm.module.employee.repository;

import com.company.hrm.module.employee.entity.HrEmployeeDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrEmployeeDocumentRepository extends JpaRepository<HrEmployeeDocument, Long> {

    List<HrEmployeeDocument> findAllByEmployeeEmployeeIdAndDeletedFalseOrderByUploadedAtDescEmployeeDocumentIdDesc(Long employeeId);

    Optional<HrEmployeeDocument> findByEmployeeDocumentIdAndDeletedFalse(Long employeeDocumentId);
}
