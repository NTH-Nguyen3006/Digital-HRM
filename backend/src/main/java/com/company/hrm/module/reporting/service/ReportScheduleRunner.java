package com.company.hrm.module.reporting.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReportScheduleRunner {

    private final ReportingService reportingService;

    public ReportScheduleRunner(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @Scheduled(fixedDelayString = "${app.reporting.scheduler-fixed-delay-ms:300000}")
    public void runDueSchedules() {
        reportingService.processDueSchedules();
    }
}
