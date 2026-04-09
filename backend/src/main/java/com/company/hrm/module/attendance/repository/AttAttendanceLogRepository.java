package com.company.hrm.module.attendance.repository;

import com.company.hrm.module.attendance.entity.AttAttendanceLog;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttAttendanceLogRepository extends JpaRepository<AttAttendanceLog, Long> {

    boolean existsByEmployeeEmployeeIdAndAttendanceDateAndEventTypeAndDeletedFalse(Long employeeId, java.time.LocalDate attendanceDate, com.company.hrm.common.constant.AttendanceLogEventType eventType);

    List<AttAttendanceLog> findAllByEmployeeEmployeeIdAndAttendanceDateAndDeletedFalseOrderByEventTimeAscAttendanceLogIdAsc(Long employeeId, LocalDate attendanceDate);

    List<AttAttendanceLog> findAllByEmployeeEmployeeIdAndAttendanceDateBetweenAndDeletedFalseOrderByEventTimeDescAttendanceLogIdDesc(
            Long employeeId,
            LocalDate fromDate,
            LocalDate toDate
    );

    boolean existsByAdjustmentRequestAdjustmentRequestIdAndDeletedFalse(Long adjustmentRequestId);
}
