import axios from "axios";

const ADMIN_USER_API = "/api/v1/admin/users";

/**
 * Lấy danh sách user (phân trang, tìm kiếm, lọc)
 *
 * @param {Object} params
 * @param {string}  [params.keyword]   - Tìm theo username / email
 * @param {string}  [params.status]    - UserStatus: ACTIVE | INACTIVE | LOCKED
 * @param {string}  [params.roleCode]  - RoleCode: ADMIN | HR | MANAGER | EMPLOYEE
 * @param {number}  [params.page=0]    - Trang hiện tại (0-indexed)
 * @param {number}  [params.size=20]   - Số bản ghi mỗi trang
 * @returns {Promise<ApiResponse<PageResponse<UserListItemResponse>>>}
 *
 * UserListItemResponse: { userId, employeeId, username, email, phoneNumber,
 *   status, mustChangePassword, primaryRoleCode, primaryRoleName,
 *   lastLoginAt, lastLoginIp }
 */
export const getUsers = async ({
    keyword,
    status,
    roleCode,
    page = 0,
    size = 20,
} = {}) => {
    try {
        const params = { page, size };
        if (keyword) params.keyword = keyword;
        if (status) params.status = status;
        if (roleCode) params.roleCode = roleCode;

        const response = await axios.get(ADMIN_USER_API, { params });
        return response.data;
    } catch (error) {
        console.error("getUsers failed:", error);
        throw error;
    }
};

/**
 * Lấy chi tiết một user theo UUID
 *
 * @param {string} userId - UUID của user
 * @returns {Promise<ApiResponse<UserDetailResponse>>}
 *
 * UserDetailResponse: { userId, employeeId, username, email, phoneNumber,
 *   status, mustChangePassword, mfaEnabled, passwordChangedAt, lastLoginAt,
 *   lastLoginIp, failedLoginCount, lockedUntil, roleHistory[] }
 */
export const getUserDetail = async (userId) => {
    try {
        const response = await axios.get(`${ADMIN_USER_API}/${userId}`);
        return response.data;
    } catch (error) {
        console.error("getUserDetail failed:", error);
        throw error;
    }
};

/**
 * Tạo mới user
 *
 * @param {Object}       payload
 * @param {number|null}  [payload.employeeId]          - ID nhân viên liên kết
 * @param {string}       payload.username               - Tên đăng nhập (bắt buộc, ≤50 ký tự)
 * @param {string}       [payload.email]                - Email (≤150 ký tự)
 * @param {string}       [payload.phoneNumber]          - Số điện thoại (≤20 ký tự)
 * @param {string}       [payload.status]               - UserStatus: ACTIVE | INACTIVE
 * @param {string}       payload.roleCode               - RoleCode (bắt buộc)
 * @param {string}       [payload.initialPassword]      - Mật khẩu ban đầu (≤255 ký tự)
 * @param {boolean}      [payload.sendSetupEmail=false] - Gửi email hướng dẫn đặt mật khẩu
 * @returns {Promise<ApiResponse<UserDetailResponse>>}
 */
export const createUser = async (payload) => {
    try {
        const response = await axios.post(ADMIN_USER_API, payload);
        return response.data;
    } catch (error) {
        console.error("createUser failed:", error);
        throw error;
    }
};

/**
 * Cập nhật thông tin user
 *
 * @param {string}       userId  - UUID của user
 * @param {Object}       payload
 * @param {number|null}  [payload.employeeId]           - ID nhân viên liên kết
 * @param {string}       payload.username               - Tên đăng nhập (bắt buộc, ≤50 ký tự)
 * @param {string}       [payload.email]                - Email (≤150 ký tự)
 * @param {string}       [payload.phoneNumber]          - Số điện thoại (≤20 ký tự)
 * @param {string}       [payload.status]               - UserStatus: ACTIVE | INACTIVE | LOCKED
 * @param {boolean}      [payload.mustChangePassword=false] - Bắt đổi mật khẩu lần sau
 * @returns {Promise<ApiResponse<UserDetailResponse>>}
 */
export const updateUser = async (userId, payload) => {
    try {
        const response = await axios.put(`${ADMIN_USER_API}/${userId}`, payload);
        return response.data;
    } catch (error) {
        console.error("updateUser failed:", error);
        throw error;
    }
};

/**
 * Khóa / mở khóa tài khoản user
 *
 * @param {string}       userId  - UUID của user
 * @param {Object}       payload
 * @param {boolean}      payload.locked          - true = khóa, false = mở khóa
 * @param {string}       payload.reason          - Lý do (bắt buộc, ≤255 ký tự)
 * @param {string|null}  [payload.lockedUntil]   - ISO 8601, null = vô thời hạn
 * @returns {Promise<ApiResponse<UserDetailResponse>>}
 */
export const lockOrUnlockUser = async (userId, payload) => {
    try {
        const response = await axios.patch(`${ADMIN_USER_API}/${userId}/lock-state`, payload);
        return response.data;
    } catch (error) {
        console.error("lockOrUnlockUser failed:", error);
        throw error;
    }
};

/**
 * Gán role chính cho user
 *
 * @param {string} userId  - UUID của user
 * @param {Object} payload
 * @param {string} payload.roleCode - RoleCode mới (bắt buộc)
 * @param {string} payload.reason   - Lý do gán role (bắt buộc, ≤255 ký tự)
 * @returns {Promise<ApiResponse<UserDetailResponse>>}
 */
export const assignPrimaryRole = async (userId, payload) => {
    try {
        const response = await axios.put(`${ADMIN_USER_API}/${userId}/primary-role`, payload);
        return response.data;
    } catch (error) {
        console.error("assignPrimaryRole failed:", error);
        throw error;
    }
};