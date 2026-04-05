package com.company.hrm.module.attendance.repository;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.module.attendance.entity.AttShiftVersion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AttShiftVersionRepository extends JpaRepository<AttShiftVersion, Long> {

    Optional<AttShiftVersion> findByShiftVersionIdAndDeletedFalse(Long shiftVersionId);

    List<AttShiftVersion> findAllByShiftShiftIdAndDeletedFalseOrderByVersionNoDesc(Long shiftId);

    @Query("select coalesce(max(v.versionNo), 0) from AttShiftVersion v where v.deleted = false and v.shift.shiftId = :shiftId")
    Integer findMaxVersionNoByShiftId(Long shiftId);

    @Query("""
            select v from AttShiftVersion v
            where v.deleted = false
              and v.shift.shiftId = :shiftId
              and v.status = :status
              and v.effectiveFrom <= :attendanceDate
              and (v.effectiveTo is null or v.effectiveTo >= :attendanceDate)
            order by v.versionNo desc
            """)
    List<AttShiftVersion> findActiveVersions(Long shiftId, LocalDate attendanceDate, RecordStatus status);

    default Optional<AttShiftVersion> findEffectiveVersion(Long shiftId, LocalDate attendanceDate) {
        List<AttShiftVersion> versions = findActiveVersions(shiftId, attendanceDate, RecordStatus.ACTIVE);
        return versions.stream().findFirst();
    }
}
