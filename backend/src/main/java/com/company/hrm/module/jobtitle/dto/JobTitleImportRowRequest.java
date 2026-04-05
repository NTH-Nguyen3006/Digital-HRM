package com.company.hrm.module.jobtitle.dto;

import com.company.hrm.common.constant.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JobTitleImportRowRequest(
        @NotBlank(message = "jobTitleCode là bắt buộc.")
        @Size(max = 30, message = "jobTitleCode tối đa 30 ký tự.")
        String jobTitleCode,

        @NotBlank(message = "jobTitleName là bắt buộc.")
        @Size(max = 200, message = "jobTitleName tối đa 200 ký tự.")
        String jobTitleName,

        @Size(max = 30, message = "jobLevelCode tối đa 30 ký tự.")
        String jobLevelCode,

        @Size(max = 1000, message = "description tối đa 1000 ký tự.")
        String description,

        Integer sortOrder,

        @NotNull(message = "status là bắt buộc.")
        RecordStatus status
) {
}
