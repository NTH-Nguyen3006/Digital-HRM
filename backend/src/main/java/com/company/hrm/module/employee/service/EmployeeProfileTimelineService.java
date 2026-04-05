package com.company.hrm.module.employee.service;

import com.company.hrm.module.employee.entity.HrEmployee;
import com.company.hrm.module.employee.entity.HrEmployeeProfileTimeline;
import com.company.hrm.module.employee.repository.HrEmployeeProfileTimelineRepository;
import com.company.hrm.module.employee.repository.HrEmployeeRepository;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.user.repository.SecUserAccountRepository;
import com.company.hrm.security.SecurityUserContext;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EmployeeProfileTimelineService {

    private final HrEmployeeRepository employeeRepository;
    private final HrEmployeeProfileTimelineRepository timelineRepository;
    private final SecUserAccountRepository userAccountRepository;

    public EmployeeProfileTimelineService(
            HrEmployeeRepository employeeRepository,
            HrEmployeeProfileTimelineRepository timelineRepository,
            SecUserAccountRepository userAccountRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.timelineRepository = timelineRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public void record(Long employeeId, String eventType, String summary, String detailJson) {
        HrEmployee employee = employeeRepository.findByEmployeeIdAndDeletedFalse(employeeId).orElse(null);
        if (employee == null) {
            return;
        }
        HrEmployeeProfileTimeline timeline = new HrEmployeeProfileTimeline();
        timeline.setEmployee(employee);
        timeline.setEventType(eventType);
        timeline.setSummary(summary);
        timeline.setDetailJson(detailJson);
        timeline.setActorUser(currentActor().orElse(null));
        timeline.setEventAt(LocalDateTime.now());
        timelineRepository.save(timeline);
    }

    private Optional<SecUserAccount> currentActor() {
        return SecurityUserContext.getCurrentUserId().flatMap(userAccountRepository::findById);
    }
}
