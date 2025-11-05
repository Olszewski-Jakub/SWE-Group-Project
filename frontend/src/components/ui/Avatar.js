"use client";

import { classNames } from '@/utils/helpers';

function getInitials(nameOrEmail) {
  if (!nameOrEmail) return '?';
  const s = String(nameOrEmail);
  if (s.includes('@')) return s[0]?.toUpperCase() || '?';
  const parts = s.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 1) return parts[0][0]?.toUpperCase() || '?';
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

export default function Avatar({ src, name, size = 'md', className }) {
  const sizes = {
    sm: 'h-8 w-8 text-xs',
    md: 'h-10 w-10 text-sm',
    lg: 'h-14 w-14 text-base',
    xl: 'h-20 w-20 text-lg',
  };
  if (src) {
    return (
      // eslint-disable-next-line @next/next/no-img-element
      <img
        alt={name || 'Avatar'}
        src={src}
        className={classNames('rounded-full object-cover ring-2 ring-white shadow-sm', sizes[size], className)}
      />
    );
  }
  const initials = getInitials(name);
  return (
    <div
      className={classNames(
        'flex items-center justify-center rounded-full bg-amber-600 text-white ring-2 ring-white shadow-sm',
        sizes[size],
        className
      )}
      aria-label={name || 'Avatar'}
    >
      {initials}
    </div>
  );
}

