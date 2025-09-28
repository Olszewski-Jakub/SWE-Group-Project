"use client";

import RoleGuard from '@/components/common/RoleGuard';
import AdminInfoCard from '@/features/dashboard/components/AdminInfoCard';

export default function AdminPage() {
  return (
    <RoleGuard requireRoles={["ADMIN"]}>
      <section className="space-y-4">
        <h1 className="text-2xl font-bold">Admin</h1>
        <AdminInfoCard />
      </section>
    </RoleGuard>
  );
}
