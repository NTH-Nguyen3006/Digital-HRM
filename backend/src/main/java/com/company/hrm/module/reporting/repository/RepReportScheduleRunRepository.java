package com.company.hrm.module.reporting.repository;

import com.company.hrm.module.reporting.entity.RepReportScheduleRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepReportScheduleRunRepository extends JpaRepository<RepReportScheduleRun, Long> {

    List<RepReportScheduleRun> findAllByReportScheduleConfigReportScheduleConfigIdAndDeletedFalseOrderByStartedAtDescReportScheduleRunIdDesc(Long reportScheduleConfigId);
}
