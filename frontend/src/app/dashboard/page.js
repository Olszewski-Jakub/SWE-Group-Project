"use client";

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../../hooks/useAuth';

export default function DashboardPage() {
  const { user, isAuthenticated, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.replace('/login');
    }
  }, [isAuthenticated, loading, router]);

  if (loading) return <p>Loading...</p>;
  if (!isAuthenticated) return null;

  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-bold">Dashboard</h1>
      <p className="text-gray-700">Welcome back{user?.name ? `, ${user.name}` : ''}!</p>
      <div className="rounded border border-gray-200 bg-white p-4 shadow-sm">
        <p className="text-sm text-gray-600">Protected content fetched from the API would go here.</p>
      </div>
    </section>
  );
}

