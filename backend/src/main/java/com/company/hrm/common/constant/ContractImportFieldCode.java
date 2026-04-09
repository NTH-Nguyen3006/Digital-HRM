package com.company.hrm.common.constant;

public enum ContractImportFieldCode {
    CONTRACT_NUMBER("Số hợp đồng"),
    EMPLOYEE_CODE("Mã nhân viên"),
    EMPLOYEE_FULL_NAME("Họ tên nhân viên"),
    CONTRACT_TYPE_NAME("Loại hợp đồng"),
    SIGN_DATE("Ngày ký"),
    EFFECTIVE_DATE("Ngày hiệu lực"),
    END_DATE("Ngày kết thúc"),
    JOB_TITLE_NAME("Chức danh"),
    ORG_UNIT_NAME("Đơn vị"),
    WORK_LOCATION("Nơi làm việc"),
    BASE_SALARY("Lương cơ bản"),
    SALARY_CURRENCY("Tiền tệ"),
    WORKING_TYPE("Hình thức làm việc"),
    NOTE("Ghi chú");

    private final String label;

    ContractImportFieldCode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
