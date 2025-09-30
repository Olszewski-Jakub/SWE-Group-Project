"use client";

import { useEffect, useMemo, useState } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { resetPassword } from '@/lib/auth';

function KeyIcon({ className = 'h-5 w-5' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M14 3a7 7 0 1 0 6.32 10H23v3h-3v3h-3v3h-3v-4.68A7 7 0 0 0 14 3Zm0 3a4 4 0 1 1 0 8 4 4 0 0 1 0-8Z"/>
    </svg>
  );
}

export default function ResetPasswordPage() {
  const router = useRouter();
  const params = useSearchParams();
  const token = useMemo(() => params?.get('token') || '', [params]);

  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  useEffect(() => {
    if (!token) {
      setError('Missing or invalid reset token. Please use the link from your email.');
    }
  }, [token]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!token) return;
    if (!password || password.length < 10) {
      setError('Password must be at least 10 characters.');
      return;
    }
    if (password !== confirm) {
      setError('Passwords do not match.');
      return;
    }
    setLoading(true);
    try {
      await resetPassword({ token, password });
      setDone(true);
    } catch (err) {
      const msg = err?.response?.data?.message || 'Unable to reset password. The link may be invalid or expired.';
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
            <KeyIcon className="h-4 w-4" />
          </span>
          <h1 className="text-xl font-bold">Reset your password</h1>
        </div>

        {done ? (
          <div className="space-y-4">
            <p className="text-stone-700">Your password has been reset successfully.</p>
            <Button className="w-full" onClick={() => router.replace('/signin')}>Go to Sign In</Button>
          </div>
        ) : (
          <form onSubmit={onSubmit} className="space-y-4">
            <Input
              id="password"
              label="New password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              hint="Use at least 10 characters."
            />
            <Input
              id="confirm"
              label="Confirm new password"
              type="password"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              required
            />
            {error ? <p className="text-sm text-red-600">{error}</p> : null}
            <Button type="submit" className="w-full" disabled={loading || !token}>
              {loading ? 'Resetting…' : 'Reset Password'}
            </Button>
          </form>
        )}
      </Card>
    </div>
  );
}

