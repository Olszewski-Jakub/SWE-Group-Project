"use client";

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { requestPasswordReset } from '@/lib/auth';

function MailIcon({ className = 'h-5 w-5' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M2 6a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v.35l-10 6.25L2 6.35V6Zm0 2.74V18a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8.74l-9.37 5.86a2 2 0 0 1-2.26 0L2 8.74Z"/>
    </svg>
  );
}

export default function ForgotPasswordPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [done, setDone] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!email) return;
    setLoading(true);
    try {
      const locale = (typeof navigator !== 'undefined' && navigator.language) ? navigator.language : 'en';
      await requestPasswordReset({ email, locale });
      setDone(true);
    } catch (err) {
      const msg = err?.response?.data?.message || 'Unable to process request. Please try again later.';
      setError(String(msg));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[60vh] grid place-items-center bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-amber-50 to-white">
      <Card className="w-full max-w-md">
        <div className="mb-4 flex items-center gap-2 text-amber-800">
          <span className="inline-flex h-8 w-8 items-center justify-center rounded-md bg-amber-700 text-white">
            <MailIcon className="h-4 w-4" />
          </span>
          <h1 className="text-xl font-bold">Forgot your password?</h1>
        </div>

        {done ? (
          <div className="space-y-4">
            <p className="text-stone-700">
              If an account exists for <span className="font-medium">{email}</span>, you will receive a password reset email shortly.
            </p>
            <Button className="w-full" onClick={() => router.replace('/signin')}>Return to Sign In</Button>
          </div>
        ) : (
          <form onSubmit={onSubmit} className="space-y-4">
            <Input
              id="email"
              label="Email address"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            {error ? <p className="text-sm text-red-600">{error}</p> : null}
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? 'Sending…' : 'Send reset link'}
            </Button>
          </form>
        )}
      </Card>
    </div>
  );
}

