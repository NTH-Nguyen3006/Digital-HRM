package com.company.hrm.module.portal.dto;

import com.company.hrm.module.contract.dto.LaborContractListItemResponse;
import java.util.List;

public record PortalContractOverviewResponse(
        List<LaborContractListItemResponse> contracts
) {
}
