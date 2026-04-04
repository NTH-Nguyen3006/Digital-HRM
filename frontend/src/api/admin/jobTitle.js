import axios from "axios";

const JOB_TITLE_API = "/api/v1/admin/job-titles";

/**
 * Lấy danh sách chức danh (phân trang, tìm kiếm, lọc trạng thái)
 *
 * @param {Object} params
 * @param {string}  [params.keyword]   - Tìm theo jobTitleCode / jobTitleName
 * @param {string}  [params.status]    - RecordStatus: ACTIVE | INACTIVE
 * @param {number}  [params.page=0]    - Trang hiện tại (0-indexed)
 * @param {number}  [params.size=20]   - Số bản ghi mỗi trang
 * @returns {Promise<ApiResponse<PageResponse<JobTitleListItemResponse>>>}
 *
 * JobTitleListItemResponse: { jobTitleId, jobTitleCode, jobTitleName,
 *   jobLevelCode, description, sortOrder, status }
 */
export const getJobTitles = async ({
    keyword,
    status,
    page = 0,
    size = 20,
} = {}) => {
    try {
        const params = { page, size };
        if (keyword) params.keyword = keyword;
        if (status)  params.status  = status;

        const response = await axios.get(JOB_TITLE_API, { params });
        return response.data;
    } catch (error) {
        console.error("getJobTitles failed:", error);
        throw error;
    }
};

/**
 * Lấy chi tiết một chức danh
 *
 * @param {number} jobTitleId
 * @returns {Promise<ApiResponse<JobTitleDetailResponse>>}
 *
 * JobTitleDetailResponse: { jobTitleId, jobTitleCode, jobTitleName,
 *   jobLevelCode, description, sortOrder, status }
 */
export const getJobTitleDetail = async (jobTitleId) => {
    try {
        const response = await axios.get(`${JOB_TITLE_API}/${jobTitleId}`);
        return response.data;
    } catch (error) {
        console.error("getJobTitleDetail failed:", error);
        throw error;
    }
};

/**
 * Tạo mới chức danh
 *
 * @param {Object}  payload
 * @param {string}  payload.jobTitleCode   - Mã chức danh (bắt buộc, ≤30 ký tự)
 * @param {string}  payload.jobTitleName   - Tên chức danh (bắt buộc, ≤200 ký tự)
 * @param {string}  [payload.jobLevelCode] - Mã cấp bậc (≤30 ký tự)
 * @param {string}  [payload.description]  - Mô tả (≤1000 ký tự)
 * @param {number}  [payload.sortOrder]    - Thứ tự hiển thị (≥0)
 * @returns {Promise<ApiResponse<JobTitleDetailResponse>>}
 */
export const createJobTitle = async (payload) => {
    try {
        const response = await axios.post(JOB_TITLE_API, payload);
        return response.data;
    } catch (error) {
        console.error("createJobTitle failed:", error);
        throw error;
    }
};

/**
 * Cập nhật chức danh
 *
 * @param {number}  jobTitleId
 * @param {Object}  payload
 * @param {string}  payload.jobTitleCode   - Mã chức danh (bắt buộc, ≤30 ký tự)
 * @param {string}  payload.jobTitleName   - Tên chức danh (bắt buộc, ≤200 ký tự)
 * @param {string}  [payload.jobLevelCode] - Mã cấp bậc (≤30 ký tự)
 * @param {string}  [payload.description]  - Mô tả (≤1000 ký tự)
 * @param {number}  [payload.sortOrder]    - Thứ tự hiển thị (≥0)
 * @returns {Promise<ApiResponse<JobTitleDetailResponse>>}
 */
export const updateJobTitle = async (jobTitleId, payload) => {
    try {
        const response = await axios.put(`${JOB_TITLE_API}/${jobTitleId}`, payload);
        return response.data;
    } catch (error) {
        console.error("updateJobTitle failed:", error);
        throw error;
    }
};

/**
 * Thay đổi trạng thái chức danh (kích hoạt / vô hiệu hoá)
 *
 * @param {number}  jobTitleId
 * @param {Object}  payload
 * @param {string}       payload.status  - RecordStatus (bắt buộc): ACTIVE | INACTIVE
 * @param {string|null}  [payload.note]  - Ghi chú lý do
 * @returns {Promise<ApiResponse<JobTitleDetailResponse>>}
 */
export const changeJobTitleStatus = async (jobTitleId, payload) => {
    try {
        const response = await axios.patch(`${JOB_TITLE_API}/${jobTitleId}/status`, payload);
        return response.data;
    } catch (error) {
        console.error("changeJobTitleStatus failed:", error);
        throw error;
    }
};
