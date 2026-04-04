import * as authApi from '../auth.js';

export const testAuthFlow = async () => {

    let accessToken = null;
    let refreshToken = null;

    try {
        const loginData = await authApi.handleLogin(
            'admin',
            'Admin@123456',
            'Test Device',
            'Windows',
            'Chrome'
        );

        accessToken = loginData.data?.accessToken;
        refreshToken = loginData.data?.refreshToken;

        // 2. Test Refresh Token
        if (refreshToken) {
            const refreshData = await authApi.handleRefresh(refreshToken);
        }

        // 3. Test Forgot Password
        const forgotData = await authApi.forgotPassword('admin@digitalhrm.local');

        // 4. Test Logout (Nếu có token)
        if (accessToken) {
            const logoutData = await authApi.handleLogout(accessToken);
        }

    } catch (error) {
        if (error.response) {
            console.error('Status:', error.response.status);
            console.error('Error Data:', error.response.data);
        } else {
            console.error('Message:', error.message);
        }
    }

};
