import { handleLogin, handleLogout } from '@/api/auth';
import { defineStore } from 'pinia';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user')) || null,
    isAuthenticatedStatus: localStorage.getItem('isAuthenticated') === 'true'
  }),

  getters: {
    isAuthenticated: (state) => state.isAuthenticatedStatus,
    roles: (state) => state.user?.roles || [],
    roleCode: (state) => state.user?.roleCode || '',
    isAdmin: (state) => state.user?.roleCode === 'ADMIN' || state.user?.roles?.includes('admin'),
    isHR: (state) => state.user?.roleCode === 'HR' || state.user?.roles?.includes('hr'),
    isManager: (state) => state.user?.roleCode === 'MANAGER' || state.user?.roles?.includes('manager'),
    isEmployee: (state) => state.user?.roleCode === 'EMPLOYEE' || state.user?.roles?.includes('employee'),
  },

  actions: {
    async login(username, password) {
      try {
        const responseData = await handleLogin(username, password);

        // Backend returns an ApiResponse with the actual payload in the "data" field.
        const authData = responseData.data;

        console.log(authData);


        // Backend sets HttpOnly cookies for tokens automatically.
        // We only save user info and auth status here
        this.user = authData;
        this.isAuthenticatedStatus = true;

        localStorage.setItem('isAuthenticated', 'true');
        if (this.user) {
          localStorage.setItem('user', JSON.stringify(this.user));
        }

        return authData;
      } catch (error) {
        console.error('Store Login failed', error);
        throw error;
      }
    },

    async logout() {
      try {
        await handleLogout();
      } catch (e) {
        console.warn('Logout api failed', e);
      } finally {
        this.clearAuth();
      }
    },

    clearAuth() {
      this.user = null;
      this.isAuthenticatedStatus = false;
      localStorage.removeItem('isAuthenticated');
      localStorage.removeItem('user');
    }
  }
});
