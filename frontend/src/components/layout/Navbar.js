"use client";

import { useEffect, useRef, useState } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { classNames } from '@/utils/helpers';
import Button from '../ui/Button';
import { useAuth } from '@/hooks/useAuth';

function CoffeeIcon({ className = 'h-5 w-5' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M3 7h13a4 4 0 1 1 0 8h-1.05A6.5 6.5 0 0 1 8 20.5 6.5 6.5 0 0 1 1.5 14V9a2 2 0 0 1 2-2ZM19 9a2 2 0 0 0-2-2v6a2 2 0 0 0 2-2V9Z"/>
    </svg>
  );
}

function HomeIcon({ className = 'h-4 w-4' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M3 10l9-7 9 7" />
      <path d="M9 21v-6h6v6" />
      <path d="M4 21h16" />
    </svg>
  );
}

function BoxIcon({ className = 'h-4 w-4' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M12 3l8 4-8 4-8-4 8-4z" />
      <path d="M20 7v6l-8 4v-6l8-4z" />
      <path d="M4 7v6l8 4" />
    </svg>
  );
}

function MailIcon({ className = 'h-4 w-4' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <rect x="3" y="5" width="18" height="14" rx="2" />
      <path d="M3 7l9 6 9-6" />
    </svg>
  );
}

function GaugeIcon({ className = 'h-4 w-4' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M21 16a9 9 0 1 0-18 0" />
      <path d="M12 12l5-3" />
    </svg>
  );
}

function ShieldIcon({ className = 'h-4 w-4' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M12 3l8 4v6c0 4.5-3.5 7.5-8 8-4.5-.5-8-3.5-8-8V7l8-4z" />
    </svg>
  );
}

function UserIcon({ className = 'h-4 w-4' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <circle cx="12" cy="7" r="4" />
      <path d="M4 21a8 8 0 0 1 16 0" />
    </svg>
  );
}

function LogInIcon({ className = 'h-4 w-4' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
      <path d="M10 17l5-5-5-5" />
      <path d="M15 12H3" />
    </svg>
  );
}

function LogOutIcon({ className = 'h-4 w-4' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
      <path d="M16 17l5-5-5-5" />
      <path d="M21 12H9" />
    </svg>
  );
}

