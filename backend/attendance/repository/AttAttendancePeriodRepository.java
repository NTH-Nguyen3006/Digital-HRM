package com.company.hrm.module.attendance.repository;

import com.company.hrm.common.constant.AttendancePeriodStatus;
import com.company.hrm.module.attendance.entity.AttAttendancePeriod;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AttAttendancePeriodRepository extends JpaRepository<AttAttendancePeriod, Long> {

    Optional<AttAttendancePeriod> findByAttendancePeriodIdAndDeletedFalse(Long attendancePeriodId);

    Optional<AttAttendancePeriod> findByPeriodYearAndPeriodMonthAndDeletedFalse(Integer periodYear, Integer periodMonth);

    List<AttAttendancePeriod> findAllByDeletedFalseOrderByPeriodYearDescPeriodMonthDesc();

    @Query("""
            select p from AttAttendancePeriod p
            where p.deleted = false
              and p.periodStatus = :periodStatus
              and p.periodStartDate <= :attendanceDate
              and p.periodEndDate >= :attendanceDate
            """)
    List<AttAttendancePeriod> findByDateAndStatus(LocalDate attendanceDate, AttendancePeriodStatus periodStatus);

    default Optional<AttAttendancePeriod> findClosedPeriodByDate(LocalDate attendanceDate) {
        return findByDateAndStatus(attendanceDate, AttendancePeriodStatus.CLOSED).stream().findFirst();
    }
}
