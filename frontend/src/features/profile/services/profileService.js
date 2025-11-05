import axiosClient from '@/lib/axiosClient';
import { fetchMe } from '@/lib/auth';

export async function getProfile() {
  return fetchMe();
}

// Try common endpoints; adjust as backend stabilizes
export async function updateProfile(payload) {
  // Use /auth/me per backend contract
  const res = await axiosClient.patch('/auth/me', payload);
  return res?.data ?? null;
}
