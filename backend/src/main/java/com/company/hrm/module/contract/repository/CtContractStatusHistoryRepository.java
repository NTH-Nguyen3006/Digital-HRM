package com.company.hrm.module.contract.repository;

import com.company.hrm.module.contract.entity.CtContractStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CtContractStatusHistoryRepository extends JpaRepository<CtContractStatusHistory, Long> {

    List<CtContractStatusHistory> findAllByLaborContract_LaborContractIdOrderByChangedAtDescContractStatusHistoryIdDesc(Long laborContractId);
}
