"use client";

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import Input from '@/components/ui/Input';
import Link from 'next/link';
import Button from '@/components/ui/Button';

export default function SignInForm() {
  const router = useRouter();
  const { isAuthenticated, signIn } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isAuthenticated) router.replace('/');
  }, [isAuthenticated, router]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await signIn({ email, password });
      router.replace('/');
    } catch (err) {
      const msg = err?.response?.data?.message || 'Invalid credentials';
      setError(String(msg));
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={onSubmit} className="space-y-4">
      <Input id="email" label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
      <Input id="password" label="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
      <div className="text-right">
        <Link href="/forgot-password" className="text-sm font-medium text-amber-700 hover:text-amber-800">Forgot password?</Link>
      </div>
      {error ? <p className="text-sm text-red-600">{error}</p> : null}
      <Button type="submit" className="w-full" disabled={loading}>
        {loading ? 'Signing in…' : 'Sign In'}
      </Button>
    </form>
  );
}
