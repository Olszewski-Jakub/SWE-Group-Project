"use client";

import Card from '@/components/ui/Card';

export default function RecentOrdersPlaceholder() {
  return (
    <Card title="Recent Orders" footer={<span>Order history coming soon.</span>}>
      <ul className="divide-y divide-stone-100">
        {[1, 2, 3].map((i) => (
          <li key={i} className="flex items-center gap-3 py-3">
            <div className="h-10 w-10 rounded-md bg-stone-100" />
            <div className="flex-1">
              <p className="text-sm font-medium text-stone-900">Order #{1000 + i}</p>
              <p className="text-xs text-stone-500">Placed on —</p>
            </div>
            <span className="text-xs font-medium text-stone-600">—</span>
          </li>
        ))}
      </ul>
    </Card>
  );
}

