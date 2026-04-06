import axios from 'axios';

const ATTENDANCE_API = '/api/v1/admin/attendance';

/**
 * Lấy danh sách các ca làm việc
 * @param {Object} [params] - Thông tin phân trang (page, size, keyword)
 * @returns {Promise<Object>}
 */
export const getShifts = async (params) => {
    const response = await axios.get(`${ATTENDANCE_API}/shifts`, { params });
    return response.data;
};

/**
 * Lấy chi tiết mẫu một ca làm việc
 * @param {string|number} shiftId - ID ca làm việc
 * @returns {Promise<Object>}
 */
export const getShiftDetail = async (shiftId) => {
    const response = await axios.get(`${ATTENDANCE_API}/shifts/${shiftId}`);
    return response.data;
};

/**
 * Tạo mới mẫu ca làm việc (Shift)
 * @param {Object} payload - Dữ liệu (startTime, endTime, breakTime...)
 * @returns {Promise<Object>}
 */
export const createShift = async (payload) => {
    const response = await axios.post(`${ATTENDANCE_API}/shifts`, payload);
    return response.data;
};

/**
 * Chỉnh sửa ca làm việc
 * @param {string|number} shiftId - ID ca làm việc
 * @param {Object} payload - Dữ liệu cập nhật
 * @returns {Promise<Object>}
 */
export const updateShift = async (shiftId, payload) => {
    const response = await axios.put(`${ATTENDANCE_API}/shifts/${shiftId}`, payload);
    return response.data;
};

/**
 * Lấy bảng xếp ca của tổ chức
 * @param {Object} [params] - Tham số lọc (dateFrom, dateTo, orgUnitId...)
 * @returns {Promise<Object>}
 */
export const getShiftAssignments = async (params) => {
    const response = await axios.get(`${ATTENDANCE_API}/shift-assignments`, { params });
    return response.data;
};

/**
 * Lên lịch/gán ca làm việc cho nhân viên
 * @param {Object} payload - Mảng các lịch phân ca
 * @returns {Promise<Object>}
 */
export const assignShifts = async (payload) => {
    const response = await axios.post(`${ATTENDANCE_API}/shift-assignments`, payload);
    return response.data;
};


/**
 * Lấy nhật ký chấm công (ra/vào) hàng ngày của tổng công ty
 * @param {Object} [params] - Query Filter (date)
 * @returns {Promise<Object>}
 */
export const getDailyAttendance = async (params) => {
    const response = await axios.get(`${ATTENDANCE_API}/daily`, { params });
    return response.data;
};

/**
 * Lấy chi tiết lịch sử Checkin/Checkout của nhân sự trong 1 ngày
 * @param {Object} [params] - params truy vấn chi tiết (employeeId, date)
 * @returns {Promise<Object>}
 */
export const getDailyAttendanceDetail = async (params) => {
    const response = await axios.get(`${ATTENDANCE_API}/daily/detail`, { params });
    return response.data;
};

/**
 * Lấy danh sách các đơn yêu cầu điều chỉnh giờ công (Quên checkin/checkout)
 * @param {Object} [params] - Status, employeeId...
 * @returns {Promise<Object>}
 */
export const getAdjustmentRequests = async (params) => {
    const response = await axios.get(`${ATTENDANCE_API}/adjustment-requests`, { params });
    return response.data;
};

/**
 * @param {string|number} adjustmentRequestId - ID yêu cầu 
 * @returns {Promise<Object>}
 */
export const getAdjustmentRequestDetail = async (adjustmentRequestId) => {
    const response = await axios.get(`${ATTENDANCE_API}/adjustment-requests/${adjustmentRequestId}`);
    return response.data;
};

/**
 * Admin HR chốt thực hiện điều chỉnh giờ công 
 * @param {string|number} adjustmentRequestId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const finalizeAdjustmentRequest = async (adjustmentRequestId, payload) => {
    const response = await axios.patch(`${ATTENDANCE_API}/adjustment-requests/${adjustmentRequestId}/finalize`, payload);
    return response.data;
};

// ==========================================
// OVERTIME
// ==========================================

/**
 * Lấy danh sách đơn đề nghị Tăng ca (OT) toàn tổ chức
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getOvertimeRequests = async (params) => {
    const response = await axios.get(`${ATTENDANCE_API}/overtime-requests`, { params });
    return response.data;
};

// ==========================================
// ATTENDANCE PERIODS
// ==========================================

/**
 * Lấy danh sách các chu kỳ chốt công (Các tháng)
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getAttendancePeriods = async (params) => {
    const response = await axios.get(`${ATTENDANCE_API}/periods`, { params });
    return response.data;
};

/**
 * Khóa (đóng) sổ chấm công của 1 chu kỳ để tính lương
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const closeAttendancePeriod = async (payload) => {
    const response = await axios.post(`${ATTENDANCE_API}/periods/close`, payload);
    return response.data;
};

/**
 * Mở lại sổ chấm công nếu có sai sót
 * @param {string|number} attendancePeriodId - Kỳ chốt công
 * @param {Object} payload - Lý do mở lại
 * @returns {Promise<Object>}
 */
export const reopenAttendancePeriod = async (attendancePeriodId, payload) => {
    const response = await axios.patch(`${ATTENDANCE_API}/periods/${attendancePeriodId}/reopen`, payload);
    return response.data;
};

/**
 * Xuất file báo cáo các trường hợp bất thường (Thiếu công, đi trễ, về sớm)
 * @param {Object} [params] 
 * @returns {Promise<Blob>}
 */
export const exportAnomaliesReport = async (params) => {
    const response = await axios.get(`${ATTENDANCE_API}/reports/anomalies/export`, { params, responseType: 'blob' });
    return response.data;
};
