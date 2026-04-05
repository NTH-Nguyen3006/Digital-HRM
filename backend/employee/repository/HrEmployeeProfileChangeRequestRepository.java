package com.company.hrm.module.employee.repository;

import com.company.hrm.common.constant.ProfileChangeRequestStatus;
import com.company.hrm.module.employee.entity.HrEmployeeProfileChangeRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HrEmployeeProfileChangeRequestRepository extends JpaRepository<HrEmployeeProfileChangeRequest, Long>, JpaSpecificationExecutor<HrEmployeeProfileChangeRequest> {

    List<HrEmployeeProfileChangeRequest> findAllByEmployeeEmployeeIdAndDeletedFalseOrderBySubmittedAtDesc(Long employeeId);

    List<HrEmployeeProfileChangeRequest> findAllByRequesterUserUserIdAndDeletedFalseOrderBySubmittedAtDesc(java.util.UUID userId);

    Optional<HrEmployeeProfileChangeRequest> findByProfileChangeRequestIdAndDeletedFalse(Long requestId);

    boolean existsByEmployeeEmployeeIdAndRequestStatusAndDeletedFalse(Long employeeId, ProfileChangeRequestStatus status);
}
