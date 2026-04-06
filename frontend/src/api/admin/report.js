import axios from 'axios';

const REPORT_API = '/api/v1/admin/reports';

// ==========================================
// DASHBOARD & HEALTH
// ==========================================

/**
 * Thống kê biến động nhân sự để vẽ biểu đồ tổng quan
 * @param {Object} [params] - Năm, kỳ
 * @returns {Promise<Object>}
 */
export const getHeadcountDashboard = async (params) => {
    const response = await axios.get(`${REPORT_API}/dashboard/headcount`, { params });
    return response.data;
};

/**
 * Trạng thái cấp phép, server health
 * @returns {Promise<Object>}
 */
export const getSystemHealth = async () => {
    const response = await axios.get(`${REPORT_API}/system-health`);
    return response.data;
};

// ==========================================
// EXPORTS
// ==========================================
const getExportConfig = () => ({ responseType: 'blob' });

/**
 * Xuất excel lịch sử điều chuyển, thăng tiến phòng ban
 * @param {Object} [params] 
 * @returns {Promise<Blob>} Mảng Byte file Excel
 */
export const exportOrgMovementReport = async (params) => {
    const response = await axios.get(`${REPORT_API}/org-movement/export`, { params, ...getExportConfig() });
    return response.data;
};

/**
 * Xuất danh sách nhân sự sắp hết hạn hợp đồng
 * @param {Object} [params] 
 * @returns {Promise<Blob>}
 */
export const exportContractExpiryReport = async (params) => {
    const response = await axios.get(`${REPORT_API}/contracts/expiry/export`, { params, ...getExportConfig() });
    return response.data;
};

/**
 * Tải file kết xuất tồn phép nhân viên
 * @param {Object} [params] 
 * @returns {Promise<Blob>}
 */
export const exportLeaveBalancesReport = async (params) => {
    const response = await axios.get(`${REPORT_API}/leave-balances/export`, { params, ...getExportConfig() });
    return response.data;
};

/**
 * Tải file phân tích các ca quẹt thẻ bất thường / danh sách Tăng ca
 * @param {Object} [params] 
 * @returns {Promise<Blob>}
 */
export const exportAttendanceAnomalyReport = async (params) => {
    const response = await axios.get(`${REPORT_API}/attendance/anomaly-ot/export`, { params, ...getExportConfig() });
    return response.data;
};

/**
 * File tổng kết Quỹ lương
 * @param {Object} [params] 
 * @returns {Promise<Blob>}
 */
export const exportPayrollSummaryReport = async (params) => {
    const response = await axios.get(`${REPORT_API}/payroll/summary/export`, { params, ...getExportConfig() });
    return response.data;
};

/**
 * File tổng kết Thuế thu nhập cá nhân trong kỳ
 * @param {Object} [params] 
 * @returns {Promise<Blob>}
 */
export const exportPayrollPitReport = async (params) => {
    const response = await axios.get(`${REPORT_API}/payroll/pit/export`, { params, ...getExportConfig() });
    return response.data;
};

/**
 * Export dữ liệu đánh giá Ứng viên gia nhập và Nhân sự rời đi
 * @param {Object} [params] 
 * @returns {Promise<Blob>}
 */
export const exportOnboardingOffboardingReport = async (params) => {
    const response = await axios.get(`${REPORT_API}/onboarding-offboarding/export`, { params, ...getExportConfig() });
    return response.data;
};

/**
 * Export nhật ký hệ thống (Audit log) để thanh tra
 * @param {Object} [params] 
 * @returns {Promise<Blob>}
 */
export const exportAuditReport = async (params) => {
    const response = await axios.get(`${REPORT_API}/audit/export`, { params, ...getExportConfig() });
    return response.data;
};

// ==========================================
// REPORT SCHEDULES (Lên lịch báo cáo gửi email tự động)
// ==========================================

/**
 * Xem lịch trình máy chủ tự động gửi các báo cáo cho Ban Lãnh Đạo
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getReportSchedules = async (params) => {
    const response = await axios.get(`${REPORT_API}/schedules`, { params });
    return response.data;
};

/**
 * Khởi tạo một cài đặt lịch Gửi Email báo cáo
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const createReportSchedule = async (payload) => {
    const response = await axios.post(`${REPORT_API}/schedules`, payload);
    return response.data;
};

/**
 * Sửa tham số gửi tự động (Tần suất, Danh sách email nhận..)
 * @param {string|number} scheduleId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const updateReportSchedule = async (scheduleId, payload) => {
    const response = await axios.put(`${REPORT_API}/schedules/${scheduleId}`, payload);
    return response.data;
};

/**
 * Xem lại lịch sử các lần Cron-Job đã gửi mail trong quá khứ 
 * @param {string|number} scheduleId 
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getReportScheduleRuns = async (scheduleId, params) => {
    const response = await axios.get(`${REPORT_API}/schedules/${scheduleId}/runs`, { params });
    return response.data;
};

/**
 * Bỏ qua bộ đếm tự động, ép hệ thống tổng hợp và gửi mail Báo cáo ngay lập tức
 * @param {string|number} scheduleId 
 * @returns {Promise<Object>}
 */
export const runReportScheduleNow = async (scheduleId) => {
    const response = await axios.post(`${REPORT_API}/schedules/${scheduleId}/run-now`);
    return response.data;
};
