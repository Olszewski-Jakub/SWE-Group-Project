"use client";

import AuthGuard from './AuthGuard';
import { useAuth } from '../../hooks/useAuth';

export default function RoleGuard({ requireRoles = [], fallback = null, children }) {
  return (
    <AuthGuard>
      <RoleOnly requireRoles={requireRoles} fallback={fallback}>
        {children}
      </RoleOnly>
    </AuthGuard>
  );
}

function RoleOnly({ requireRoles = [], fallback = null, children }) {
  const { roles = [] } = useAuth();
  const hasRoles = requireRoles.every((r) => roles.includes(r));
  if (!hasRoles) {
    return fallback || (
      <div className="mx-auto max-w-2xl rounded border border-yellow-200 bg-yellow-50 p-6 text-yellow-900">
        <h2 className="mb-2 text-lg font-semibold">403 — Forbidden</h2>
        <p>You don&apos;t have permission to view this page.</p>
      </div>
    );
  }
  return children;
}

