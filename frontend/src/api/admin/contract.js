import axios from 'axios';

const CONTRACT_TYPE_API = '/api/v1/admin/contract-types';
const CONTRACT_API = '/api/v1/admin/contracts';

// ==========================================
// CONTRACT TYPE
// ==========================================

/**
 * Lấy danh sách loại hợp đồng (phân trang)
 * @param {Object} [params] - Params lọc (page, size, keyword, status)
 * @returns {Promise<Object>} Danh sách loại hợp đồng
 */
export const getContractTypes = async (params) => {
    const response = await axios.get(CONTRACT_TYPE_API, { params });
    return response.data;
};

/**
 * Lấy danh sách tuỳ chọn loại hợp đồng cho Dropdown (không phân trang)
 * @returns {Promise<Object>} Danh sách options
 */
export const getContractTypeOptions = async () => {
    const response = await axios.get(`${CONTRACT_TYPE_API}/options`);
    return response.data;
};

/**
 * Lấy chi tiết loại hợp đồng theo ID
 * @param {string|number} contractTypeId - ID của loại hợp đồng
 * @returns {Promise<Object>} Chi tiết loại hợp đồng
 */
export const getContractTypeDetail = async (contractTypeId) => {
    const response = await axios.get(`${CONTRACT_TYPE_API}/${contractTypeId}`);
    return response.data;
};

/**
 * Tạo mới loại hợp đồng
 * @param {Object} payload - Dữ liệu tạo mới (name, code, maxDuration...)
 * @returns {Promise<Object>} Kết quả tạo mới
 */
export const createContractType = async (payload) => {
    const response = await axios.post(CONTRACT_TYPE_API, payload);
    return response.data;
};

/**
 * Cập nhật loại hợp đồng
 * @param {string|number} contractTypeId - ID loại hợp đồng
 * @param {Object} payload - Dữ liệu cập nhật
 * @returns {Promise<Object>} Kết quả cập nhật
 */
export const updateContractType = async (contractTypeId, payload) => {
    const response = await axios.put(`${CONTRACT_TYPE_API}/${contractTypeId}`, payload);
    return response.data;
};

/**
 * Thay đổi trạng thái (Active/Inactive) của loại hợp đồng
 * @param {string|number} contractTypeId - ID loại hợp đồng
 * @param {Object} payload - { status: 'ACTIVE' | 'INACTIVE' }
 * @returns {Promise<Object>}
 */
export const changeContractTypeStatus = async (contractTypeId, payload) => {
    const response = await axios.patch(`${CONTRACT_TYPE_API}/${contractTypeId}/status`, payload);
    return response.data;
};

// ==========================================
// LABOR CONTRACT
// ==========================================

/**
 * Lấy danh sách hợp đồng (phân trang, theo bộ lọc)
 * @param {Object} [params] - Params lọc (employeeId, contractTypeId, status...)
 * @returns {Promise<Object>} Danh sách hợp đồng
 */
export const getContracts = async (params) => {
    const response = await axios.get(CONTRACT_API, { params });
    return response.data;
};

/**
 * Lấy danh sách hợp đồng sắp hết hạn
 * @param {number} [days=30] - Số ngày báo trước (mặc định 30)
 * @returns {Promise<Object>}
 */
export const getExpiringContracts = async (days = 30) => {
    const response = await axios.get(`${CONTRACT_API}/expiring`, { params: { days } });
    return response.data;
};

/**
 * Lấy chi tiết hợp đồng theo ID
 * @param {string|number} laborContractId - ID của hợp đồng
 * @returns {Promise<Object>}
 */
export const getContractDetail = async (laborContractId) => {
    const response = await axios.get(`${CONTRACT_API}/${laborContractId}`);
    return response.data;
};

/**
 * Lấy lịch sử chỉnh sửa của một hợp đồng
 * @param {string|number} laborContractId - ID của hợp đồng
 * @returns {Promise<Object>}
 */
export const getContractHistory = async (laborContractId) => {
    const response = await axios.get(`${CONTRACT_API}/${laborContractId}/history`);
    return response.data;
};

