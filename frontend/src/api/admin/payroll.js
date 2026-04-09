import axios from 'axios';

const PAYROLL_API = '/api/v1/admin/payroll';

/**
 * Lấy danh sách các cấu phần lương (Phụ cấp, Thưởng, Cố định...)
 * @param {Object} [params] - Tham số lọc dữ liệu
 * @returns {Promise<Object>}
 */
export const getSalaryComponents = async (params) => {
    const response = await axios.get(`${PAYROLL_API}/components`, { params });
    return response.data;
};

/**
 * Xem chi tiết một cấu phần lương
 * @param {string|number} salaryComponentId - ID của cấu phần lương
 * @returns {Promise<Object>}
 */
export const getSalaryComponentDetail = async (salaryComponentId) => {
    const response = await axios.get(`${PAYROLL_API}/components/${salaryComponentId}`);
    return response.data;
};

/**
 * Tạo mới cấu phần lương
 * @param {Object} payload - Dữ liệu định nghĩa cấu phần
 * @returns {Promise<Object>}
 */
export const createSalaryComponent = async (payload) => {
    const response = await axios.post(`${PAYROLL_API}/components`, payload);
    return response.data;
};

/**
 * Sửa thông tin cấu phần lương
 * @param {string|number} salaryComponentId - ID của cấu phần lương
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const updateSalaryComponent = async (salaryComponentId, payload) => {
    const response = await axios.put(`${PAYROLL_API}/components/${salaryComponentId}`, payload);
    return response.data;
};

/**
 * Lấy danh sách các công thức lương đang ban hành
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getPayrollFormulas = async (params) => {
    const response = await axios.get(`${PAYROLL_API}/formulas`, { params });
    return response.data;
};

/**
 * Đọc chi tiết phương thức tính của 1 phiên bản công thức
 * @param {string|number} formulaVersionId - ID phiên bản
 * @returns {Promise<Object>}
 */
export const getPayrollFormulaDetail = async (formulaVersionId) => {
    const response = await axios.get(`${PAYROLL_API}/formulas/${formulaVersionId}`);
    return response.data;
};

/**
 * Ban hành phiên bản công thức tính lương mới
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const createPayrollFormula = async (payload) => {
    const response = await axios.post(`${PAYROLL_API}/formulas`, payload);
    return response.data;
};

/**
 * Chỉnh sửa công thức lương
 * @param {string|number} formulaVersionId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const updatePayrollFormula = async (formulaVersionId, payload) => {
    const response = await axios.put(`${PAYROLL_API}/formulas/${formulaVersionId}`, payload);
    return response.data;
};

/**
 * Lấy danh sách chế độ đãi ngộ (Lương gốc + phụ cấp cố định) của mọi nhân viên
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getCompensations = async (params) => {
    const response = await axios.get(`${PAYROLL_API}/compensations`, { params });
    return response.data;
};

/**
 * Xem chế độ đãi ngộ đang áp dụng của riêng 1 người
 * @param {string|number} employeeId - Mã hệ thống của nhân viên
 * @returns {Promise<Object>}
 */
export const getEmployeeCompensation = async (employeeId) => {
    const response = await axios.get(`${PAYROLL_API}/compensations/${employeeId}`);
    return response.data;
};

/**
 * Cập nhật cấu trúc lương cơ bản của nhân viên
 * @param {string|number} employeeId - ID nhân viên
 * @param {Object} payload - Dữ liệu chính sách lương
 * @returns {Promise<Object>}
 */
export const updateEmployeeCompensation = async (employeeId, payload) => {
    const response = await axios.put(`${PAYROLL_API}/compensations/${employeeId}`, payload);
    return response.data;
};

/**
 * Láy hồ sơ thuế vụ (MST cá nhân, Thông tin giảm trừ gia cảnh)
 * @param {string|number} employeeId - ID nhân viên
 * @returns {Promise<Object>}
 */
export const getTaxProfile = async (employeeId) => {
    const response = await axios.get(`${PAYROLL_API}/tax-profiles/${employeeId}`);
    return response.data;
};

