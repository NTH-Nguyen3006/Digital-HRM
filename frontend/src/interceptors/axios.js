import axios from "axios";

axios.defaults.withCredentials = true;

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

// send rq config
axios.interceptors.request.use((config) => {
  config.withCredentials = true;
  return config;
}, (error) => {
  return Promise.reject(error);
});


// response config
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (!originalRequest) {
      return Promise.reject(error);
    }

    if (originalRequest.url.includes('/api/v1/auth/login') ||
      originalRequest.url.includes('/api/v1/auth/refresh')) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise(function (resolve, reject) {
          failedQueue.push({ resolve, reject });
        }).then(() => {
          return axios(originalRequest);
        }).catch(err => {
          return Promise.reject(err);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        await axios.post('/api/v1/auth/refresh', {}, { withCredentials: true });

        processQueue(null);
        isRefreshing = false;

        return axios(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        isRefreshing = false;

        if (!window.location.pathname.includes('/login')) {
          console.warn('Session expired (refresh failed), redirecting to login...');
          localStorage.removeItem('user');
          localStorage.removeItem('isAuthenticated');
          window.location.href = '/login';
        }
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default axios;