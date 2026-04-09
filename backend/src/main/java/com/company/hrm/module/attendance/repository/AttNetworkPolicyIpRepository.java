package com.company.hrm.module.attendance.repository;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.module.attendance.entity.AttNetworkPolicyIp;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttNetworkPolicyIpRepository extends JpaRepository<AttNetworkPolicyIp, Long> {

    List<AttNetworkPolicyIp> findAllByNetworkPolicyNetworkPolicyIdAndDeletedFalseOrderByNetworkPolicyIpIdAsc(Long networkPolicyId);

    List<AttNetworkPolicyIp> findAllByNetworkPolicyNetworkPolicyIdAndStatusAndDeletedFalseOrderByNetworkPolicyIpIdAsc(Long networkPolicyId, RecordStatus status);

    Optional<AttNetworkPolicyIp> findByNetworkPolicyIpIdAndDeletedFalse(Long networkPolicyIpId);
}
