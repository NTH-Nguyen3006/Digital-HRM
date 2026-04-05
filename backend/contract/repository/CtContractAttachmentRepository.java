package com.company.hrm.module.contract.repository;

import com.company.hrm.module.contract.entity.CtContractAttachment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CtContractAttachmentRepository extends JpaRepository<CtContractAttachment, Long> {

    List<CtContractAttachment> findAllByLaborContract_LaborContractIdAndDeletedFalseOrderByUploadedAtDescContractAttachmentIdDesc(Long laborContractId);

    Optional<CtContractAttachment> findByContractAttachmentIdAndLaborContract_LaborContractIdAndDeletedFalse(Long contractAttachmentId, Long laborContractId);
}
