package com.company.hrm.module.contract.repository;

import com.company.hrm.module.contract.entity.CtContractAppendix;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CtContractAppendixRepository extends JpaRepository<CtContractAppendix, Long> {

    List<CtContractAppendix> findAllByLaborContract_LaborContractIdAndDeletedFalseOrderByEffectiveDateDescContractAppendixIdDesc(Long laborContractId);

    Optional<CtContractAppendix> findByContractAppendixIdAndLaborContract_LaborContractIdAndDeletedFalse(Long contractAppendixId, Long laborContractId);

    boolean existsByLaborContract_LaborContractIdAndAppendixNumberIgnoreCaseAndDeletedFalse(Long laborContractId, String appendixNumber);

    boolean existsByLaborContract_LaborContractIdAndAppendixNumberIgnoreCaseAndDeletedFalseAndContractAppendixIdNot(
            Long laborContractId,
            String appendixNumber,
            Long contractAppendixId
    );
}
