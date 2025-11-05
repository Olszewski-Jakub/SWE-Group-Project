"use client";

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { setAccessToken, setRefreshToken } from '@/lib/axiosClient';

export default function LoginRedirectPage() {
  const { refresh } = useAuth();
  const router = useRouter();

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        // Prefer tokens passed via URL fragment by backend callback
        const hash = typeof window !== 'undefined' ? window.location.hash : '';
        const params = new URLSearchParams((hash || '').replace(/^#/, ''));
        const token = params.get('accessToken');
        const refreshTok = params.get('refreshToken');
        if (token) {
          setAccessToken(token);
          if (refreshTok) setRefreshToken(refreshTok);
          // Clean up the hash to avoid leaking tokens in history
          try { window.history.replaceState(null, '', window.location.pathname + window.location.search); } catch (_) {}
          // Sync auth context (user/roles) and schedule proactive refresh
          try { await refresh(); } catch (_) {}
        } else {
          // Fallback: rely on refresh cookie to obtain an access token
          await refresh();
        }
      } catch (_) {}
      if (!cancelled) router.replace('/');
    })();
    return () => { cancelled = true; };
  }, [refresh, router]);

  return null;
}
