import axiosClient, { setAccessToken, loadTokenFromStorage, refreshAccessTokenAndStore } from './axiosClient';

export function bootstrapToken({ onRefreshSchedule } = {}) {
  const token = loadTokenFromStorage();
  if (token) setAccessToken(token, { onRefreshSchedule });
  return token;
}

export async function signIn({ email, password }, { onRefreshSchedule } = {}) {
  const res = await axiosClient.post('/auth/login', { email, password });
  const { accessToken, user } = res.data || {};
  if (accessToken) setAccessToken(accessToken, { onRefreshSchedule });
  return { user: user || null, accessToken: accessToken || null };
}

export async function signUp({ firstName, lastName, email, password }) {
  const res = await axiosClient.post('/auth/register', { firstName, lastName, email, password });
  return res.data || {};
}

export async function signOut() {
  try { await axiosClient.post('/auth/logout'); } catch (_) {}
  setAccessToken(null);
}

export async function signOutAll() {
  try { await axiosClient.post('/auth/logout-all'); } catch (_) {}
  setAccessToken(null);
}

export async function refresh({ onRefreshSchedule } = {}) {
  const token = await refreshAccessTokenAndStore({ onRefreshSchedule });
  return token;
}

export async function fetchMe(config = {}) {
  const res = await axiosClient.get('/auth/me', { ...config });
  return res.data || null;
}

export async function verifyEmail(token) {
  const res = await axiosClient.post('/auth/verify-email', { token });
  return res?.data ?? null;
}

// Reset password using token issued via forgot-password flow
export async function resetPassword({ token, password }) {
  // Endpoint served by ResetPasswordController at /auth/reset-password
  // Returns 204 No Content on success
  return axiosClient.post('/auth/reset-password', { token, password });
}

// Request a password reset email
export async function requestPasswordReset({ email, locale }) {
  const payload = { email, locale };
  const res = await axiosClient.post('/auth/forgot-password', payload);
  return res?.data ?? null; // { message }
}
