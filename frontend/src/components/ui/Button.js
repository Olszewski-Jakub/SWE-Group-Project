"use client";

import { forwardRef } from 'react';
import { classNames } from '../../utils/helpers';

const Button = forwardRef(function Button(
  { className, children, variant = 'primary', ...props },
  ref
) {
  const base = 'inline-flex items-center justify-center rounded px-4 py-2 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed';
  const variants = {
    primary: 'bg-indigo-600 text-white hover:bg-indigo-700 focus:ring-indigo-500',
    secondary: 'bg-gray-200 text-gray-900 hover:bg-gray-300 focus:ring-gray-400',
    ghost: 'bg-transparent text-indigo-600 hover:bg-indigo-50 focus:ring-indigo-500',
  };
  return (
    <button ref={ref} className={classNames(base, variants[variant], className)} {...props}>
      {children}
    </button>
  );
});

export default Button;

