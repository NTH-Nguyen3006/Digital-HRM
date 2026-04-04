import axios from "axios";

const AUDIT_LOG_API = "/api/v1/admin/audit-logs";

/**
 * Tìm kiếm audit log (chỉ đọc, không có write endpoint)
 *
 * @param {Object} params
 * @param {string}  [params.moduleCode]     - Mã module (vd: USER, EMPLOYEE, ROLE, ...)
 * @param {string}  [params.actionCode]     - Mã hành động (vd: CREATE, UPDATE, DELETE, ...)
 * @param {string}  [params.resultCode]     - AuditResultCode: SUCCESS | FAILURE
 * @param {string}  [params.actorUsername]  - Username của người thực hiện
 * @param {string}  [params.from]           - Thời điểm bắt đầu ISO 8601 (vd: 2024-01-01T00:00:00)
 * @param {string}  [params.to]             - Thời điểm kết thúc ISO 8601
 * @param {number}  [params.page=0]         - Trang hiện tại (0-indexed)
 * @param {number}  [params.size=20]        - Số bản ghi mỗi trang
 * @returns {Promise<ApiResponse<PageResponse<AuditLogListItemResponse>>>}
 *
 * AuditLogListItemResponse: { auditLogId, actorUserId, actorUsername,
 *   actionCode, moduleCode, entityName, entityId, requestId,
 *   ipAddress, actionAt, resultCode, message }
 */
export const getAuditLogs = async ({
    moduleCode,
    actionCode,
    resultCode,
    actorUsername,
    from,
    to,
    page = 0,
    size = 20,
} = {}) => {
    try {
        const params = { page, size };
        if (moduleCode)    params.moduleCode    = moduleCode;
        if (actionCode)    params.actionCode    = actionCode;
        if (resultCode)    params.resultCode    = resultCode;
        if (actorUsername) params.actorUsername = actorUsername;
        if (from)          params.from          = from;
        if (to)            params.to            = to;

        const response = await axios.get(AUDIT_LOG_API, { params });
        return response.data;
    } catch (error) {
        console.error("getAuditLogs failed:", error);
        throw error;
    }
};
