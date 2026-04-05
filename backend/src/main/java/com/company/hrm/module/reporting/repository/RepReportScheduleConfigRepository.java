package com.company.hrm.module.reporting.repository;

import com.company.hrm.common.constant.RecordStatus;
import com.company.hrm.module.reporting.entity.RepReportScheduleConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RepReportScheduleConfigRepository extends JpaRepository<RepReportScheduleConfig, Long>, JpaSpecificationExecutor<RepReportScheduleConfig> {

    Optional<RepReportScheduleConfig> findByReportScheduleConfigIdAndDeletedFalse(Long reportScheduleConfigId);

    boolean existsByScheduleCodeIgnoreCaseAndDeletedFalse(String scheduleCode);

    boolean existsByScheduleCodeIgnoreCaseAndDeletedFalseAndReportScheduleConfigIdNot(String scheduleCode, Long reportScheduleConfigId);

    List<RepReportScheduleConfig> findAllByDeletedFalseOrderByScheduleCodeAsc();

    List<RepReportScheduleConfig> findAllByDeletedFalseAndStatusAndNextRunAtLessThanEqualOrderByNextRunAtAsc(RecordStatus status, LocalDateTime nextRunAt);
}
