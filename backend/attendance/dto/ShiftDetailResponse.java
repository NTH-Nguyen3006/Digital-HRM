package com.company.hrm.module.attendance.dto;

import java.util.List;

public record ShiftDetailResponse(
        Long shiftId,
        String shiftCode,
        String shiftName,
        String description,
        Integer sortOrder,
        String status,
        List<ShiftVersionResponse> versions
) {
}
