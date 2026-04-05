package com.company.hrm.module.contract.repository;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.module.contract.entity.CtContractType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CtContractTypeRepository extends JpaRepository<CtContractType, Long>, JpaSpecificationExecutor<CtContractType> {

    Optional<CtContractType> findByContractTypeIdAndDeletedFalse(Long contractTypeId);

    boolean existsByContractTypeCodeIgnoreCaseAndDeletedFalse(String contractTypeCode);

    boolean existsByContractTypeCodeIgnoreCaseAndDeletedFalseAndContractTypeIdNot(String contractTypeCode, Long contractTypeId);

    List<CtContractType> findAllByDeletedFalseAndStatusOrderByContractTypeNameAsc(RecordStatus status);
}
