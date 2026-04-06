import axios from "axios";

const AUTH_API = '/api/v1/auth';

/**
 * Đăng nhập vào hệ thống
 * @param {string} username - Tên đăng nhập (loginId)
 * @param {string} password - Mật khẩu
 * @param {string} [deviceName=''] - Tên thiết bị đăng nhập
 * @param {string} [deviceOs=''] - Hệ điều hành thiết bị
 * @param {string} [browserName=''] - Tên trình duyệt
 * @returns {Promise<Object>} - Dữ liệu trả về từ API (chứa accessToken, refreshToken,...)
 */
export const handleLogin = async (username, password, deviceName = '', deviceOs = '', browserName = '') => {
    try {
        const response = await axios.post(`${AUTH_API}/login`, {
            loginId: username,
            password: password,
            deviceName: deviceName,
            deviceOs: deviceOs,
            browserName: browserName
        });
        return response.data;
    } catch (error) {
        console.error('Login failed:', error);
        throw error;
    }
};

/**
 * Làm mới mã xác thực (refresh token)
 * Backend sẽ tự động lấy refreshToken từ HttpOnly cookie.
 * @returns {Promise<Object>} - Dữ liệu mã xác thực mới
 */
export const handleRefresh = async () => {
    try {
        const response = await axios.post(`${AUTH_API}/refresh`);
        return response.data;
    } catch (error) {
        console.error('Refresh token failed:', error);
        throw error;
    }
};

/**
 * Đăng xuất khỏi hệ thống
 * @param {string} [token] - Access token hiện tại (để gửi trong header Authorization)
 * @returns {Promise<Object>} - Kết quả đăng xuất
 */
export const handleLogout = async () => {
    try {
        const response = await axios.post(`${AUTH_API}/logout`);
        return response.data;
    } catch (error) {
        console.error('Logout failed:', error);
        throw error;
    }
};

/**
 * Gửi yêu cầu quên mật khẩu
 * @param {string} email - Email nhận mã khôi phục
 * @returns {Promise<Object>} - Trạng thái gửi email
 */
export const forgotPassword = async (email) => {
    try {
        const response = await axios.post(`${AUTH_API}/forgot-password`, {
            email: email
        });
        return response.data;
    } catch (error) {
        console.error('Forgot password failed:', error);
        throw error;
    }
};

/**
 * Đặt lại mật khẩu mới
 * @param {string} token - Mã xác thực đặt lại mật khẩu từ email
 * @param {string} newPassword - Mật khẩu mới
 * @param {string} confirmPassword - Xác nhận mật khẩu mới
 * @returns {Promise<Object>} - Kết quả thay đổi mật khẩu
 */
export const resetPassword = async (token, newPassword, confirmPassword) => {
    try {
        const response = await axios.post(`${AUTH_API}/reset-password`, {
            token: token,
            newPassword: newPassword,
            confirmPassword: confirmPassword
        });
        return response.data;
    } catch (error) {
        console.error('Reset password failed:', error);
        throw error;
    }
};

/**
 * Thay đổi mật khẩu khi đang đăng nhập
 * @param {string} currentPassword - Mật khẩu hiện tại
 * @param {string} newPassword - Mật khẩu mới
 * @param {string} confirmPassword - Xác nhận mật khẩu mới
 * @param {string} [token] - Access token (Authorization header)
 * @returns {Promise<Object>} - Kết quả thay đổi mật khẩu
 */
export const changePassword = async (currentPassword, newPassword, confirmPassword) => {
    try {
        const response = await axios.post(`${AUTH_API}/change-password`, {
            currentPassword: currentPassword,
            newPassword: newPassword,
            confirmPassword: confirmPassword
        });
        return response.data;
    } catch (error) {
        console.error('Change password failed:', error);
        throw error;
    }
};