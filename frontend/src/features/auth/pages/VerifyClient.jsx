"use client";

import { useState } from 'react';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Link from 'next/link';
import { verifyEmail } from '../../../lib/auth';

export default function VerifyClient({ token }) {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  const onVerify = async () => {
    setError('');
    setLoading(true);
    try {
      await verifyEmail(token);
      setSuccess(true);
    } catch (e) {
      const msg = e?.response?.data?.message || 'Verification failed';
      setError(String(msg));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[60vh] grid place-items-center bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-amber-50 to-white">
      <Card className="w-full max-w-md text-center">
        <h1 className="mb-2 text-2xl font-bold text-stone-900">Email verification</h1>
        {!success ? (
          <>
            <p className="mb-6 text-stone-700">Click the button below to verify your email.</p>
            {error ? <p className="mb-3 text-sm text-red-600">{error}</p> : null}
            <Button onClick={onVerify} disabled={loading} className="w-full">
              {loading ? 'Verifying…' : 'Verify Email'}
            </Button>
          </>
        ) : (
          <>
            <p className="mb-6 text-stone-700">Your email has been verified successfully.</p>
            <Link href="/signin" className="inline-flex w-full items-center justify-center rounded-md bg-amber-700 px-4 py-2 text-sm font-medium text-white hover:bg-amber-800">
              Continue to Sign In
            </Link>
          </>
        )}
      </Card>
    </div>
  );
}

