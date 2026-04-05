import axios from "axios";

axios.defaults.baseURL = '/api';

// Request interceptor
axios.interceptors.request.use((config) => {
  // Can add tokens here if not using cookies
  config.withCredentials = true
  return config;
}, (error) => {
  return Promise.reject(error);
});

// Response interceptor
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('Axios Error:', error.response?.data || error.message);

    if (error.response?.status === 401) {
      if (!window.location.pathname.includes('/login')) {
        console.warn('Session expired, redirecting to login...');
        window.location.href = '/login';
      }
    }

    return Promise.reject(error);
  }
);

export default axios;