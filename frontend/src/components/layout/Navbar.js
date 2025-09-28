"use client";

import { useEffect, useRef, useState } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { classNames } from '../../utils/helpers';
import Button from '../ui/Button';
import { useAuth } from '../../hooks/useAuth';

function CoffeeIcon({ className = 'h-5 w-5' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M3 7h13a4 4 0 1 1 0 8h-1.05A6.5 6.5 0 0 1 8 20.5 6.5 6.5 0 0 1 1.5 14V9a2 2 0 0 1 2-2ZM19 9a2 2 0 0 0-2-2v6a2 2 0 0 0 2-2V9Z"/>
    </svg>
  );
}

export default function Navbar() {
  const pathname = usePathname();
  const router = useRouter();
  const { isAuthenticated, user, roles = [], signOut } = useAuth();
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);
  const linkRefs = useRef({});
  const indicatorRef = useRef(null);

  const handleLogout = async () => {
    await signOut();
    router.push('/signin');
  };

  const items = [
    { href: '/', label: 'Home', show: true },
    { href: '/products', label: 'Products', show: true },
    { href: '/contact', label: 'Contact', show: true },
    { href: '/dashboard', label: 'Dashboard', show: isAuthenticated },
    { href: '/admin', label: 'Admin', show: roles.includes('ADMIN') },
  ].filter((i) => i.show);

  useEffect(() => {
    const active = linkRefs.current[pathname];
    const container = containerRef.current;
    const indicator = indicatorRef.current;
    if (!active || !container || !indicator) return;
    const cRect = container.getBoundingClientRect();
    const aRect = active.getBoundingClientRect();
    const left = aRect.left - cRect.left + container.scrollLeft;
    indicator.style.transform = `translateX(${Math.max(0, left)}px)`;
    indicator.style.width = `${aRect.width}px`;
  }, [pathname, items.length]);

  useEffect(() => {
    const onResize = () => {
      // Recompute on resize for responsiveness
      const active = linkRefs.current[pathname];
      const container = containerRef.current;
      const indicator = indicatorRef.current;
      if (!active || !container || !indicator) return;
      const cRect = container.getBoundingClientRect();
      const aRect = active.getBoundingClientRect();
      const left = aRect.left - cRect.left + container.scrollLeft;
      indicator.style.transform = `translateX(${Math.max(0, left)}px)`;
      indicator.style.width = `${aRect.width}px`;
    };
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, [pathname]);

  return (
    <nav className="sticky top-0 z-40 w-full border-b border-stone-200 bg-white/70 backdrop-blur supports-[backdrop-filter]:bg-white/50">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          <div className="flex items-center gap-6">
            <Link href="/" className="group flex items-center gap-2 text-lg font-semibold tracking-tight text-stone-900">
              <span className="inline-flex h-8 w-8 items-center justify-center rounded-md bg-amber-700 text-white shadow-sm group-hover:bg-amber-800">
                <CoffeeIcon className="h-4 w-4" />
              </span>
              <span>Copper Cup</span>
            </Link>
            <div ref={containerRef} className="relative hidden md:flex items-center gap-1">
              {items.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  prefetch
                  className={classNames(
                    'relative rounded-md px-3 py-2 text-sm font-medium text-stone-700 hover:text-stone-900 hover:bg-stone-100 transition-colors',
                    pathname === item.href && 'text-stone-900'
                  )}
                  ref={(el) => { if (el) linkRefs.current[item.href] = el; }}
                >
                  {item.label}
                </Link>
              ))}
              <span
                ref={indicatorRef}
                className="pointer-events-none absolute bottom-0 h-0.5 rounded-full bg-amber-600 transition-[transform,width] duration-300 ease-out"
                style={{ width: 0, transform: 'translateX(0px)' }}
              />
            </div>
          </div>
          <div className="relative flex items-center gap-3">
            {isAuthenticated ? (
              <>
                <Link href="/profile" prefetch>
                  <Button variant="secondary">Profile</Button>
                </Link>
                <Button variant="ghost" onClick={handleLogout}>Logout</Button>
              </>
            ) : (
              <Link href="/signin" prefetch>
                <Button>Login</Button>
              </Link>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
