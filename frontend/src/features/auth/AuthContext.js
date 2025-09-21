"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { login as apiLogin, logout as apiLogout, restoreSession } from '../../lib/authService';
import { setLogoutHandler } from '../../lib/apiClient';

const AuthContext = createContext({
  user: null,
  loading: true,
  isAuthenticated: false,
  login: async () => {},
  logout: () => {},
});

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const u = await restoreSession();
        if (mounted) setUser(u);
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => {
      mounted = false;
    };
  }, []);

  const login = useCallback(async (credentials) => {
    const u = await apiLogin(credentials);
    setUser(u);
    return u;
  }, []);

  const logout = useCallback(async () => {
    await apiLogout();
    setUser(null);
  }, []);

  // Allow apiClient to trigger logout + redirect on failed refresh
  useEffect(() => {
    setLogoutHandler(logout);
    return () => setLogoutHandler(null);
  }, [logout]);

  const value = useMemo(() => ({
    user,
    loading,
    isAuthenticated: !!user,
    login,
    logout,
  }), [user, loading, login, logout]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuthContext() {
  return useContext(AuthContext);
}

export default AuthContext;

