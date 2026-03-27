package com.company.hrm.module.jobtitle.dto;

import jakarta.validation.constraints.*;

public record CreateJobTitleRequest(
        @NotBlank(message = "jobTitleCode không được để trống.")
        @Size(max = 30, message = "jobTitleCode tối đa 30 ký tự.")
        String jobTitleCode,
        @NotBlank(message = "jobTitleName không được để trống.")
        @Size(max = 200, message = "jobTitleName tối đa 200 ký tự.")
        String jobTitleName,
        @Size(max = 30, message = "jobLevelCode tối đa 30 ký tự.")
        String jobLevelCode,
        @Size(max = 1000, message = "description tối đa 1000 ký tự.")
        String description,
        @Min(value = 0, message = "sortOrder phải lớn hơn hoặc bằng 0.")
        Integer sortOrder
) {
}
