package com.company.hrm.module.employee.dto;

import com.company.hrm.common.constant.AddressType;
import jakarta.validation.constraints.*;

public record EmployeeAddressRequest(
        @NotNull(message = "addressType không được để trống.")
        AddressType addressType,
        @Size(max = 100, message = "countryName tối đa 100 ký tự.")
        String countryName,
        @Size(max = 100, message = "provinceName tối đa 100 ký tự.")
        String provinceName,
        @Size(max = 100, message = "districtName tối đa 100 ký tự.")
        String districtName,
        @Size(max = 100, message = "wardName tối đa 100 ký tự.")
        String wardName,
        @NotBlank(message = "addressLine không được để trống.")
        @Size(max = 300, message = "addressLine tối đa 300 ký tự.")
        String addressLine,
        @Size(max = 20, message = "postalCode tối đa 20 ký tự.")
        String postalCode,
        boolean primary
) {
}
