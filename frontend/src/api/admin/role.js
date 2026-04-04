import axios from "axios";

const ROLE_API = "/api/v1/admin/roles";

/**
 * Lấy toàn bộ danh sách role (không phân trang)
 *
 * @returns {Promise<ApiResponse<RoleListItemResponse[]>>}
 *
 * RoleListItemResponse: { roleId, roleCode, roleName, description,
 *   status, systemRole, sortOrder, activeUserCount }
 */
export const getRoles = async () => {
    try {
        const response = await axios.get(ROLE_API);
        return response.data;
    } catch (error) {
        console.error("getRoles failed:", error);
        throw error;
    }
};

/**
 * Lấy chi tiết một role theo UUID
 *
 * @param {string} roleId - UUID của role
 * @returns {Promise<ApiResponse<RoleDetailResponse>>}
 *
 * RoleDetailResponse: { roleId, roleCode, roleName, description, status,
 *   systemRole, sortOrder, activeUserCount,
 *   permissions: PermissionSummaryResponse[],
 *   dataScopes: DataScopeResponse[] }
 *
 * PermissionSummaryResponse: { permissionCode, moduleCode, actionCode, permissionName, allowed }
 * DataScopeResponse: { dataScopeAssignmentId, subjectType, scopeCode, targetType,
 *   targetRefId, inclusive, priorityOrder, status, effectiveFrom, effectiveTo }
 */
export const getRoleDetail = async (roleId) => {
    try {
        const response = await axios.get(`${ROLE_API}/${roleId}`);
        return response.data;
    } catch (error) {
        console.error("getRoleDetail failed:", error);
        throw error;
    }
};

/**
 * Tạo mới role
 *
 * @param {Object}   payload
 * @param {string}   payload.roleCode           - Mã role (bắt buộc, ≤30 ký tự)
 * @param {string}   payload.roleName           - Tên role (bắt buộc, ≤100 ký tự)
 * @param {string}   [payload.description]      - Mô tả (≤500 ký tự)
 * @param {number}   [payload.sortOrder]        - Thứ tự hiển thị
 * @param {string}   [payload.status]           - RecordStatus: ACTIVE | INACTIVE
 * @param {string[]} payload.permissionCodes    - Danh sách mã quyền (bắt buộc, không được rỗng)
 * @param {DataScopeRequest[]} [payload.dataScopes] - Danh sách data scope
 *
 * DataScopeRequest: { scopeCode (bắt buộc), targetType, targetRefId,
 *   inclusive, priorityOrder, effectiveFrom, effectiveTo, status }
 *
 * @returns {Promise<ApiResponse<RoleDetailResponse>>}
 */
export const createRole = async (payload) => {
    try {
        const response = await axios.post(ROLE_API, payload);
        return response.data;
    } catch (error) {
        console.error("createRole failed:", error);
        throw error;
    }
};

/**
 * Cập nhật role (không thể đổi roleCode)
 *
 * @param {string}   roleId  - UUID của role
 * @param {Object}   payload
 * @param {string}   payload.roleName           - Tên role (bắt buộc, ≤100 ký tự)
 * @param {string}   [payload.description]      - Mô tả (≤500 ký tự)
 * @param {number}   [payload.sortOrder]        - Thứ tự hiển thị
 * @param {string}   [payload.status]           - RecordStatus: ACTIVE | INACTIVE
 * @param {string[]} [payload.permissionCodes]  - Danh sách mã quyền mới (ghi đè toàn bộ)
 * @param {DataScopeRequest[]} [payload.dataScopes] - Danh sách data scope mới (ghi đè toàn bộ)
 * @returns {Promise<ApiResponse<RoleDetailResponse>>}
 */
export const updateRole = async (roleId, payload) => {
    try {
        const response = await axios.put(`${ROLE_API}/${roleId}`, payload);
        return response.data;
    } catch (error) {
        console.error("updateRole failed:", error);
        throw error;
    }
};
