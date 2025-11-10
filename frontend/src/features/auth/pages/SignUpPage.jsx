"use client";

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';

function CoffeeIcon({ className = 'h-5 w-5' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M3 7h13a4 4 0 1 1 0 8h-1.05A6.5 6.5 0 0 1 8 20.5 6.5 6.5 0 0 1 1.5 14V9a2 2 0 0 1 2-2ZM19 9a2 2 0 0 0-2-2v6a2 2 0 0 0 2-2V9Z"/>
    </svg>
  );
}

export default function SignUpPage() {
  const router = useRouter();
  const { isAuthenticated, signUp } = useAuth();
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
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
      await signUp({ firstName, lastName, email, password });
      const qs = new URLSearchParams({ email }).toString();
      router.replace(`/verify-email?${qs}`);
    } catch (err) {
      const msg = err?.response?.data?.message || 'Sign up failed';
      setError(String(msg));
    } finally {
      setLoading(false);
    }
  };

  const passwordHint = 'Use 8+ chars with a mix of letters, numbers and symbols.';

  return (
    <div className="min-h-[60vh] grid place-items-center bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-amber-50 to-white">
      <Card className="w-full max-w-md">
        <div className="mb-4 flex items-center gap-2 text-amber-800">
          <span className="inline-flex h-8 w-8 items-center justify-center rounded-md bg-amber-700 text-white"><CoffeeIcon className="h-4 w-4"/></span>
          <h1 className="text-xl font-bold">Join StackOverFlowedCup</h1>
        </div>
        <p className="mb-6 text-sm text-stone-600">
          Already have an account?{' '}
          <Link href="/signin" className="font-medium text-amber-700 hover:text-amber-800">Sign in</Link>
        </p>
        <form onSubmit={onSubmit} className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input id="firstName" label="First name" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
            <Input id="lastName" label="Last name" value={lastName} onChange={(e) => setLastName(e.target.value)} required />
          </div>
          <Input id="email" label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          <Input id="password" label="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required hint={passwordHint} />
          {error ? <p className="text-sm text-red-600">{error}</p> : null}
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Creating account…' : 'Sign Up'}
          </Button>
        </form>
      </Card>
    </div>
  );
}

