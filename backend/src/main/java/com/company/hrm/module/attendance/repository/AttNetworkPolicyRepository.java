package com.company.hrm.module.attendance.repository;

import com.company.hrm.module.attendance.entity.AttNetworkPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AttNetworkPolicyRepository extends JpaRepository<AttNetworkPolicy, Long>, JpaSpecificationExecutor<AttNetworkPolicy> {

    Optional<AttNetworkPolicy> findByNetworkPolicyIdAndDeletedFalse(Long networkPolicyId);

    boolean existsByPolicyCodeIgnoreCaseAndDeletedFalse(String policyCode);

    boolean existsByPolicyCodeIgnoreCaseAndDeletedFalseAndNetworkPolicyIdNot(String policyCode, Long networkPolicyId);
}
