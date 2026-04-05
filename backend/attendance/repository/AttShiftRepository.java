package com.company.hrm.module.attendance.repository;

import com.company.hrm.module.attendance.entity.AttShift;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AttShiftRepository extends JpaRepository<AttShift, Long>, JpaSpecificationExecutor<AttShift> {

    Optional<AttShift> findByShiftIdAndDeletedFalse(Long shiftId);

    boolean existsByShiftCodeIgnoreCaseAndDeletedFalse(String shiftCode);

    boolean existsByShiftCodeIgnoreCaseAndDeletedFalseAndShiftIdNot(String shiftCode, Long shiftId);
}
