"use client";

import { useMemo, useState } from 'react';
import AuthGuard from '@/components/common/AuthGuard';
import { useAuth } from '@/hooks/useAuth';
import Card from '@/components/ui/Card';
import ProfileDetails from '@/features/profile/components/ProfileDetails';
import SideNav from '@/features/profile/components/SideNav';
import ProfileForm from '@/features/profile/components/ProfileForm';
import Avatar from '@/components/ui/Avatar';
import RecentOrders from '@/features/profile/components/RecentOrdersPlaceholder';

const NAV_ITEMS = [
  { key: 'overview', label: 'Overview' },
  { key: 'profile', label: 'Profile' },
  { key: 'orders', label: 'Orders' },
  { key: 'security', label: 'Security' },
];

export default function ProfilePage() {
  const { user, roles = [] } = useAuth();
  const [active, setActive] = useState('overview');

  const content = useMemo(() => {
    switch (active) {
      case 'overview':
        return (
          <div className="space-y-4">
            {/* Profile header */}
            <div className="relative overflow-hidden rounded-2xl border border-stone-200 bg-gradient-to-br from-amber-50 to-stone-50 p-6 shadow-sm">
              <div className="flex items-center gap-4">
                <Avatar name={user?.name || user?.email} size="lg" />
                <div>
                  <p className="text-lg font-semibold text-stone-900">{user?.name || 'Your Account'}</p>
                  <p className="text-sm text-stone-600">{user?.email || '—'}</p>
                  <div className="mt-2 flex flex-wrap gap-2">
                    {(roles || []).map((r) => (
                      <span key={r} className="inline-flex items-center rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-medium text-amber-800 ring-1 ring-inset ring-amber-200">{r}</span>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            {/* Overview grid */}
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <Card title="Account Details">
                <ProfileDetails email={user?.email} roles={roles} />
              </Card>
              <RecentOrders />
            </div>

            <Card title="Quick Actions">
              <div className="flex flex-wrap gap-3">
                <button onClick={() => setActive('profile')} className="rounded-md bg-amber-700 px-3 py-2 text-sm font-medium text-white shadow-sm hover:bg-amber-800 focus:outline-none focus:ring-2 focus:ring-amber-500">Edit profile</button>
                <button onClick={() => setActive('orders')} className="rounded-md bg-stone-200 px-3 py-2 text-sm font-medium text-stone-900 shadow-sm hover:bg-stone-300 focus:outline-none focus:ring-2 focus:ring-stone-400">View orders</button>
              </div>
            </Card>
          </div>
        );
      case 'profile':
        return <ProfileForm user={user} />;
      case 'orders':
        return (
          <Card title="Order History">
            <p className="text-sm text-stone-600">Coming soon.</p>
          </Card>
        );
      case 'security':
        return (
          <Card title="Security Settings">
            <p className="text-sm text-stone-600">Coming soon.</p>
          </Card>
        );
      default:
        return null;
    }
  }, [active, roles, user]);

  return (
    <AuthGuard>
      <section className="space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-stone-900">Your Account</h1>
        </div>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
          <aside className="lg:col-span-3">
            <SideNav items={NAV_ITEMS} active={active} onChange={setActive} />
          </aside>
          <main className="lg:col-span-9 space-y-6">{content}</main>
        </div>
      </section>
    </AuthGuard>
  );
}
