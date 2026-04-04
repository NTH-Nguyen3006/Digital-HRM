import axios from "axios";

const EMPLOYEE_API = "/api/v1/admin/employees";

// ─────────────────────────────────────────────
// EMPLOYEE CORE
// ─────────────────────────────────────────────

/**
 * Lấy danh sách nhân sự (phân trang, tìm kiếm, lọc)
 *
 * @param {Object} params
 * @param {string}  [params.keyword]           - Tìm theo employeeCode / fullName / email
 * @param {string}  [params.employmentStatus]  - EmploymentStatus: ACTIVE | RESIGNED | ON_LEAVE | ...
 * @param {number}  [params.orgUnitId]          - Lọc theo đơn vị tổ chức
 * @param {number}  [params.jobTitleId]         - Lọc theo chức danh
 * @param {number}  [params.page=0]             - Trang hiện tại (0-indexed)
 * @param {number}  [params.size=20]            - Số bản ghi mỗi trang
 * @returns {Promise<ApiResponse<PageResponse<EmployeeListItemResponse>>>}
 *
 * EmployeeListItemResponse: { employeeId, employeeCode, fullName, workEmail, workPhone,
 *   genderCode, employmentStatus, orgUnitId, orgUnitName, jobTitleId, jobTitleName,
 *   managerEmployeeId, managerEmployeeName, hireDate, avatarUrl }
 */
export const getEmployees = async ({
    keyword,
    employmentStatus,
    orgUnitId,
    jobTitleId,
    page = 0,
    size = 20,
} = {}) => {
    try {
        const params = { page, size };
        if (keyword)          params.keyword          = keyword;
        if (employmentStatus) params.employmentStatus = employmentStatus;
        if (orgUnitId)        params.orgUnitId        = orgUnitId;
        if (jobTitleId)       params.jobTitleId       = jobTitleId;

        const response = await axios.get(EMPLOYEE_API, { params });
        return response.data;
    } catch (error) {
        console.error("getEmployees failed:", error);
        throw error;
    }
};

/**
 * Lấy chi tiết nhân sự
 *
 * @param {number} employeeId
 * @returns {Promise<ApiResponse<EmployeeDetailResponse>>}
 *
 * EmployeeDetailResponse: { employeeId, employeeCode, fullName, workEmail, workPhone,
 *   genderCode, dateOfBirth, hireDate, employmentStatus, workLocation, taxCode,
 *   personalEmail, mobilePhone, avatarUrl, note,
 *   orgUnitId, orgUnitCode, orgUnitName, orgUnitType,
 *   jobTitleId, jobTitleCode, jobTitleName, jobLevelCode,
 *   managerEmployeeId, managerEmployeeCode, managerEmployeeName }
 */
export const getEmployeeDetail = async (employeeId) => {
    try {
        const response = await axios.get(`${EMPLOYEE_API}/${employeeId}`);
        return response.data;
    } catch (error) {
        console.error("getEmployeeDetail failed:", error);
        throw error;
    }
};

/**
 * Tạo mới nhân sự
 *
 * @param {Object}  payload
 * @param {string}  payload.employeeCode         - Mã nhân viên (bắt buộc, ≤30 ký tự)
 * @param {number}  payload.orgUnitId             - ID đơn vị (bắt buộc)
 * @param {number}  payload.jobTitleId            - ID chức danh (bắt buộc)
 * @param {number}  [payload.managerEmployeeId]  - ID quản lý trực tiếp
 * @param {string}  payload.fullName              - Họ tên (bắt buộc, ≤200 ký tự)
 * @param {string}  [payload.workEmail]           - Email công ty (≤150 ký tự)
 * @param {string}  [payload.workPhone]           - Điện thoại công ty (≤20 ký tự)
 * @param {string}  payload.genderCode            - GenderCode (bắt buộc): MALE | FEMALE | OTHER
 * @param {string}  payload.dateOfBirth           - Ngày sinh ISO date (bắt buộc)
 * @param {string}  payload.hireDate              - Ngày vào làm ISO date (bắt buộc)
 * @param {string}  payload.employmentStatus      - EmploymentStatus (bắt buộc)
 * @param {string}  [payload.workLocation]        - Địa điểm làm việc (≤200 ký tự)
 * @param {string}  [payload.taxCode]             - Mã số thuế (≤30 ký tự)
 * @param {string}  [payload.personalEmail]       - Email cá nhân (≤150 ký tự)
 * @param {string}  [payload.mobilePhone]         - SĐT cá nhân (≤20 ký tự)
 * @param {string}  [payload.avatarUrl]           - URL ảnh đại diện (≤500 ký tự)
 * @param {string}  [payload.note]                - Ghi chú (≤1000 ký tự)
 * @returns {Promise<ApiResponse<EmployeeDetailResponse>>}
 */
export const createEmployee = async (payload) => {
    try {
        const response = await axios.post(EMPLOYEE_API, payload);
        return response.data;
    } catch (error) {
        console.error("createEmployee failed:", error);
        throw error;
    }
};

