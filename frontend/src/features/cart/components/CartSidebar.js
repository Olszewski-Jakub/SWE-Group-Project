// src/features/cart/components/CartSidebar.js
"use client";

import { useCart } from '../CartContext';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';

export default function CartSidebar() {
  const { items, removeItem, clear } = useCart();

  if (!items.length) return <Card title="Your Cart">Cart is empty</Card>;

  return (
    <Card
      title={`Your Cart (${items.length})`}
      footer={<Button onClick={clear} variant="secondary">Clear Cart</Button>}
    >
      <ul className="space-y-2">
        {items.map((item) => (
          <li key={item.id} className="flex justify-between items-center">
            <span>{item.name} x {item.quantity}</span>
            <button className="text-red-600 hover:underline" onClick={() => removeItem(item.id)}>Remove</button>
          </li>
        ))}
      </ul>
    </Card>
  );
}
