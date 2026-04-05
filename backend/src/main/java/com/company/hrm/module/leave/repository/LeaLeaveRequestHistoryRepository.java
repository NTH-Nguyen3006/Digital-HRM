package com.company.hrm.module.leave.repository;

import com.company.hrm.module.leave.entity.LeaLeaveRequestHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaLeaveRequestHistoryRepository extends JpaRepository<LeaLeaveRequestHistory, Long> {

    List<LeaLeaveRequestHistory> findAllByLeaveRequestLeaveRequestIdOrderByChangedAtDescLeaveRequestHistoryIdDesc(Long leaveRequestId);
}
