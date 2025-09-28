"use client";

import AuthGuard from '@/components/common/AuthGuard';
import { useAuth } from '@/hooks/useAuth';
import ProfileDetails from '@/features/profile/components/ProfileDetails';

export default function ProfilePage() {
  const { user, roles = [] } = useAuth();
  return (
    <AuthGuard>
      <section className="space-y-6">
        <h1 className="text-2xl font-bold text-stone-900">Your Profile</h1>
        <ProfileDetails email={user?.email} roles={roles} />
      </section>
    </AuthGuard>
  );
}
