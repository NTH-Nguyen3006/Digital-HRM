package com.company.hrm.module.attendance.repository;

import com.company.hrm.module.attendance.entity.AttDailyAttendance;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AttDailyAttendanceRepository extends JpaRepository<AttDailyAttendance, Long>, JpaSpecificationExecutor<AttDailyAttendance> {

    Optional<AttDailyAttendance> findByEmployeeEmployeeIdAndAttendanceDateAndDeletedFalse(Long employeeId, LocalDate attendanceDate);
}
