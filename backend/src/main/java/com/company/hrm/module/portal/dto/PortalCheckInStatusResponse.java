package com.company.hrm.module.portal.dto;

import java.time.LocalDateTime;

public record PortalCheckInStatusResponse(
        LocalDateTime lastEventTime,
        String lastEventType,
        boolean canCheckIn,
        boolean canCheckOut
) {}
