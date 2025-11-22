"use client";

import RoleGuard from '@/components/common/RoleGuard';
import { useEffect, useMemo, useState } from 'react';
import AdminLayout from '@/features/admin/components/AdminLayout';
import ProductManagementPanel from '@/features/admin/components/ProductManagementPanel';
import UserManagementPanel from '@/features/admin/components/UserManagementPanel';
import AnalyticsPanel from '@/features/admin/components/AnalyticsPanel';

export default function AdminPage() {
  const items = [
    { key: 'products', label: 'Products' },
    { key: 'users', label: 'Users' },
    { key: 'analytics', label: 'Analytics' },
  ];
  const validKeys = useMemo(() => items.map(i => i.key), []);
  const [active, setActive] = useState('products');

  useEffect(() => {
    const fromHash = () => {
      if (typeof window === 'undefined') return;
      const h = (window.location.hash || '').replace(/^#/, '');
      if (h && validKeys.includes(h)) setActive(h);
    };
    fromHash();
    window.addEventListener('hashchange', fromHash);
    return () => window.removeEventListener('hashchange', fromHash);
  }, [validKeys]);

  useEffect(() => {
    if (typeof window === 'undefined') return;
    const h = (window.location.hash || '').replace(/^#/, '');
    if (active && h !== active) {
      const url = `${window.location.pathname}#${active}`;
      window.history.replaceState(null, '', url);
    }
  }, [active]);

  return (
    <RoleGuard requireRoles={["ADMIN", "MANAGER"]}>
      <AdminLayout items={items} activeKey={active} onSelect={setActive}>
        {active === 'products' && <ProductManagementPanel />}
        {active === 'users' && <UserManagementPanel />}
        {active === 'analytics' && <AnalyticsPanel />}
      </AdminLayout>
    </RoleGuard>
  );
}