/**
 * Tạo mới hợp đồng lao động
 * @param {Object} payload - Dữ liệu hợp đồng (employeeId, contractTypeId, startDate, endDate, salary...)
 * @returns {Promise<Object>}
 */
export const createContract = async (payload) => {
    const response = await axios.post(CONTRACT_API, payload);
    return response.data;
};

/**
 * Cập nhật thông tin hợp đồng đang ở trạng thái nháp
 * @param {string|number} laborContractId - ID của hợp đồng
 * @param {Object} payload - Dữ liệu cập nhật
 * @returns {Promise<Object>}
 */
export const updateContract = async (laborContractId, payload) => {
    const response = await axios.put(`${CONTRACT_API}/${laborContractId}`, payload);
    return response.data;
};

/**
 * Xóa hợp đồng lao động (Chỉ cho phép khi ở trạng thái DRAFT)
 * @param {string|number} laborContractId - ID của hợp đồng
 * @returns {Promise<Object>}
 */
export const deleteContract = async (laborContractId) => {
    const response = await axios.delete(`${CONTRACT_API}/${laborContractId}`);
    return response.data;
};

/**
 * Gửi yêu cầu duyệt hợp đồng (Sang trạng thái REVIEWING)
 * @param {string|number} laborContractId - ID của hợp đồng
 * @returns {Promise<Object>}
 */
export const submitContract = async (laborContractId) => {
    const response = await axios.patch(`${CONTRACT_API}/${laborContractId}/submit`);
    return response.data;
};

/**
 * Thực hiện duyệt/từ chối hợp đồng
 * @param {string|number} laborContractId - ID của hợp đồng
 * @param {Object} payload - { decision: 'APPROVED' | 'REJECTED', reason: '...' }
 * @returns {Promise<Object>}
 */
export const reviewContract = async (laborContractId, payload) => {
    const response = await axios.patch(`${CONTRACT_API}/${laborContractId}/review`, payload);
    return response.data;
};

/**
 * Kích hoạt hợp đồng (Sang trạng thái ACTIVE / có hiệu lực)
 * @param {string|number} laborContractId - ID của hợp đồng
 * @returns {Promise<Object>}
 */
export const activateContract = async (laborContractId) => {
    const response = await axios.patch(`${CONTRACT_API}/${laborContractId}/activate`);
    return response.data;
};

/**
 * Sửa đổi vòng đời hợp đồng (vd: TERMINATED, EXPIRED)
 * @param {string|number} laborContractId - ID của hợp đồng
 * @param {Object} payload - { lifecycleStatus: 'TERMINATED', reason: '...' }
 * @returns {Promise<Object>}
 */
export const changeContractLifecycleStatus = async (laborContractId, payload) => {
    const response = await axios.patch(`${CONTRACT_API}/${laborContractId}/lifecycle-status`, payload);
    return response.data;
};

/**
 * Tạo bản nháp của hợp đồng tái ký từ hợp đồng hiện tại
 * @param {string|number} laborContractId - ID của hợp đồng gốc
 * @param {Object} payload - Dữ liệu tinh chỉnh cho bản nháp
 * @returns {Promise<Object>}
 */
export const createRenewalDraft = async (laborContractId, payload) => {
    const response = await axios.post(`${CONTRACT_API}/${laborContractId}/renewal-draft`, payload);
    return response.data;
};

// ==========================================
// APPENDIX
// ==========================================

/**
 * Lấy danh sách toàn bộ Pụ lục của 1 hợp đồng
 * @param {string|number} laborContractId - ID hợp đồng chính
 * @returns {Promise<Object>}
 */
export const getAppendices = async (laborContractId) => {
    const response = await axios.get(`${CONTRACT_API}/${laborContractId}/appendices`);
    return response.data;
};

/**
 * Thêm mới Phụ lục vào hợp đồng
 * @param {string|number} laborContractId - ID hợp đồng chính
 * @param {Object} payload - Dữ liệu phụ lục
 * @returns {Promise<Object>}
 */
