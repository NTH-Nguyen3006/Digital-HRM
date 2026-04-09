import axios from "axios";

const ADMIN_SYSTEM_CONFIG_API = "/api/v1/admin";

export const getPermissionMatrix = async () => {
    try {
        const response = await axios.get(`${ADMIN_SYSTEM_CONFIG_API}/permissions/matrix`);
        return response.data;
    } catch (error) {
        console.error("getPermissionMatrix failed:", error);
        throw error;
    }
};

export const getRoleMenuConfigs = async (roleId) => {
    try {
        const response = await axios.get(`${ADMIN_SYSTEM_CONFIG_API}/roles/${roleId}/menu-configs`);
        return response.data;
    } catch (error) {
        console.error("getRoleMenuConfigs failed:", error);
        throw error;
    }
};

export const replaceRoleMenuConfigs = async (roleId, payload) => {
    try {
        const response = await axios.put(`${ADMIN_SYSTEM_CONFIG_API}/roles/${roleId}/menu-configs`, payload);
        return response.data;
    } catch (error) {
        console.error("replaceRoleMenuConfigs failed:", error);
        throw error;
    }
};

export const getNotificationTemplates = async () => {
    try {
        const response = await axios.get(`${ADMIN_SYSTEM_CONFIG_API}/notification-templates`);
        return response.data;
    } catch (error) {
        console.error("getNotificationTemplates failed:", error);
        throw error;
    }
};

export const createNotificationTemplate = async (payload) => {
    try {
        const response = await axios.post(`${ADMIN_SYSTEM_CONFIG_API}/notification-templates`, payload);
        return response.data;
    } catch (error) {
        console.error("createNotificationTemplate failed:", error);
        throw error;
    }
};

export const updateNotificationTemplate = async (templateId, payload) => {
    try {
        const response = await axios.put(`${ADMIN_SYSTEM_CONFIG_API}/notification-templates/${templateId}`, payload);
        return response.data;
    } catch (error) {
        console.error("updateNotificationTemplate failed:", error);
        throw error;
    }
};

export const getPlatformSettings = async () => {
    try {
        const response = await axios.get(`${ADMIN_SYSTEM_CONFIG_API}/platform-settings`);
        return response.data;
    } catch (error) {
        console.error("getPlatformSettings failed:", error);
        throw error;
    }
};

export const updatePlatformSetting = async (settingId, payload) => {
    try {
        const response = await axios.put(`${ADMIN_SYSTEM_CONFIG_API}/platform-settings/${settingId}`, payload);
        return response.data;
    } catch (error) {
        console.error("updatePlatformSetting failed:", error);
        throw error;
    }
};
