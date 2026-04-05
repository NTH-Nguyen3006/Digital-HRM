package com.company.hrm.module.attendance.repository;

import com.company.hrm.module.attendance.entity.AttAttendanceLog;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttAttendanceLogRepository extends JpaRepository<AttAttendanceLog, Long> {

    List<AttAttendanceLog> findAllByEmployeeEmployeeIdAndAttendanceDateAndDeletedFalseOrderByEventTimeAscAttendanceLogIdAsc(Long employeeId, LocalDate attendanceDate);

    List<AttAttendanceLog> findAllByEmployeeEmployeeIdAndAttendanceDateBetweenAndDeletedFalseOrderByEventTimeDescAttendanceLogIdDesc(
            Long employeeId,
            LocalDate fromDate,
            LocalDate toDate
    );

    boolean existsByAdjustmentRequestAdjustmentRequestIdAndDeletedFalse(Long adjustmentRequestId);
}