export default function Navbar() {
  const pathname = usePathname();
  const router = useRouter();
  const { isAuthenticated, user, roles = [], signOut } = useAuth();
  const [open, setOpen] = useState(false);
  const [menuAnim, setMenuAnim] = useState(false);
  const containerRef = useRef(null);
  const linkRefs = useRef({});
  const indicatorRef = useRef(null);

  const handleLogout = async () => {
    await signOut();
    router.push('/signin');
  };

  const items = [
    { href: '/', label: 'Home', icon: HomeIcon, show: true },
    { href: '/products', label: 'Products', icon: BoxIcon, show: true },
    { href: '/contact', label: 'Contact', icon: MailIcon, show: true },
    { href: '/dashboard', label: 'Dashboard', icon: GaugeIcon, show: isAuthenticated },
    { href: '/admin', label: 'Admin', icon: ShieldIcon, show: roles.includes('ADMIN') },
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

      // Close mobile menu on upsize to md and above
      if (window.matchMedia('(min-width: 768px)').matches && open) {
        setOpen(false);
      }
    };
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, [pathname, open]);

  // Close mobile menu on route change
  useEffect(() => {
    if (open) setOpen(false);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pathname]);

  // Animate mobile menu when opening
  useEffect(() => {
    if (open) {
      const id = requestAnimationFrame(() => setMenuAnim(true));
      return () => cancelAnimationFrame(id);
    }
    setMenuAnim(false);
  }, [open]);

  return (
    <nav className="sticky top-0 z-40 w-full border-b border-stone-200/70 bg-white/60 backdrop-blur-xl supports-[backdrop-filter]:bg-white/40 shadow-sm">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          <div className="flex items-center gap-6">
            <Link href="/" className="group flex items-center gap-2 text-lg font-semibold tracking-tight text-stone-900">
              <span className="inline-flex h-8 w-8 items-center justify-center rounded-md bg-gradient-to-br from-amber-600 to-orange-500 text-white shadow-sm ring-1 ring-black/5 transition-colors group-hover:from-amber-700 group-hover:to-orange-600">
                <CoffeeIcon className="h-4 w-4" />
              </span>
              <span className="hidden sm:inline">StackOverFlowedCup</span>
            </Link>
            <div ref={containerRef} className="relative hidden md:flex items-center gap-1">
              {items.map((item) => {
                const Icon = item.icon;
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    prefetch
                    className={classNames(
                      'group relative inline-flex items-center gap-2 rounded-full px-3 py-2 text-sm font-medium text-stone-700 hover:text-stone-900 hover:bg-stone-100/80 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-amber-600 transition-colors',
                      pathname === item.href && 'text-stone-900'
                    )}
                    ref={(el) => { if (el) linkRefs.current[item.href] = el; }}
                  >
                    <Icon className="h-4 w-4 opacity-80 group-hover:opacity-100" />
                    <span>{item.label}</span>
                  </Link>
                );
              })}
              <span
                ref={indicatorRef}
                className="pointer-events-none absolute -bottom-px h-1 rounded-full bg-gradient-to-r from-amber-500 to-orange-500 shadow-[0_0_0_1px_rgba(0,0,0,0.05)] transition-[transform,width] duration-300 ease-out"
                style={{ width: 0, transform: 'translateX(0px)' }}
              />
            </div>
          </div>
          <div className="relative flex items-center gap-3">
            {/* Mobile hamburger toggle */}
            <button
              type="button"
              aria-label="Toggle menu"
              aria-expanded={open}
              className="inline-flex items-center justify-center rounded-lg p-2 text-stone-700 hover:bg-stone-100/80 focus:outline-none focus:ring-2 focus:ring-amber-600 md:hidden"
              onClick={() => setOpen((v) => !v)}
            >
              {/* Hamburger / Close icons */}
              {open ? (
                // X icon
                <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
                  <path d="M18 6 6 18M6 6l12 12" />
                </svg>
              ) : (
                // Hamburger icon
                <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
                  <path d="M3 6h18M3 12h18M3 18h18" />
                </svg>
              )}
            </button>
            <div className="hidden md:flex items-center gap-3">
              {isAuthenticated ? (
                <>
                  <Link href="/profile" prefetch>
                    <Button variant="primary" className="rounded-full shadow-md shadow-amber-200/40">
                      <UserIcon className="mr-2" />
                      Profile
                    </Button>
                  </Link>
                  <Button
                    variant="ghost"
                    className="rounded-full ring-1 ring-amber-600/20 hover:ring-amber-600/30"
                    onClick={handleLogout}
                  >
                    <LogOutIcon className="mr-2" />
                    Logout
                  </Button>
                </>
              ) : (
                <Link href="/signin" prefetch>
                  <Button className="rounded-full shadow-md shadow-amber-200/40">
                    <LogInIcon className="mr-2" />
                    Login
                  </Button>
                </Link>
              )}
            </div>
          </div>
        </div>
        {/* Mobile full-width overlay menu */}
        {open && (
          <div className="md:hidden">
            {/* Backdrop overlay (below panel) */}
            <button
              type="button"
              aria-label="Close menu backdrop"
              onClick={() => setOpen(false)}
              className={classNames(
                'fixed inset-x-0 top-16 bottom-0 z-40 bg-stone-950/10 backdrop-blur-0 transition-all duration-300 ease-out',
                menuAnim && 'bg-stone-950/20 backdrop-blur-sm'
              )}
            />

            {/* Sliding panel */}
            <div className="fixed inset-x-0 top-16 z-50">
              <div
                className={classNames(
                  'overflow-hidden rounded-none ring-1 ring-black/5 bg-white/95 backdrop-blur-xl shadow-2xl transition-all duration-300 ease-out',
                  'origin-top transform',
                  menuAnim ? 'opacity-100 translate-y-0 scale-100' : 'opacity-0 -translate-y-2 scale-[0.98]'
                )}
              >
                <div className="flex flex-col p-1">
                  {items.map((item) => {
                    const Icon = item.icon;
                    return (
                      <Link
                        key={item.href}
                        href={item.href}
                        prefetch
                        className={classNames(
                          'inline-flex items-center gap-2 px-4 py-3 text-base font-medium text-stone-800 hover:bg-stone-100/80',
                          pathname === item.href && 'text-stone-900'
                        )}
                      >
                        <Icon className="h-5 w-5 opacity-80" />
                        <span>{item.label}</span>
                      </Link>
                    );
                  })}
                </div>
                <div className="border-t border-stone-200/80" />
                <div className="px-4 py-3">
                  {isAuthenticated ? (
                    <div className="flex items-center gap-2">
                      <Link href="/profile" prefetch>
                        <Button
                          variant="primary"
                          className="shrink-0 rounded-full shadow-md shadow-amber-200/40"
                        >
                          <UserIcon className="mr-2" />
                          Profile
                        </Button>
                      </Link>
                      <Button
                        variant="ghost"
                        className="shrink-0 rounded-full ring-1 ring-amber-600/20 hover:ring-amber-600/30"
                        onClick={handleLogout}
                      >
                        <LogOutIcon className="mr-2" />
                        Logout
                      </Button>
                    </div>
                  ) : (
                    <Link href="/signin" prefetch>
                      <Button className="shrink-0 rounded-full shadow-md shadow-amber-200/40">
                        <LogInIcon className="mr-2" />
                        Login
                      </Button>
                    </Link>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </nav>
  );
}
