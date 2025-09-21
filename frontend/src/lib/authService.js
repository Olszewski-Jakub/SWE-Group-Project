import axios from 'axios';
import apiClient, { setAccessToken } from './apiClient';

const baseURL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:4000';

export async function login(credentials) {
  // Expect backend to set HttpOnly refresh cookie via Set-Cookie
  const res = await apiClient.post('/auth/login', credentials);
  const { user, accessToken } = res.data || {};
  if (accessToken) setAccessToken(accessToken);
  return user || null;
}

export async function logout() {
  try {
    await apiClient.post('/auth/logout');
  } catch (_) {}
  setAccessToken(null);
}

export async function getProfile() {
  const res = await apiClient.get('/auth/me');
  return res.data || null;
}

export async function refreshAccessToken() {
  // Use a bare axios call to avoid interceptor recursion
  const res = await axios.post(`${baseURL}/auth/refresh`, {}, { withCredentials: true });
  const token = res?.data?.accessToken;
  if (token) setAccessToken(token);
  return token;
}

export async function restoreSession() {
  try {
    await refreshAccessToken();
    const user = await getProfile();
    return user;
  } catch (e) {
    return null;
  }
}

