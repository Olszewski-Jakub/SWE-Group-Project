import axios from 'axios';

// Simple in-memory access token store
let accessToken = null;
let refreshPromise = null;
let logoutHandler = null;

export function setAccessToken(token) {
  accessToken = token || null;
}

export function getAccessToken() {
  return accessToken;
}

export function setLogoutHandler(handler) {
  logoutHandler = typeof handler === 'function' ? handler : null;
}

const baseURL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:4000';

const apiClient = axios.create({
  baseURL,
  withCredentials: true, // send cookies (refresh token lives in HttpOnly cookie)
});

// Attach Authorization header if access token exists
apiClient.interceptors.request.use(
  (config) => {
    const token = getAccessToken();
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

async function refreshAccessToken() {
  if (!refreshPromise) {
    // Use a bare axios call to avoid interceptor loops
    refreshPromise = axios
      .post(`${baseURL}/auth/refresh`, {}, { withCredentials: true })
      .then((res) => {
        const newToken = res?.data?.accessToken;
        if (!newToken) throw new Error('No access token in refresh response');
        setAccessToken(newToken);
        return newToken;
      })
      .catch((err) => {
        setAccessToken(null);
        throw err;
      })
      .finally(() => {
        // Allow future refresh attempts
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

// On 401, try refresh once and retry the original request
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error?.config || {};
    const status = error?.response?.status;

    if (status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        await refreshAccessToken();
        // Re-attach the (new) token header and retry
        const token = getAccessToken();
        originalRequest.headers = originalRequest.headers || {};
        if (token) {
          originalRequest.headers.Authorization = `Bearer ${token}`;
        } else {
          delete originalRequest.headers.Authorization;
        }
        return apiClient(originalRequest);
      } catch (refreshErr) {
        // Refresh failed — ensure logout and redirect
        try {
          if (logoutHandler) logoutHandler();
        } catch (_) {}
        if (typeof window !== 'undefined') {
          // Avoid infinite loops if already on /login
          if (window.location.pathname !== '/login') {
            window.location.assign('/login');
          }
        }
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;

