"use client";

import Link from 'next/link';
import Card from '@/components/ui/Card';

export default function VerifyEmailPage({ email }) {
  return (
    <div className="min-h-[60vh] grid place-items-center bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-amber-50 to-white">
      <Card className="w-full max-w-md text-center">
        <h1 className="mb-2 text-2xl font-bold text-stone-900">Verify your email</h1>
        <p className="mb-6 text-stone-700">
          We sent a verification link to{` `}
          <span className="font-medium text-stone-900">{email || 'your inbox'}</span>.
          Please check your email to activate your account.
        </p>
        <div className="space-y-3">
          <p className="text-sm text-stone-600">Didn&apos;t receive it? Check your spam folder or try again later.</p>
          <Link className="inline-flex items-center justify-center rounded-md bg-amber-700 px-4 py-2 text-sm font-medium text-white hover:bg-amber-800" href="/signin">
            Back to Sign In
          </Link>
        </div>
      </Card>
    </div>
  );
}

