import axios from 'axios';

const ONBOARDING_API = '/api/v1/admin/onboarding';

/**
 * Lấy danh sách các ứng viên đang làm thủ tục tiếp nhận
 * @param {Object} [params] - Lọc theo phòng ban, trạng thái onboarding
 * @returns {Promise<Object>}
 */
export const getOnboardings = async (params) => {
    const response = await axios.get(ONBOARDING_API, { params });
    return response.data;
};

/**
 * Xem hồ sơ nhận việc của một cá nhân
 * @param {string|number} onboardingId - Mã biên bản
 * @returns {Promise<Object>}
 */
export const getOnboardingDetail = async (onboardingId) => {
    const response = await axios.get(`${ONBOARDING_API}/${onboardingId}`);
    return response.data;
};

/**
 * Bắt đầu tạo mới tiến trình nhập môn cho nhân viên đỗ phỏng vấn
 * @param {Object} payload - Dữ liệu ứng viên
 * @returns {Promise<Object>}
 */
export const createOnboarding = async (payload) => {
    const response = await axios.post(ONBOARDING_API, payload);
    return response.data;
};

/**
 * Liên tục cập nhật các thủ tục onboarding đang diễn ra
 * @param {string|number} onboardingId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const updateOnboarding = async (onboardingId, payload) => {
    const response = await axios.put(`${ONBOARDING_API}/${onboardingId}`, payload);
    return response.data;
};

/**
 * HR thẩm định đóng hồ sơ kết thúc quá trình Onboarding (Hoàn tất 100%)
 * @param {string|number} onboardingId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const completeOnboarding = async (onboardingId, payload) => {
    const response = await axios.patch(`${ONBOARDING_API}/${onboardingId}/complete`, payload);
    return response.data;
};

/**
 * Gửi email báo hiệu thời gian đi làm đến ứng viên và IT Desk
 * @param {string|number} onboardingId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const notifyOnboarding = async (onboardingId, payload) => {
    const response = await axios.post(`${ONBOARDING_API}/${onboardingId}/notify`, payload);
    return response.data;
};

/**
 * Cấp phát sinh vạch User Name & ID login cho nhân sự 
 * @param {string|number} onboardingId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const createUserForOnboarding = async (onboardingId, payload) => {
    const response = await axios.post(`${ONBOARDING_API}/${onboardingId}/create-user`, payload);
    return response.data;
};

/**
 * Chuyển đổi dữ liệu và Tạo bản nháp Hợp đồng thử việc
 * @param {string|number} onboardingId 
 * @param {Object} payload 
 * @returns {Promise<Object>}
 */
export const createInitialContract = async (onboardingId, payload) => {
    const response = await axios.post(`${ONBOARDING_API}/${onboardingId}/initial-contract`, payload);
    return response.data;
};

/**
 * Quản lý checklist nhập môn
 */
export const upsertOnboardingChecklist = async (onboardingId, checklistId, payload) => {
    if (checklistId) {
        return (await axios.put(`${ONBOARDING_API}/${onboardingId}/checklist/${checklistId}`, payload)).data;
    }
    return (await axios.post(`${ONBOARDING_API}/${onboardingId}/checklist`, payload)).data;
};

/**
 * Quản lý hồ sơ đầu vào
 */
export const upsertOnboardingDocument = async (onboardingId, documentId, payload) => {
    if (documentId) {
        return (await axios.put(`${ONBOARDING_API}/${onboardingId}/documents/${documentId}`, payload)).data;
    }
    return (await axios.post(`${ONBOARDING_API}/${onboardingId}/documents`, payload)).data;
};

/**
 * Quản lý cấp phát tài sản
 */
export const upsertOnboardingAsset = async (onboardingId, assetId, payload) => {
    if (assetId) {
        return (await axios.put(`${ONBOARDING_API}/${onboardingId}/assets/${assetId}`, payload)).data;
    }
    return (await axios.post(`${ONBOARDING_API}/${onboardingId}/assets`, payload)).data;
};
