import axios from 'axios';

const ADMIN_LEAVE_API = '/api/v1/admin/leave';
const LEAVE_TYPE_API = '/api/v1/admin/leave-types';

// ==========================================
// LEAVE TYPE
// ==========================================

/**
 * Lấy danh sách loại hình nghỉ phép
 * @param {Object} [params] - Tham số truy vấn (keyword, status)
 * @returns {Promise<Object>}
 */
export const getLeaveTypes = async (params) => {
    const response = await axios.get(LEAVE_TYPE_API, { params });
    return response.data;
};

/**
 * Xem chi tiết loại hình nghỉ phép
 * @param {string|number} leaveTypeId - ID của loại hình nghỉ phép
 * @returns {Promise<Object>}
 */
export const getLeaveTypeDetail = async (leaveTypeId) => {
    const response = await axios.get(`${LEAVE_TYPE_API}/${leaveTypeId}`);
    return response.data;
};

/**
 * Tạo mới cấu hình một loại hình nghỉ phép
 * @param {Object} payload - Dữ liệu (tên, có hưởng lương, max ngày)
 * @returns {Promise<Object>}
 */
export const createLeaveType = async (payload) => {
    const response = await axios.post(LEAVE_TYPE_API, payload);
    return response.data;
};

/**
 * Cập nhật cấu hình loại hình nghỉ phép
 * @param {string|number} leaveTypeId - ID của loại hình nghỉ phép
 * @param {Object} payload - Dữ liệu thay đổi
 * @returns {Promise<Object>}
 */
export const updateLeaveType = async (leaveTypeId, payload) => {
    const response = await axios.put(`${LEAVE_TYPE_API}/${leaveTypeId}`, payload);
    return response.data;
};

/**
 * Vô hiệu hóa một loại hình nghỉ phép
 * @param {string|number} leaveTypeId - ID của loại hình nghỉ phép
 * @returns {Promise<Object>}
 */
export const deactivateLeaveType = async (leaveTypeId) => {
    const response = await axios.patch(`${LEAVE_TYPE_API}/${leaveTypeId}/deactivate`);
    return response.data;
};

// ==========================================
// LEAVE BALANCE
// ==========================================

/**
 * Lấy danh sách quỹ ngày phép của các nhân sự
 * @param {Object} [params] - Params lọc (employeeId, year, leaveTypeId)
 * @returns {Promise<Object>}
 */
export const getLeaveBalances = async (params) => {
    const response = await axios.get(`${ADMIN_LEAVE_API}/balances`, { params });
    return response.data;
};

/**
 * Lấy chi tiết quỹ phép cá nhân
 * @param {string|number} leaveBalanceId - ID của quỹ phép
 * @returns {Promise<Object>}
 */
export const getLeaveBalanceDetail = async (leaveBalanceId) => {
    const response = await axios.get(`${ADMIN_LEAVE_API}/balances/${leaveBalanceId}`);
    return response.data;
};

/**
 * Thực hiện cộng/trừ ngày phép thủ công
 * @param {Object} payload - { leaveBalanceId, daysToAdjust, reason }
 * @returns {Promise<Object>}
 */
export const adjustLeaveBalance = async (payload) => {
    const response = await axios.patch(`${ADMIN_LEAVE_API}/balances/adjust`, payload);
    return response.data;
};

// ==========================================
// LEAVE REQUEST (HR WORKFLOW)
// ==========================================

/**
 * Lấy danh sách tất cả các đơn xin nghỉ phép trên hệ thống
 * @param {Object} [params] - Tham số lọc (employeeId, status, targetDate)
 * @returns {Promise<Object>}
 */
export const getLeaveRequests = async (params) => {
    const response = await axios.get(`${ADMIN_LEAVE_API}/requests`, { params });
    return response.data;
};

/**
 * Lấy thông tin chi tiết một đơn xin nghỉ phép cụ thể
 * @param {string|number} leaveRequestId - ID đơn xin nghỉ phép
 * @returns {Promise<Object>}
 */
export const getLeaveRequestDetail = async (leaveRequestId) => {
    const response = await axios.get(`${ADMIN_LEAVE_API}/requests/${leaveRequestId}`);
    return response.data;
};

/**
 * Quản trị viên Nhân sự thực hiện duyệt chốt đơn phép sau khi Quản lý trực tiếp duyệt
 * @param {string|number} leaveRequestId - ID đơn xin nghỉ phép
 * @param {Object} payload - { finalStatus: 'APPROVED' | 'REJECTED', hrNote: '...' }
 * @returns {Promise<Object>}
 */
export const finalizeLeaveRequest = async (leaveRequestId, payload) => {
    const response = await axios.patch(`${ADMIN_LEAVE_API}/requests/${leaveRequestId}/finalize`, payload);
    return response.data;
};

/**
 * Chốt kỳ phép (Ví dụ vào dịp cuối năm để tính chuyển ngày phép dư)
 * @param {Object} payload - Dữ liệu kỳ chốt (Năm, tháng)
 * @returns {Promise<Object>}
 */
export const closeLeavePeriod = async (payload) => {
    const response = await axios.post(`${ADMIN_LEAVE_API}/period-close`, payload);
    return response.data;
};

/**
 * Tính toán thanh toán ngày phép dư (quy đổi ra tiền lương)
 * @param {Object} payload - Thông tin cấu hình chi trả
 * @returns {Promise<Object>}
 */
export const calculateLeaveSettlement = async (payload) => {
    const response = await axios.post(`${ADMIN_LEAVE_API}/settlement`, payload);
    return response.data;
};

/**
 * Export báo cáo danh sách ngày phép
 * @param {Object} [params] - Lọc điều kiện
 * @returns {Promise<Blob>} Dữ liệu Excel
 */
export const exportLeaveReports = async (params) => {
    const response = await axios.get(`${ADMIN_LEAVE_API}/reports/export`, { params, responseType: 'blob' });
    return response.data;
};
