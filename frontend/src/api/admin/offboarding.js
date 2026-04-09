import axios from 'axios';

const OFFBOARDING_API = '/api/v1/admin/offboarding';

/**
 * Xem danh sách nhân sự đang làm thủ tục bàn giao, nghỉ việc
 * @param {Object} [params] - Params lọc trạng thái
 * @returns {Promise<Object>}
 */
export const getOffboardings = async (params) => {
    const response = await axios.get(OFFBOARDING_API, { params });
    return response.data;
};

/**
 * Xem chi tiết hồ sơ Offboard
 * @param {string|number} offboardingCaseId - Mã hồ sơ Offboarding
 * @returns {Promise<Object>}
 */
export const getOffboardingDetail = async (offboardingCaseId) => {
    const response = await axios.get(`${OFFBOARDING_API}/${offboardingCaseId}`);
    return response.data;
};

/**
 * HR thẩm định giấy thanh lý, báo cáo tổng và phê duyệt đóng Offboarding
 * @param {string|number} offboardingCaseId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const finalizeOffboarding = async (offboardingCaseId, payload) => {
    const response = await axios.patch(`${OFFBOARDING_API}/${offboardingCaseId}/finalize`, payload);
    return response.data;
};

/**
 * Khởi tạo Record thu hồi tài sản IT (Màn hình, công cụ)
 * @param {string|number} offboardingCaseId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const createAssetReturn = async (offboardingCaseId, payload) => {
    const response = await axios.post(`${OFFBOARDING_API}/${offboardingCaseId}/asset-returns`, payload);
    return response.data;
};

/**
 * IT Dept cập nhật tình trạng trả máy móc
 * @param {string|number} assetReturnId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const updateAssetReturn = async (assetReturnId, payload) => {
    const response = await axios.put(`${OFFBOARDING_API}/asset-returns/${assetReturnId}`, payload);
    return response.data;
};

/**
 * Kế toán thực hiện đối soát lương dư và công nợ kết thúc
 * @param {string|number} offboardingCaseId 
 * @param {Object} payload - { clearanceStatus: true,... }
 * @returns {Promise<Object>}
 */
export const processSettlement = async (offboardingCaseId, payload) => {
    const response = await axios.patch(`${OFFBOARDING_API}/${offboardingCaseId}/settlement`, payload);
    return response.data;
};

/**
 * Đóng vĩnh viễn tiến trình nghỉ việc
 * @param {string|number} offboardingCaseId 
 * @returns {Promise<Object>}
 */
export const closeOffboarding = async (offboardingCaseId, payload) => {
    const response = await axios.patch(`${OFFBOARDING_API}/${offboardingCaseId}/close`, payload);
    return response.data;
};

/**
 * Thu hồi quyền login vào hệ thống
 * @param {string|number} offboardingCaseId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const revokeAccess = async (offboardingCaseId, payload) => {
    const response = await axios.patch(`${OFFBOARDING_API}/${offboardingCaseId}/revoke-access`, payload);
    return response.data;
};
