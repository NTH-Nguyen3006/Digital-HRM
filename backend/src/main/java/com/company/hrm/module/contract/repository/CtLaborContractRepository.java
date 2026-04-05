package com.company.hrm.module.contract.repository;

import com.company.hrm.common.constant.ContractStatus;
import com.company.hrm.module.contract.entity.CtLaborContract;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CtLaborContractRepository extends JpaRepository<CtLaborContract, Long>, JpaSpecificationExecutor<CtLaborContract> {

    Optional<CtLaborContract> findByLaborContractIdAndDeletedFalse(Long laborContractId);

    boolean existsByContractNumberIgnoreCaseAndDeletedFalse(String contractNumber);

    boolean existsByContractNumberIgnoreCaseAndDeletedFalseAndLaborContractIdNot(String contractNumber, Long laborContractId);

    List<CtLaborContract> findAllByDeletedFalseAndContractStatusAndEndDateBetweenOrderByEndDateAsc(
            ContractStatus contractStatus,
            LocalDate fromDate,
            LocalDate toDate
    );
}
