"use client";

import Link from 'next/link';
import Card from '@/components/ui/Card';
import SignInForm from '@/features/auth/components/SignInForm';
import ProviderButtons from '@/features/auth/components/ProviderButtons';

function CoffeeIcon({ className = 'h-5 w-5' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M3 7h13a4 4 0 1 1 0 8h-1.05A6.5 6.5 0 0 1 8 20.5 6.5 6.5 0 0 1 1.5 14V9a2 2 0 0 1 2-2ZM19 9a2 2 0 0 0-2-2v6a2 2 0 0 0 2-2V9Z"/>
    </svg>
  );
}

export default function SignInPage() {
  return (
    <div className="min-h-[60vh] grid place-items-center bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-amber-50 to-white">
      <Card className="w-full max-w-md">
        <div className="mb-4 flex items-center gap-2 text-amber-800">
          <span className="inline-flex h-8 w-8 items-center justify-center rounded-md bg-amber-700 text-white"><CoffeeIcon className="h-4 w-4"/></span>
          <h1 className="text-xl font-bold">Welcome back to StackOverFlowedCup</h1>
        </div>
        <p className="mb-6 text-sm text-stone-600">
          Don&apos;t have an account?{' '}
          <Link href="/signup" className="font-medium text-amber-700 hover:text-amber-800">Sign up</Link>
        </p>
        <SignInForm />
        <ProviderButtons />
      </Card>
    </div>
  );
}
