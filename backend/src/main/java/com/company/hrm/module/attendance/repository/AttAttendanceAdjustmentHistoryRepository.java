package com.company.hrm.module.attendance.repository;

import com.company.hrm.module.attendance.entity.AttAttendanceAdjustmentHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttAttendanceAdjustmentHistoryRepository extends JpaRepository<AttAttendanceAdjustmentHistory, Long> {

    List<AttAttendanceAdjustmentHistory> findAllByAdjustmentRequestAdjustmentRequestIdOrderByChangedAtAscAdjustmentHistoryIdAsc(Long adjustmentRequestId);
}
