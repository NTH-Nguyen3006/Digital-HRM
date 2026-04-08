import axios from 'axios';

const MANAGER_API = '/api/v1/manager';

// ==========================================
// LEAVE (Duyệt phép & Lịch nhóm)
// ==========================================

/**
 * Lấy danh sách các đơn tranh thủ/nghỉ phép từ các thành viên trong đội nhóm chờ trưởng phòng duyệt
 * @param {Object} [params] - Keyword, khoảng thời gian
 * @returns {Promise<Object>}
 */
export const getPendingLeaveRequests = async (params) => {
    const response = await axios.get(`${MANAGER_API}/leave/requests/pending`, { params });
    return response.data;
};

/**
 * Trưởng phòng thực hiện Đồng ý / Yêu cầu giải trình thêm với đơn vị Phép
 * @param {string|number} leaveRequestId 
 * @param {Object} payload - { status: 'APPROVED'|'REJECTED', note: '...' }
 * @returns {Promise<Object>}
 */
export const reviewLeaveRequest = async (leaveRequestId, payload) => {
    const response = await axios.patch(`${MANAGER_API}/leave/requests/${leaveRequestId}/review`, payload);
    return response.data;
};

/**
 * Lấy danh sách tổng hợp trực ban, vắng mặt của phòng ban trong tháng (dạng Gantt Calendar)
 * @param {Object} [params] - Tháng
 * @returns {Promise<Object>}
 */
export const getTeamLeaveCalendar = async (params) => {
    const response = await axios.get(`${MANAGER_API}/leave/calendar`, { params });
    return response.data;
};

// ==========================================
// ONBOARDING (Duyệt tiếp nhận)
// ==========================================

/**
 * Quản lý trực tiếp nhận người, xác nhận rằng nhân sự mới đã tham gia team
 * @param {string|number} onboardingId 
 * @param {Object} payload - { isReady: boolean } 
 * @returns {Promise<Object>}
 */
export const confirmOrientation = async (onboardingId, payload) => {
    const response = await axios.patch(`${MANAGER_API}/onboarding/${onboardingId}/orientation-confirm`, payload);
    return response.data;
};

// ==========================================
// ATTENDANCE (Duyệt điều chỉnh công & tăng ca)
// ==========================================

/**
 * Xem các đơn xin bù công khi chấm công thiếu từ nhân viên cấp dưới
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getPendingAttendanceAdjustments = async (params) => {
    const response = await axios.get(`${MANAGER_API}/attendance/adjustment-requests/pending`, { params });
    return response.data;
};

/**
 * Quản lý chốt đơn xác nhận chấm bù công
 * @param {string|number} adjustmentRequestId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const reviewAttendanceAdjustment = async (adjustmentRequestId, payload) => {
    const response = await axios.patch(`${MANAGER_API}/attendance/adjustment-requests/${adjustmentRequestId}/review`, payload);
    return response.data;
};

/**
 * Quản trị đơn đề xuất OT (Tăng ca) của các nhân viên phòng ban
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getPendingOvertimeRequests = async (params) => {
    const response = await axios.get(`${MANAGER_API}/attendance/overtime-requests/pending`, { params });
    return response.data;
};

/**
 * Phê duyệt đơn OT
 * @param {string|number} overtimeRequestId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const reviewOvertimeRequest = async (overtimeRequestId, payload) => {
    const response = await axios.patch(`${MANAGER_API}/attendance/overtime-requests/${overtimeRequestId}/review`, payload);
    return response.data;
};

// ==========================================
// PAYROLL (Xác nhận bảng công/lương thành viên)
// ==========================================

/**
 * Láy báo cáo KPI Lương để duyệt
 * @param {string|number} payrollPeriodId 
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getTeamPayrollItems = async (payrollPeriodId, params) => {
    const response = await axios.get(`${MANAGER_API}/payroll/periods/${payrollPeriodId}/items`, { params });
    return response.data;
};

/**
 * Xác minh tính đúng đắn cho quỹ điểm thưởng của nhân sự mình quản lý 
 * @param {string|number} payrollItemId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const confirmPayrollItem = async (payrollItemId, payload) => {
    const response = await axios.patch(`${MANAGER_API}/payroll/items/${payrollItemId}/confirm`, payload);
    return response.data;
};

// ==========================================
// OFFBOARDING (Duyệt nghỉ việc)
// ==========================================

/**
 * Nhận thông báo chờ giải quyết đơn từ chức của nhân viên
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getPendingOffboardings = async (params) => {
    const response = await axios.get(`${MANAGER_API}/offboarding/pending`, { params });
    return response.data;
};

/**
 * Xác nhận quy trình rút lui, điều hướng bàn giao công việc
 * @param {string|number} offboardingCaseId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const reviewOffboarding = async (offboardingCaseId, payload) => {
    const response = await axios.patch(`${MANAGER_API}/offboarding/${offboardingCaseId}/review`, payload);
    return response.data;
};

/**
 * Khởi tạo danh sách các đầu việc (Checklist) bắt buộc nhân sự phải hoàn thành bàn giao trước khi rời công ty
 * @param {string|number} offboardingCaseId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const assignChecklistItem = async (offboardingCaseId, payload) => {
    const response = await axios.post(`${MANAGER_API}/offboarding/${offboardingCaseId}/checklist-items`, payload);
    return response.data;
};

/**
 * Cập nhật Trạng thái cho từng công việc Checklist
 * @param {string|number} checklistItemId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const updateChecklistItem = async (checklistItemId, payload) => {
    const response = await axios.put(`${MANAGER_API}/offboarding/checklist-items/${checklistItemId}`, payload);
    return response.data;
};

// ==========================================
// REPORTS (Báo cáo nội bộ team)
// ==========================================

/**
 * Báo cáo hiệu suất chung của Đội nhóm
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getTeamDashboardReport = async (params) => {
    const response = await axios.get(`${MANAGER_API}/reports/dashboard/team`, { params });
    return response.data;
};

/**
 * Lấy danh sách nhân viên thuộc cấp quản lý (dựa trên Org Scope của Manager)
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getTeamMembers = async (params) => {
    // API chung nhưng Backend sẽ scope lại theo Role/Manager
    const response = await axios.get('/api/v1/admin/employees', { params });
    return response.data;
};