export const createAppendix = async (laborContractId, payload) => {
    const response = await axios.post(`${CONTRACT_API}/${laborContractId}/appendices`, payload);
    return response.data;
};

/**
 * Cập nhật phụ lục
 * @param {string|number} laborContractId - ID hợp đồng chính
 * @param {string|number} appendixId - ID phụ lục
 * @param {Object} payload - Dữ liệu cập nhật
 * @returns {Promise<Object>}
 */
export const updateAppendix = async (laborContractId, appendixId, payload) => {
    const response = await axios.put(`${CONTRACT_API}/${laborContractId}/appendices/${appendixId}`, payload);
    return response.data;
};

/**
 * Kích hoạt phụ lục
 * @param {string|number} laborContractId - ID hợp đồng chính
 * @param {string|number} appendixId - ID phụ lục
 * @returns {Promise<Object>}
 */
export const activateAppendix = async (laborContractId, appendixId) => {
    const response = await axios.patch(`${CONTRACT_API}/${laborContractId}/appendices/${appendixId}/activate`);
    return response.data;
};

/**
 * Huỷ một phụ lục
 * @param {string|number} laborContractId - ID hợp đồng chính
 * @param {string|number} appendixId - ID phụ lục
 * @param {Object} payload - { reason: '...' }
 * @returns {Promise<Object>}
 */
export const cancelAppendix = async (laborContractId, appendixId, payload) => {
    const response = await axios.patch(`${CONTRACT_API}/${laborContractId}/appendices/${appendixId}/cancel`, payload);
    return response.data;
};

// ==========================================
// ATTACHMENTS
// ==========================================

/**
 * Lấy danh sách file đính kèm của hợp đồng
 * @param {string|number} laborContractId - ID của hợp đồng
 * @returns {Promise<Object>}
 */
export const getAttachments = async (laborContractId) => {
    const response = await axios.get(`${CONTRACT_API}/${laborContractId}/attachments`);
    return response.data;
};

/**
 * Đính kèm file mới cho hợp đồng
 * @param {string|number} laborContractId - ID của hợp đồng
 * @param {Object|FormData} payload - Dữ liệu file tải lên
 * @returns {Promise<Object>}
 */
export const createAttachment = async (laborContractId, payload) => {
    const response = await axios.post(`${CONTRACT_API}/${laborContractId}/attachments`, payload);
    return response.data;
};

/**
 * Cập nhật thông tin file đính kèm (vd đổi tên file, meta)
 * @param {string|number} laborContractId - ID của hợp đồng
 * @param {string|number} attachmentId - ID file
 * @param {Object} payload - Thông tin sửa đổn
 * @returns {Promise<Object>}
 */
export const updateAttachment = async (laborContractId, attachmentId, payload) => {
    const response = await axios.put(`${CONTRACT_API}/${laborContractId}/attachments/${attachmentId}`, payload);
    return response.data;
};

/**
 * Lưu trữ vĩnh viễn (archive) file đính kèm
 * @param {string|number} laborContractId - ID của hợp đồng
 * @param {string|number} attachmentId - ID file
 * @returns {Promise<Object>}
 */
export const archiveAttachment = async (laborContractId, attachmentId) => {
    const response = await axios.patch(`${CONTRACT_API}/${laborContractId}/attachments/${attachmentId}/archive`);
    return response.data;
};

/**
 * Xóa file đính kèm
 * @param {string|number} laborContractId - ID của hợp đồng
 * @param {string|number} attachmentId - ID file
 * @returns {Promise<Object>}
 */
export const deleteAttachment = async (laborContractId, attachmentId) => {
    const response = await axios.delete(`${CONTRACT_API}/${laborContractId}/attachments/${attachmentId}`);
    return response.data;
};

/**
 * Export hợp đồng lao động ra file Word hoặc PDF
 * @param {string|number} laborContractId - ID của hợp đồng
 * @returns {Promise<Blob>} Dữ liệu nhị phân của file tải xuống
 */
export const exportContract = async (laborContractId) => {
    const response = await axios.get(`${CONTRACT_API}/${laborContractId}/export`, { responseType: 'blob' });
    return response.data;
};
