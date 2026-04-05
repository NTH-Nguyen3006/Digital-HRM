package com.company.hrm.module.portal.dto;

import com.company.hrm.module.payroll.dto.SelfPayslipListItemResponse;
import java.util.List;

public record PortalPayrollOverviewResponse(
        List<SelfPayslipListItemResponse> payslips
) {
}