/**
 * Cập nhật thông tin nhân sự (không đổi trạng thái lao động qua endpoint này)
 *
 * @param {number} employeeId
 * @param {Object} payload - Giống CreateEmployeeRequest nhưng không có employmentStatus
 * @returns {Promise<ApiResponse<EmployeeDetailResponse>>}
 */
export const updateEmployee = async (employeeId, payload) => {
    try {
        const response = await axios.put(`${EMPLOYEE_API}/${employeeId}`, payload);
        return response.data;
    } catch (error) {
        console.error("updateEmployee failed:", error);
        throw error;
    }
};

/**
 * Cập nhật trạng thái lao động của nhân sự
 *
 * @param {number} employeeId
 * @param {Object} payload
 * @param {string}       payload.employmentStatus - EmploymentStatus (bắt buộc)
 * @param {string|null}  [payload.note]           - Ghi chú lý do
 * @returns {Promise<ApiResponse<EmployeeDetailResponse>>}
 */
export const changeEmploymentStatus = async (employeeId, payload) => {
    try {
        const response = await axios.patch(`${EMPLOYEE_API}/${employeeId}/employment-status`, payload);
        return response.data;
    } catch (error) {
        console.error("changeEmploymentStatus failed:", error);
        throw error;
    }
};

/**
 * Điều chuyển nhân sự sang đơn vị / quản lý mới
 *
 * @param {number} employeeId
 * @param {Object} payload
 * @param {number}       payload.targetOrgUnitId          - ID đơn vị mới (bắt buộc)
 * @param {number|null}  [payload.targetManagerEmployeeId] - ID quản lý mới
 * @param {string|null}  [payload.note]                   - Ghi chú
 * @returns {Promise<ApiResponse<EmployeeDetailResponse>>}
 */
export const transferEmployee = async (employeeId, payload) => {
    try {
        const response = await axios.patch(`${EMPLOYEE_API}/${employeeId}/transfer`, payload);
        return response.data;
    } catch (error) {
        console.error("transferEmployee failed:", error);
        throw error;
    }
};

// ─────────────────────────────────────────────
// EMPLOYEE PROFILE (hồ sơ mở rộng)
// ─────────────────────────────────────────────

/**
 * Lấy hồ sơ mở rộng của nhân sự
 *
 * @param {number} employeeId
 * @returns {Promise<ApiResponse<EmployeeProfileResponse>>}
 */
export const getEmployeeProfile = async (employeeId) => {
    try {
        const response = await axios.get(`${EMPLOYEE_API}/${employeeId}/profile`);
        return response.data;
    } catch (error) {
        console.error("getEmployeeProfile failed:", error);
        throw error;
    }
};

/**
 * Tạo / cập nhật (upsert) hồ sơ mở rộng của nhân sự
 *
 * @param {number} employeeId
 * @param {Object} payload  - EmployeeProfileRequest fields
 * @returns {Promise<ApiResponse<EmployeeProfileResponse>>}
 */
export const upsertEmployeeProfile = async (employeeId, payload) => {
    try {
        const response = await axios.put(`${EMPLOYEE_API}/${employeeId}/profile`, payload);
        return response.data;
    } catch (error) {
        console.error("upsertEmployeeProfile failed:", error);
        throw error;
    }
};

// ─────────────────────────────────────────────
// ADDRESSES (địa chỉ)
// ─────────────────────────────────────────────

export const listAddresses = async (employeeId) => {
    try {
        const response = await axios.get(`${EMPLOYEE_API}/${employeeId}/addresses`);
        return response.data;
    } catch (error) {
        console.error("listAddresses failed:", error);
        throw error;
    }
};

export const createAddress = async (employeeId, payload) => {
    try {
        const response = await axios.post(`${EMPLOYEE_API}/${employeeId}/addresses`, payload);
        return response.data;
    } catch (error) {
        console.error("createAddress failed:", error);
        throw error;
    }
};

export const updateAddress = async (employeeId, addressId, payload) => {
    try {
        const response = await axios.put(`${EMPLOYEE_API}/${employeeId}/addresses/${addressId}`, payload);
        return response.data;
    } catch (error) {
        console.error("updateAddress failed:", error);
        throw error;
    }
};

export const deleteAddress = async (employeeId, addressId) => {
    try {
        const response = await axios.delete(`${EMPLOYEE_API}/${employeeId}/addresses/${addressId}`);
        return response.data;
    } catch (error) {
        console.error("deleteAddress failed:", error);
        throw error;
    }
};

// ─────────────────────────────────────────────
// EMERGENCY CONTACTS (liên hệ khẩn cấp)
// ─────────────────────────────────────────────

export const listEmergencyContacts = async (employeeId) => {
    try {
        const response = await axios.get(`${EMPLOYEE_API}/${employeeId}/emergency-contacts`);
        return response.data;
    } catch (error) {
        console.error("listEmergencyContacts failed:", error);
        throw error;
    }
};