/**
 * Cập nhật MST và hồ sơ Thuế
 * @param {string|number} employeeId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const updateTaxProfile = async (employeeId, payload) => {
    const response = await axios.put(`${PAYROLL_API}/tax-profiles/${employeeId}`, payload);
    return response.data;
};

/**
 * Nhập thêm người phụ thuộc để tính giảm trừ Thuế
 * @param {string|number} employeeId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const addTaxDependent = async (employeeId, payload) => {
    const response = await axios.post(`${PAYROLL_API}/tax-profiles/${employeeId}/dependents`, payload);
    return response.data;
};

/**
 * Chỉnh sửa người phụ thuộc Thuế
 * @param {string|number} taxDependentId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const updateTaxDependent = async (taxDependentId, payload) => {
    const response = await axios.put(`${PAYROLL_API}/dependents/${taxDependentId}`, payload);
    return response.data;
};

/**
 * Xóa/ Ngưng hiệu lực người phụ thuộc
 * @param {string|number} taxDependentId 
 * @returns {Promise<Object>}
 */
export const deactivateTaxDependent = async (taxDependentId) => {
    const response = await axios.patch(`${PAYROLL_API}/dependents/${taxDependentId}/deactivate`);
    return response.data;
};

/**
 * Lấy danh sách các đợt phát lương
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getPayrollPeriods = async (params) => {
    const response = await axios.get(`${PAYROLL_API}/periods`, { params });
    return response.data;
};

/**
 * Tạo một kỳ phát lương (Tháng) mới
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const createPayrollPeriod = async (payload) => {
    const response = await axios.post(`${PAYROLL_API}/periods`, payload);
    return response.data;
};

/**
 * Bấm nút chạy tính toán ra Bảng Lương nháp tổng cho toàn bộ công ty
 * @param {string|number} payrollPeriodId - Mã kỳ lương
 * @returns {Promise<Object>}
 */
export const generatePayrollDraft = async (payrollPeriodId, payload = { regenerate: true, note: 'Tạo lại bảng lương nháp từ HR workspace.' }) => {
    const response = await axios.post(`${PAYROLL_API}/periods/${payrollPeriodId}/generate-draft`, payload);
    return response.data;
};

/**
 * Lấy danh sách phiếu lương thành viên trong 1 đợt thanh toán
 * @param {string|number} payrollPeriodId 
 * @param {Object} [params] 
 * @returns {Promise<Object>}
 */
export const getPayrollItems = async (payrollPeriodId, params) => {
    const response = await axios.get(`${PAYROLL_API}/periods/${payrollPeriodId}/items`, { params });
    return response.data;
};

/**
 * Xem chi tiết kết cấu thu nhập một cá nhân nhận được (Payslip)
 * @param {string|number} payrollItemId 
 * @returns {Promise<Object>}
 */
export const getPayrollItemDetail = async (payrollItemId) => {
    const response = await axios.get(`${PAYROLL_API}/items/${payrollItemId}`);
    return response.data;
};

/**
 * Điều chỉnh cộng trừ tiền thủ công (thưởng phạt đột xuất ngoài công thức)
 * @param {string|number} payrollItemId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const adjustPayrollItem = async (payrollItemId, payload) => {
    const response = await axios.patch(`${PAYROLL_API}/items/${payrollItemId}/adjust`, payload);
    return response.data;
};

/**
 * GĐ/ Kế toán Trưởng Duyệt bảng lương toàn cục
 * @param {string|number} payrollPeriodId 
 * @returns {Promise<Object>}
 */
export const approvePayrollPeriod = async (payrollPeriodId, payload = { note: 'Phê duyệt kỳ lương từ HR workspace.' }) => {
    const response = await axios.patch(`${PAYROLL_API}/periods/${payrollPeriodId}/approve`, payload);
    return response.data;
};

/**
 * Xuất bản phiếu lương trực tuyến cho toàn bộ nhân sự (Gửi Email/Báo app)
 * @param {string|number} payrollPeriodId 
 * @returns {Promise<Object>}
 */
export const publishPayrollPeriod = async (payrollPeriodId, payload = { note: 'Phát hành phiếu lương từ HR workspace.' }) => {
    const response = await axios.patch(`${PAYROLL_API}/periods/${payrollPeriodId}/publish`, payload);
    return response.data;
};


/**
 * Tải file Text/Excel đồng bộ UNC chuyển khoản ngân hàng tự động (Bank Transfer API)
 * @param {Object} [params] - Kỳ lương
 * @returns {Promise<Blob>}
 */
export const exportBankTransferReport = async (params) => {
    const response = await axios.get(`${PAYROLL_API}/reports/bank-transfer/export`, { params, responseType: 'blob' });
    return response.data;
};

/**
 * Tải file tổng hợp kê khai Quyết toán Thuế TNCN
 * @param {Object} [params] - Năm/Kỳ
 * @returns {Promise<Blob>}
 */
export const exportPitReport = async (params) => {
    const response = await axios.get(`${PAYROLL_API}/reports/pit/export`, { params, responseType: 'blob' });
    return response.data;
};
