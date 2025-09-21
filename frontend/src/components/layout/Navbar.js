"use client";

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { classNames } from '../../utils/helpers';
import Button from '../ui/Button';
import { useAuth } from '../../hooks/useAuth';

const navLinks = [
  { href: '/', label: 'Home' },
  { href: '/dashboard', label: 'Dashboard' },
  { href: '/profile', label: 'Profile' },
];

export default function Navbar() {
  const pathname = usePathname();
  const router = useRouter();
  const { isAuthenticated, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    router.push('/login');
  };

  return (
    <nav className="sticky top-0 z-40 w-full border-b border-gray-200 bg-white/80 backdrop-blur">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-14 items-center justify-between">
          <div className="flex items-center gap-6">
            <Link href="/" className="text-lg font-semibold tracking-tight text-gray-900">
              Acme
            </Link>
            <div className="hidden md:flex items-center gap-2">
              {navLinks.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  prefetch
                  className={classNames(
                    'rounded px-3 py-2 text-sm font-medium',
                    pathname === item.href ? 'bg-gray-900 text-white' : 'text-gray-700 hover:bg-gray-100'
                  )}
                >
                  {item.label}
                </Link>
              ))}
            </div>
          </div>
          <div className="flex items-center gap-3">
            {isAuthenticated ? (
              <Button variant="secondary" onClick={handleLogout}>Logout</Button>
            ) : (
              <Link href="/login" prefetch>
                <Button>Login</Button>
              </Link>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}