export const createEmergencyContact = async (employeeId, payload) => {
    try {
        const response = await axios.post(`${EMPLOYEE_API}/${employeeId}/emergency-contacts`, payload);
        return response.data;
    } catch (error) {
        console.error("createEmergencyContact failed:", error);
        throw error;
    }
};

export const updateEmergencyContact = async (employeeId, emergencyContactId, payload) => {
    try {
        const response = await axios.put(`${EMPLOYEE_API}/${employeeId}/emergency-contacts/${emergencyContactId}`, payload);
        return response.data;
    } catch (error) {
        console.error("updateEmergencyContact failed:", error);
        throw error;
    }
};

export const deleteEmergencyContact = async (employeeId, emergencyContactId) => {
    try {
        const response = await axios.delete(`${EMPLOYEE_API}/${employeeId}/emergency-contacts/${emergencyContactId}`);
        return response.data;
    } catch (error) {
        console.error("deleteEmergencyContact failed:", error);
        throw error;
    }
};

// ─────────────────────────────────────────────
// IDENTIFICATIONS (giấy tờ định danh)
// ─────────────────────────────────────────────

export const listIdentifications = async (employeeId) => {
    try {
        const response = await axios.get(`${EMPLOYEE_API}/${employeeId}/identifications`);
        return response.data;
    } catch (error) {
        console.error("listIdentifications failed:", error);
        throw error;
    }
};

export const createIdentification = async (employeeId, payload) => {
    try {
        const response = await axios.post(`${EMPLOYEE_API}/${employeeId}/identifications`, payload);
        return response.data;
    } catch (error) {
        console.error("createIdentification failed:", error);
        throw error;
    }
};

export const updateIdentification = async (employeeId, identificationId, payload) => {
    try {
        const response = await axios.put(`${EMPLOYEE_API}/${employeeId}/identifications/${identificationId}`, payload);
        return response.data;
    } catch (error) {
        console.error("updateIdentification failed:", error);
        throw error;
    }
};

export const deleteIdentification = async (employeeId, identificationId) => {
    try {
        const response = await axios.delete(`${EMPLOYEE_API}/${employeeId}/identifications/${identificationId}`);
        return response.data;
    } catch (error) {
        console.error("deleteIdentification failed:", error);
        throw error;
    }
};

// ─────────────────────────────────────────────
// BANK ACCOUNTS (tài khoản ngân hàng)
// ─────────────────────────────────────────────

export const listBankAccounts = async (employeeId) => {
    try {
        const response = await axios.get(`${EMPLOYEE_API}/${employeeId}/bank-accounts`);
        return response.data;
    } catch (error) {
        console.error("listBankAccounts failed:", error);
        throw error;
    }
};

export const createBankAccount = async (employeeId, payload) => {
    try {
        const response = await axios.post(`${EMPLOYEE_API}/${employeeId}/bank-accounts`, payload);
        return response.data;
    } catch (error) {
        console.error("createBankAccount failed:", error);
        throw error;
    }
};

export const updateBankAccount = async (employeeId, bankAccountId, payload) => {
    try {
        const response = await axios.put(`${EMPLOYEE_API}/${employeeId}/bank-accounts/${bankAccountId}`, payload);
        return response.data;
    } catch (error) {
        console.error("updateBankAccount failed:", error);
        throw error;
    }
};

export const deleteBankAccount = async (employeeId, bankAccountId) => {
    try {
        const response = await axios.delete(`${EMPLOYEE_API}/${employeeId}/bank-accounts/${bankAccountId}`);
        return response.data;
    } catch (error) {
        console.error("deleteBankAccount failed:", error);
        throw error;
    }
};

// ─────────────────────────────────────────────
// DOCUMENTS (tài liệu nhân sự)
// ─────────────────────────────────────────────

export const listDocuments = async (employeeId) => {
    try {
        const response = await axios.get(`${EMPLOYEE_API}/${employeeId}/documents`);
        return response.data;
    } catch (error) {
        console.error("listDocuments failed:", error);
        throw error;
    }
};

export const createDocument = async (employeeId, payload) => {
    try {
        const response = await axios.post(`${EMPLOYEE_API}/${employeeId}/documents`, payload);
        return response.data;
    } catch (error) {
        console.error("createDocument failed:", error);
        throw error;
    }
};

export const updateDocument = async (employeeId, documentId, payload) => {
    try {
        const response = await axios.put(`${EMPLOYEE_API}/${employeeId}/documents/${documentId}`, payload);
        return response.data;
    } catch (error) {
        console.error("updateDocument failed:", error);
        throw error;
    }
};

export const deleteDocument = async (employeeId, documentId) => {
    try {
        const response = await axios.delete(`${EMPLOYEE_API}/${employeeId}/documents/${documentId}`);
        return response.data;
    } catch (error) {
        console.error("deleteDocument failed:", error);
        throw error;
    }
};
