"use client";

import { useEffect, useRef, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';

function XIcon({ className = 'h-6 w-6' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M18.3 5.7 12 12l6.3 6.3-1.4 1.4L10.6 13.4 4.3 19.7 2.9 18.3 9.2 12 2.9 5.7 4.3 4.3 10.6 10.6 16.9 4.3z" />
    </svg>
  );
}

export default function CancelPage() {
  const router = useRouter();
  const [seconds, setSeconds] = useState(10);
  const [barToZero, setBarToZero] = useState(false);
  const [enter, setEnter] = useState(false);
  const blob1Ref = useRef(null);
  const blob2Ref = useRef(null);

  useEffect(() => {
    const t1 = setTimeout(() => setBarToZero(true), 50);
    const t2 = setTimeout(() => setEnter(true), 20);
    return () => { clearTimeout(t1); clearTimeout(t2); };
  }, []);

  useEffect(() => {
    let cancelled = false;
    const interval = setInterval(() => {
      setSeconds((s) => (s > 0 ? s - 1 : 0));
    }, 1000);
    const timeout = setTimeout(() => {
      if (!cancelled) router.replace('/');
    }, 10000);
    return () => {
      cancelled = true;
      clearInterval(interval);
      clearTimeout(timeout);
    };
  }, [router]);

  // Subtle mouse-based parallax on desktop without enabling scroll
  useEffect(() => {
    if (typeof window === 'undefined') return;
    const mq = window.matchMedia('(min-width: 640px)');
    let raf = 0;

    const onMove = (e) => {
      if (!mq.matches) return;
      const x = e.clientX / window.innerWidth - 0.5; // -0.5..0.5
      const y = e.clientY / window.innerHeight - 0.5;
      const b1 = blob1Ref.current;
      const b2 = blob2Ref.current;
      cancelAnimationFrame(raf);
      raf = requestAnimationFrame(() => {
        if (b1) b1.style.transform = `translate3d(${x * 16}px, ${y * 10}px, 0)`;
        if (b2) b2.style.transform = `translate3d(${x * -22}px, ${y * -14}px, 0)`;
      });
    };

    window.addEventListener('mousemove', onMove);
    return () => {
      window.removeEventListener('mousemove', onMove);
      cancelAnimationFrame(raf);
    };
  }, []);

  return (
    <div className="relative isolate h-auto overflow-auto sm:fixed sm:inset-x-0 sm:top-16 sm:bottom-0 sm:overflow-hidden bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-amber-50 to-white">
      {/* Animated background blobs */}
      <div ref={blob1Ref} className="pointer-events-none absolute -left-28 top-10 -z-10 h-72 w-72 rounded-full bg-stone-200/50 blur-3xl animate-pulse will-change-transform" />
      <div ref={blob2Ref} className="pointer-events-none absolute -right-32 bottom-0 -z-10 h-96 w-96 rounded-full bg-stone-300/50 blur-3xl animate-pulse will-change-transform" />

      <div className="mx-auto grid h-full max-w-7xl place-items-center p-4 sm:p-6 lg:p-8">
        <Card
          className={`w-full max-w-2xl transform transition-all duration-500 ease-out ${enter ? 'translate-y-0 opacity-100' : 'translate-y-3 opacity-0'}`}
        >
          <div className="mb-6 flex items-center gap-3 text-stone-800">
            <span className="inline-flex h-12 w-12 items-center justify-center rounded-xl bg-stone-200 text-stone-900 shadow-sm ring-4 ring-stone-900/5">
              <XIcon className="h-6 w-6" />
            </span>
            <div>
              <h1 className="text-2xl font-bold leading-tight">Payment cancelled</h1>
              <p className="text-sm text-stone-600">No worries — nothing was charged</p>
            </div>
          </div>

          <p className="mb-6 text-stone-700">
            Your checkout was cancelled before completion. You can browse products and try again, or head back to the homepage.
          </p>

          <div className="mb-6 grid gap-3 sm:grid-cols-2">
            <div className="rounded-lg border border-stone-200 bg-white p-4">
              <h3 className="mb-1 text-sm font-semibold text-stone-900">Troubleshooting</h3>
              <ul className="space-y-1 text-sm text-stone-700">
                <li>• Check your card details and balance</li>
                <li>• Try a different payment method</li>
                <li>• Contact support if the issue persists</li>
              </ul>
            </div>
            <div className="rounded-lg border border-amber-100 bg-amber-50 p-4">
              <h3 className="mb-1 text-sm font-semibold text-amber-900">Prefer help?</h3>
              <p className="text-sm text-amber-900/80">We’re happy to assist with your order.</p>
              <Link href="/contact" className="mt-2 inline-block text-sm font-medium text-amber-700 hover:text-amber-800">Contact support →</Link>
            </div>
          </div>

          <div className="mb-4" aria-live="polite">
            <div className="mb-2 flex items-baseline justify-between text-sm">
              <span className="text-stone-600">Redirecting to home</span>
              <span className="font-medium text-stone-900">{seconds}s</span>
            </div>
            <div className="h-2 w-full overflow-hidden rounded-full bg-stone-200">
              <div
                className={`h-full bg-stone-700 transition-[width] duration-[10000ms] ease-linear ${barToZero ? 'w-0' : 'w-full'}`}
              />
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <Link href="/products">
              <Button>Browse products</Button>
            </Link>
            <Link href="/">
              <Button variant="secondary">Back to home</Button>
            </Link>
          </div>
        </Card>
      </div>
    </div>
  );
}
