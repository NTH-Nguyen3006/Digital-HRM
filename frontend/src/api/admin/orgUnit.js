import axios from "axios";

const ORG_UNIT_API = "/api/v1/admin/org-units";

/**
 * Lấy danh sách đơn vị tổ chức (phân trang, tìm kiếm, lọc trạng thái)
 *
 * @param {Object} params
 * @param {string}  [params.keyword]     - Tìm theo orgUnitCode / orgUnitName
 * @param {string}  [params.status]      - RecordStatus: ACTIVE | INACTIVE
 * @param {number}  [params.page=0]      - Trang hiện tại (0-indexed)
 * @param {number}  [params.size=20]     - Số bản ghi mỗi trang
 * @returns {Promise<ApiResponse<PageResponse<OrgUnitListItemResponse>>>}
 *
 * OrgUnitListItemResponse: { orgUnitId, parentOrgUnitId, orgUnitCode, orgUnitName,
 *   orgUnitType, hierarchyLevel, status, managerEmployeeId, managerEmployeeName }
 */
export const getOrgUnits = async ({
    keyword,
    status,
    page = 0,
    size = 20,
} = {}) => {
    try {
        const params = { page, size };
        if (keyword) params.keyword = keyword;
        if (status)  params.status  = status;

        const response = await axios.get(ORG_UNIT_API, { params });
        return response.data;
    } catch (error) {
        console.error("getOrgUnits failed:", error);
        throw error;
    }
};

/**
 * Lấy cây cơ cấu tổ chức (dạng đệ quy)
 *
 * @returns {Promise<ApiResponse<OrgUnitTreeNodeResponse[]>>}
 *
 * OrgUnitTreeNodeResponse: { orgUnitId, parentOrgUnitId, orgUnitCode, orgUnitName,
 *   orgUnitType, hierarchyLevel, status, managerEmployeeId, managerEmployeeName,
 *   children: OrgUnitTreeNodeResponse[] }
 */
export const getOrgUnitTree = async () => {
    try {
        const response = await axios.get(`${ORG_UNIT_API}/tree`);
        return response.data;
    } catch (error) {
        console.error("getOrgUnitTree failed:", error);
        throw error;
    }
};

/**
 * Lấy chi tiết một đơn vị tổ chức
 *
 * @param {number} orgUnitId - ID của đơn vị tổ chức
 * @returns {Promise<ApiResponse<OrgUnitDetailResponse>>}
 *
 * OrgUnitDetailResponse: { orgUnitId, parentOrgUnitId, parentOrgUnitCode, parentOrgUnitName,
 *   orgUnitCode, orgUnitName, orgUnitType, hierarchyLevel, pathCode, sortOrder, status,
 *   effectiveFrom, effectiveTo, managerEmployeeId, managerEmployeeCode, managerEmployeeName }
 */
export const getOrgUnitDetail = async (orgUnitId) => {
    try {
        const response = await axios.get(`${ORG_UNIT_API}/${orgUnitId}`);
        return response.data;
    } catch (error) {
        console.error("getOrgUnitDetail failed:", error);
        throw error;
    }
};

/**
 * Tạo mới đơn vị tổ chức
 *
 * @param {Object}      payload
 * @param {number|null} [payload.parentOrgUnitId]    - ID đơn vị cha (null = gốc)
 * @param {string}      payload.orgUnitCode           - Mã đơn vị (bắt buộc, ≤30 ký tự)
 * @param {string}      payload.orgUnitName           - Tên đơn vị (bắt buộc, ≤200 ký tự)
 * @param {string}      payload.orgUnitType           - OrgUnitType (bắt buộc): DEPARTMENT | DIVISION | TEAM | ...
 * @param {number|null} [payload.managerEmployeeId]  - ID nhân viên quản lý
 * @param {number}      [payload.sortOrder]           - Thứ tự hiển thị (≥0)
 * @param {string}      payload.effectiveFrom         - Ngày hiệu lực bắt đầu ISO date (bắt buộc)
 * @param {string|null} [payload.effectiveTo]         - Ngày hiệu lực kết thúc ISO date
 * @returns {Promise<ApiResponse<OrgUnitDetailResponse>>}
 */
export const createOrgUnit = async (payload) => {
    try {
        const response = await axios.post(ORG_UNIT_API, payload);
        return response.data;
    } catch (error) {
        console.error("createOrgUnit failed:", error);
        throw error;
    }
};

/**
 * Cập nhật thông tin đơn vị tổ chức
 *
 * @param {number}      orgUnitId - ID của đơn vị tổ chức
 * @param {Object}      payload
 * @param {number|null} [payload.parentOrgUnitId]  - ID đơn vị cha
 * @param {string}      payload.orgUnitCode         - Mã đơn vị (bắt buộc, ≤30 ký tự)
 * @param {string}      payload.orgUnitName         - Tên đơn vị (bắt buộc, ≤200 ký tự)
 * @param {string}      payload.orgUnitType         - OrgUnitType (bắt buộc)
 * @param {number}      [payload.sortOrder]         - Thứ tự hiển thị (≥0)
 * @param {string}      payload.effectiveFrom       - Ngày hiệu lực bắt đầu ISO date (bắt buộc)
 * @param {string|null} [payload.effectiveTo]       - Ngày hiệu lực kết thúc ISO date
 * @returns {Promise<ApiResponse<OrgUnitDetailResponse>>}
 */
export const updateOrgUnit = async (orgUnitId, payload) => {
    try {
        const response = await axios.put(`${ORG_UNIT_API}/${orgUnitId}`, payload);
        return response.data;
    } catch (error) {
        console.error("updateOrgUnit failed:", error);
        throw error;
    }
};

/**
 * Thay đổi trạng thái đơn vị tổ chức (kích hoạt / vô hiệu hoá)
 *
 * @param {number} orgUnitId - ID của đơn vị tổ chức
 * @param {Object} payload
 * @param {string}       payload.status          - RecordStatus (bắt buộc): ACTIVE | INACTIVE
 * @param {string|null}  [payload.effectiveTo]   - Ngày hiệu lực kết thúc ISO date
 * @param {string|null}  [payload.note]          - Ghi chú
 * @returns {Promise<ApiResponse<OrgUnitDetailResponse>>}
 */
export const changeOrgUnitStatus = async (orgUnitId, payload) => {
    try {
        const response = await axios.patch(`${ORG_UNIT_API}/${orgUnitId}/status`, payload);
        return response.data;
    } catch (error) {
        console.error("changeOrgUnitStatus failed:", error);
        throw error;
    }
};

/**
 * Gán / thay đổi quản lý đơn vị tổ chức
 *
 * @param {number} orgUnitId - ID của đơn vị tổ chức
 * @param {Object} payload
 * @param {number|null}  [payload.managerEmployeeId] - ID nhân viên quản lý mới (null = xóa quản lý)
 * @param {string|null}  [payload.note]              - Ghi chú
 * @returns {Promise<ApiResponse<OrgUnitDetailResponse>>}
 */
export const assignOrgUnitManager = async (orgUnitId, payload) => {
    try {
        const response = await axios.patch(`${ORG_UNIT_API}/${orgUnitId}/manager`, payload);
        return response.data;
    } catch (error) {
        console.error("assignOrgUnitManager failed:", error);
        throw error;
    }
};
