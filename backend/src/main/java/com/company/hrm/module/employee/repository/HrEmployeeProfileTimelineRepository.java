package com.company.hrm.module.employee.repository;

import com.company.hrm.module.employee.entity.HrEmployeeProfileTimeline;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrEmployeeProfileTimelineRepository extends JpaRepository<HrEmployeeProfileTimeline, Long> {

    List<HrEmployeeProfileTimeline> findAllByEmployeeEmployeeIdOrderByEventAtDesc(Long employeeId);
}
