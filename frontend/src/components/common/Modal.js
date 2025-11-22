"use client";

import { Fragment } from 'react';
import { createPortal } from 'react-dom';
import { classNames } from '../../utils/helpers';

export default function Modal({ open, onClose, children, size = '2xl', tall = false }) {
  if (typeof document === 'undefined') return null;
  if (!open) return null; // Unmount when closed to avoid visibility/pointer issues
  return createPortal(
    <Fragment>
      <div
        className={classNames('fixed inset-0 bg-black/30 opacity-100 pointer-events-auto transition-opacity z-40')}
        onClick={onClose}
      />
      <div className={classNames('fixed inset-0 z-50 flex items-center justify-center p-4 pointer-events-auto')}>
        <div
          role="dialog"
          aria-modal="true"
          className={classNames(
            'w-full rounded-lg bg-white p-6 shadow-xl ring-1 ring-black/5 transform transition-all duration-150 ease-out scale-100 overflow-auto',
            // width
            size === 'md' && 'max-w-md' ||
            size === 'lg' && 'max-w-lg' ||
            size === 'xl' && 'max-w-xl' ||
            size === '2xl' && 'max-w-2xl' ||
            size === '3xl' && 'max-w-3xl' ||
            size === '4xl' && 'max-w-4xl' ||
            size === 'screen' && 'max-w-[95vw]' ||
            'max-w-2xl',
            // height
            tall ? 'max-h-[92vh]' : 'max-h-[85vh]'
          )}
        >
          {children}
        </div>
      </div>
    </Fragment>,
    document.body
  );
}
