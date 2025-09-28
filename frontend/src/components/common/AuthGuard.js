"use client";

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../../hooks/useAuth';

export default function AuthGuard({ children }) {
  const { isAuthenticated, loading, refresh } = useAuth();
  const router = useRouter();

  useEffect(() => {
    let cancelled = false;
    (async () => {
      if (loading) return;
      if (!isAuthenticated) {
        try {
          await refresh();
        } catch (_) {}
        if (!cancelled && !isAuthenticated) {
          router.replace('/signin');
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [isAuthenticated, loading, refresh, router]);

  if (loading) return <p>Loading...</p>;
  if (!isAuthenticated) return null;
  return children;
}

