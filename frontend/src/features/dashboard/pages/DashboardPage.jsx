"use client";

import { useAuth } from '@/hooks/useAuth';
import AuthGuard from '@/components/common/AuthGuard';
import WelcomeCard from '@/features/dashboard/components/WelcomeCard';
import ProtectedInfoCard from '@/features/dashboard/components/ProtectedInfoCard';

export default function DashboardPage() {
  const { user } = useAuth();
  return (
    <AuthGuard>
      <section className="space-y-4">
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <WelcomeCard name={user?.name} />
        <ProtectedInfoCard />
      </section>
    </AuthGuard>
  );
}
