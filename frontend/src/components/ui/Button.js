"use client";

import { forwardRef } from 'react';
import { classNames } from '../../utils/helpers';

const Button = forwardRef(function Button(
  { className, children, variant = 'primary', ...props },
  ref
) {
  const base = 'inline-flex items-center justify-center rounded-md px-4 py-2 text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed shadow-sm';
  const variants = {
    primary: 'bg-amber-700 text-white hover:bg-amber-800 focus:ring-amber-500',
    secondary: 'bg-stone-200 text-stone-900 hover:bg-stone-300 focus:ring-stone-400',
    ghost: 'bg-transparent text-amber-700 hover:bg-amber-50 focus:ring-amber-500',
  };
  return (
    <button ref={ref} className={classNames(base, variants[variant], className)} {...props}>
      {children}
    </button>
  );
});

export default Button;
