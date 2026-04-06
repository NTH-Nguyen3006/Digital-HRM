import axios from 'axios';

const PORTAL_API = '/api/v1/me/portal';

/**
 * Tổng hợp thông tin khái quát cá nhân cho trang Home của Nhân viên
 * @returns {Promise<Object>}
 */
export const getPortalDashboard = async () => {
    const response = await axios.get(`${PORTAL_API}/dashboard`);
    return response.data;
};

/**
 * Lấy hồ sơ lý lịch chuyên môn, thông tin cơ bản của chính tài khoản đăng nhập
 * @returns {Promise<Object>}
 */
export const getPortalProfile = async () => {
    const response = await axios.get(`${PORTAL_API}/profile`);
    return response.data;
};

// --- Leave (Nghỉ phép)

/**
 * Tra cứu quỹ phép còn lại và lịch sử dùng phép của chính mình
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getMyLeaves = async (params) => {
    const response = await axios.get(`${PORTAL_API}/leave`, { params });
    return response.data;
};

/**
 * Submit đơn xin nghỉ phép lên Quản lý
 * @param {Object} payload - Khoảng thời gian muốn xin nghỉ, Lý do...
 * @returns {Promise<Object>}
 */
export const submitLeaveRequest = async (payload) => {
    const response = await axios.post(`${PORTAL_API}/leave-requests`, payload);
    return response.data;
};

// --- Attendance (Chấm công)

/**
 * Xem lịch sử quẹt vân tay / Checkin App của mình hàng ngày
 * @param {Object} [params] - Kỳ chấm công
 * @returns {Promise<Object>}
 */
export const getMyAttendance = async (params) => {
    const response = await axios.get(`${PORTAL_API}/attendance`, { params });
    return response.data;
};

/**
 * Submit đơn "Quên quẹt thẻ" / Giải trình bất thường
 * @param {Object} payload - Giờ đúng, lý do
 * @returns {Promise<Object>}
 */
export const submitAttendanceAdjustment = async (payload) => {
    const response = await axios.post(`${PORTAL_API}/attendance-adjustment-requests`, payload);
    return response.data;
};

// --- Contract (Hợp đồng)

/**
 * Liệt kê danh sách Hợp đồng do mình đã và đang ký với công ty
 * @returns {Promise<Object>}
 */
export const getMyContracts = async () => {
    const response = await axios.get(`${PORTAL_API}/contracts`);
    return response.data;
};

/**
 * Tải file Scan định dạng PDF của Hợp đồng cá nhân đem làm chứng từ
 * @param {string|number} laborContractId 
 * @returns {Promise<Blob>} Dữ liệu nhị phân PDF
 */
export const downloadMyContract = async (laborContractId) => {
    const response = await axios.get(`${PORTAL_API}/contracts/${laborContractId}/export`, { responseType: 'blob' });
    return response.data;
};

// --- Payroll (Lương)

/**
 * Tra cứu kho Lịch sử trả lương (Payslip) qua các tháng
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getMyPayrollHistory = async (params) => {
    const response = await axios.get(`${PORTAL_API}/payroll`, { params });
    return response.data;
};

/**
 * Xem phiếu chi tiết (giải cấu trúc thu nhập) của một tháng
 * @param {string|number} payrollPeriodId - Tháng (kỳ lương)
 * @returns {Promise<Object>}
 */
export const getMyPayslip = async (payrollPeriodId) => {
    const response = await axios.get(`${PORTAL_API}/payroll/${payrollPeriodId}`);
    return response.data;
};

// --- Inbox (Thông báo cá nhân)

/**
 * Mở Khay tin nhắn báo cáo từ hệ thống cho nhân viên
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getMyInbox = async (params) => {
    const response = await axios.get(`${PORTAL_API}/inbox`, { params });
    return response.data;
};

/**
 * Đọc (Chuyển tín hiệu unread sang read) cho một chuỗi thư
 * @param {string|number} portalInboxItemId 
 * @returns {Promise<Object>}
 */
export const markInboxAsRead = async (portalInboxItemId) => {
    const response = await axios.patch(`${PORTAL_API}/inbox/${portalInboxItemId}/read`);
    return response.data;
};
