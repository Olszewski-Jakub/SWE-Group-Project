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

// Build Google OAuth authorize URL, optionally overriding the post-login redirect
export function getGoogleAuthorizeUrl(redirectTo) {
  const apiBase = process.env.NEXT_PUBLIC_API_BASE_URL || '';
  let appOrigin = '';
  try { appOrigin = typeof window !== 'undefined' ? window.location.origin : ''; } catch (_) {}
  const finalRedirect = redirectTo || (appOrigin ? `${appOrigin}/signin/redirect` : '/signin/redirect');
  const redirect = encodeURIComponent(finalRedirect);
  return `${apiBase}/auth/oauth/google/authorize/redirect?redirect=${redirect}`;
}

// Start Google OAuth by navigating the browser to the authorize URL
export function startGoogleLogin(redirectTo) {
  const url = getGoogleAuthorizeUrl(redirectTo);
  if (typeof window !== 'undefined') {
    window.location.assign(url);
  }
  return url;
}
