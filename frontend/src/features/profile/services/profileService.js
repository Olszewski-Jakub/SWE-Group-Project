import axiosClient from '@/lib/axiosClient';
import { fetchMe } from '@/lib/auth';

export async function getProfile() {
  return fetchMe();
}

export async function updateProfile(payload) {
  const res = await axiosClient.patch('/auth/me', payload);
  return res?.data ?? null;
}
