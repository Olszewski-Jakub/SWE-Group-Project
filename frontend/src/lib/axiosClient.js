import axios from 'axios';
import { getJwtExp } from './jwt';

// In-memory token + localStorage mirror (with one-time sessionStorage migration)
let accessToken = null;
let refreshPromise = null;
let logoutHandler = null;
let proactiveTimerId = null;
let defaultRefreshScheduler = null;

export function setLogoutHandler(handler) {
  logoutHandler = typeof handler === 'function' ? handler : null;
}

export function getAccessToken() {
  return accessToken;
}

// Prefer localStorage; migrate from sessionStorage if present
export function loadTokenFromStorage() {
  try {
    if (typeof window === 'undefined') return null;
    let t = localStorage.getItem('accessToken');
    if (!t) {
      // Backward-compat: migrate existing session token once
      const legacy = sessionStorage.getItem('accessToken');
      if (legacy) {
        try { localStorage.setItem('accessToken', legacy); } catch (_) {}
        try { sessionStorage.removeItem('accessToken'); } catch (_) {}
        t = legacy;
      }
    }
    if (t) accessToken = t;
    return t || null;
  } catch (_) {
    return null;
  }
}

export function clearProactiveTimer() {
  if (proactiveTimerId) {
    clearTimeout(proactiveTimerId);
    proactiveTimerId = null;
  }
}

export function scheduleProactiveRefresh(token, refreshFn) {
  clearProactiveTimer();
  const exp = getJwtExp(token);
  if (!exp) return;
  const nowSec = Math.floor(Date.now() / 1000);
  const lead = 60; // seconds before expiry
  const delayMs = Math.max((exp - nowSec - lead) * 1000, 0);
  if (delayMs === 0) {
    // Expired or within lead window — trigger once soon
    proactiveTimerId = setTimeout(() => refreshFn().catch(() => {}), 500);
  } else {
    proactiveTimerId = setTimeout(() => refreshFn().catch(() => {}), delayMs);
  }
}

export function setDefaultRefreshScheduler(fn) {
  defaultRefreshScheduler = typeof fn === 'function' ? fn : null;
}

export function setAccessToken(token, { persist = true, onRefreshSchedule } = {}) {
  accessToken = token || null;
  try {
    if (typeof window !== 'undefined' && persist) {
      if (token) localStorage.setItem('accessToken', token);
      else {
        localStorage.removeItem('accessToken');
        // Clean up any legacy session storage
        try { sessionStorage.removeItem('accessToken'); } catch (_) {}
      }
    }
  } catch (_) {}
  if (token && (typeof onRefreshSchedule === 'function' || typeof defaultRefreshScheduler === 'function')) {
    scheduleProactiveRefresh(token, onRefreshSchedule || defaultRefreshScheduler);
  } else {
    clearProactiveTimer();
  }
}

// Base URL:
// - local: set via NEXT_PUBLIC_API_BASE_URL (e.g., http://localhost:4000)
// - dev/prod: leave empty to use same-origin relative paths or reverse proxy
const baseURL = process.env.NEXT_PUBLIC_API_BASE_URL || '';

const axiosClient = axios.create({
  baseURL: baseURL,
  withCredentials: true,
});

// Attach Authorization header
axiosClient.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

async function performRefresh() {
  if (!refreshPromise) {
    refreshPromise = axios
      .post(`${baseURL}/auth/refresh`, {}, { withCredentials: true })
      .then((res) => {
        const newToken = res?.data?.accessToken;
        if (!newToken) throw new Error('No access token from refresh');
        // setAccessToken is called by consumer to re-schedule proactive refresh
        return newToken;
      })
      .catch((err) => {
        throw err;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

// Single-flight refresh on 401s
axiosClient.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error?.config || {};
    const status = error?.response?.status;
    if (status === 401 && !original._retry) {
      original._retry = true;
      try {
        const newToken = await performRefresh();
        // Persist token for subsequent requests; scheduling uses default scheduler if set
        setAccessToken(newToken);
        const headers = original.headers || {};
        headers.Authorization = `Bearer ${newToken}`;
        original.headers = headers;
        return axiosClient(original);
      } catch (e) {
        // Allow callers to suppress global logout/redirect on bootstrap or optional calls
        if (original.suppressLogoutOn401) {
          return Promise.reject(error);
        }
        try { if (logoutHandler) logoutHandler(); } catch (_) {}
        if (typeof window !== 'undefined' && window.location.pathname !== '/signin') {
          window.location.assign('/signin');
        }
        // Also reject to propagate error if needed
        return Promise.reject(error);
      }
    }
    return Promise.reject(error);
  }
);

export async function refreshAccessTokenAndStore({ onRefreshSchedule } = {}) {
  const newToken = await performRefresh();
  setAccessToken(newToken, { onRefreshSchedule });
  return newToken;
}



export default axiosClient;
