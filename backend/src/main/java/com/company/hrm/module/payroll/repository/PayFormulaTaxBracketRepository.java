package com.company.hrm.module.payroll.repository;

import com.company.hrm.module.payroll.entity.PayFormulaTaxBracket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayFormulaTaxBracketRepository extends JpaRepository<PayFormulaTaxBracket, Long> {

    List<PayFormulaTaxBracket> findAllByFormulaVersionFormulaVersionIdAndDeletedFalseOrderBySequenceNoAsc(Long formulaVersionId);
}
