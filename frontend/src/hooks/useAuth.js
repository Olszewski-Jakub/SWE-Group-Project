"use client";

import { useAuthContext } from '@/features/auth/AuthContext';

export function useAuth() {
  return useAuthContext();
}

export default useAuth;

