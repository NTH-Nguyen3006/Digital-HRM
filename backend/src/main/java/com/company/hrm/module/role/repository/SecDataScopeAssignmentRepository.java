package com.company.hrm.module.role.repository;

import com.company.hrm.common.constant.DataScopeSubjectType;
import com.company.hrm.module.role.entity.SecDataScopeAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecDataScopeAssignmentRepository extends JpaRepository<SecDataScopeAssignment, UUID> {

    List<SecDataScopeAssignment> findAllBySubjectTypeAndSubjectIdOrderByPriorityOrderAsc(DataScopeSubjectType subjectType, UUID subjectId);

    void deleteAllBySubjectTypeAndSubjectId(DataScopeSubjectType subjectType, UUID subjectId);
}
