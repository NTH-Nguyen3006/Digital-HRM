package com.company.hrm.module.jobtitle.repository;

import com.company.hrm.module.jobtitle.entity.HrJobTitle;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HrJobTitleRepository extends JpaRepository<HrJobTitle, Long>, JpaSpecificationExecutor<HrJobTitle> {

    Optional<HrJobTitle> findByJobTitleIdAndDeletedFalse(Long jobTitleId);

    boolean existsByJobTitleCodeIgnoreCaseAndDeletedFalse(String jobTitleCode);

    boolean existsByJobTitleCodeIgnoreCaseAndDeletedFalseAndJobTitleIdNot(String jobTitleCode, Long jobTitleId);

    Optional<HrJobTitle> findByJobTitleCodeIgnoreCaseAndDeletedFalse(String jobTitleCode);
}
