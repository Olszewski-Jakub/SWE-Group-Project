"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { bootstrapToken, signIn as apiSignIn, signUp as apiSignUp, signOut as apiSignOut, refresh as apiRefresh, fetchMe } from '../../lib/auth';
import { getAccessToken, scheduleProactiveRefresh, setLogoutHandler, setAccessToken, setDefaultRefreshScheduler, loadTokenFromStorage } from '../../lib/axiosClient';
import { getJwtRoles, parseJwt } from '../../lib/jwt';

const AuthContext = createContext({
  user: null,
  roles: [],
  accessToken: null,
  loading: true,
  isAuthenticated: false,
  signIn: async () => {},
  signUp: async () => {},
  signOut: async () => {},
  refresh: async () => {},
});

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isClient, setIsClient] = useState(false);
  const mountedRef = useRef(true);

  // Helper to sync roles from current token
  const syncRolesFromToken = useCallback(() => {
    const token = getAccessToken();
    const r = getJwtRoles(token) || [];
    setRoles(Array.isArray(r) ? r : []);
    return token;
  }, []);

  const userFromToken = useCallback(() => {
    const token = getAccessToken();
    const payload = parseJwt(token) || {};
    if (!token || !payload) return null;
    const nameParts = [payload.given_name, payload.family_name].filter(Boolean);
    const name = payload.name || (nameParts.length ? nameParts.join(' ') : undefined);
    return {
      id: payload.userId || payload.id || payload.sub || null,
      email: payload.email || payload.preferred_username || payload.upn || payload.sub || null,
      name: name || null,
    };
  }, []);

  const scheduleRefresh = useCallback(() => {
    const token = getAccessToken();
    if (!token) return;
    scheduleProactiveRefresh(token, async () => {
      try {
        const newToken = await apiRefresh({ onRefreshSchedule: scheduleRefresh });
        if (newToken) {
          // Ensure roles update
          syncRolesFromToken();
        }
      } catch (_) {
        // Silently ignore; interceptor will force logout on next request
      }
    });
  }, [syncRolesFromToken]);

  useEffect(() => {
    // Mark that we are running on the client to avoid SSR hydration mismatch
    setIsClient(true);
    mountedRef.current = true;
    // Provide default scheduler so 401-based refreshes also reschedule proactively
    setDefaultRefreshScheduler(async () => {
      try {
        const newToken = await apiRefresh({ onRefreshSchedule: scheduleRefresh });
        if (newToken) syncRolesFromToken();
      } catch (_) {}
    });
    (async () => {
      try {
        // Load any persisted token and schedule refresh (uses localStorage)
        const token = bootstrapToken({ onRefreshSchedule: scheduleRefresh });
        if (token) {
          syncRolesFromToken();
          // Set optimistic user from JWT claims immediately (API may not have /auth/me)
          const optimistic = userFromToken();
          if (mountedRef.current) setUser(optimistic);
          // Try to hydrate from /auth/me if available; ignore failures
          try {
            const me = await fetchMe({ suppressLogoutOn401: true });
            if (me && mountedRef.current) setUser(me);
          } catch (_) {}
        }
      } finally {
        if (mountedRef.current) setLoading(false);
      }
    })();
    return () => {
      mountedRef.current = false;
    };
  }, [scheduleRefresh, syncRolesFromToken]);

  const signIn = useCallback(async (credentials) => {
    const { user: u, accessToken } = await apiSignIn(credentials, { onRefreshSchedule: scheduleRefresh });
    let resolvedUser = u || userFromToken();
    if (!resolvedUser) {
      try {
        const me = await fetchMe();
        resolvedUser = me || userFromToken();
      } catch (_) {
        resolvedUser = userFromToken();
      }
    }
    if (mountedRef.current) {
      setUser(resolvedUser);
      setRoles(getJwtRoles(accessToken) || []);
    }
    return resolvedUser;
  }, [scheduleRefresh, userFromToken]);

  const signUp = useCallback(async (data) => {
    return apiSignUp(data);
  }, []);

  const signOut = useCallback(async () => {
    await apiSignOut();
    if (mountedRef.current) {
      setUser(null);
      setRoles([]);
    }
  }, []);

  const refresh = useCallback(async () => {
    const t = await apiRefresh({ onRefreshSchedule: scheduleRefresh });
    if (t) {
      syncRolesFromToken();
      // Update user from token immediately; API hydration optional
      const optimistic = userFromToken();
      if (mountedRef.current) setUser(optimistic);
      try {
        const me = await fetchMe({ suppressLogoutOn401: true });
        if (me && mountedRef.current) setUser(me);
      } catch (_) {}
    }
    return t;
  }, [scheduleRefresh, syncRolesFromToken, userFromToken]);

  // Allow axios to trigger logout + redirect
  useEffect(() => {
    setLogoutHandler(async () => {
      try { await signOut(); } catch (_) {}
      if (typeof window !== 'undefined' && window.location.pathname !== '/signin') {
        window.location.assign('/signin');
      }
    });
    return () => setLogoutHandler(null);
  }, [signOut]);

  const value = useMemo(() => ({
    user,
    roles,
    accessToken: getAccessToken(),
    loading,
    isAuthenticated: !!user,
    signIn,
    signUp,
    signOut,
    refresh,
  }), [user, roles, loading, signIn, signUp, signOut, refresh]);
  // Avoid SSR/client hydration mismatch and ensure we only read browser storage on client
  if (!isClient) return null;
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuthContext() {
  return useContext(AuthContext);
}

export default AuthContext;
