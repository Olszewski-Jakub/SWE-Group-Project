import Link from 'next/link';
import Button from '@/components/ui/Button';

export default function HeroSection() {
  return (
    <section className="relative overflow-hidden rounded-2xl bg-gradient-to-b from-amber-50 to-white p-8 shadow-sm">
      <div className="space-y-6">
        <h1 className="text-4xl font-extrabold tracking-tight text-stone-900">StackOverFlowedCup</h1>
        <p className="max-w-prose text-stone-700">
          Artisanal roasts and crafted blends. This is a wireframe landing page to showcase our products and story.
        </p>
        <div className="flex flex-wrap gap-3">
          <Link href="/products"><Button>Shop Now</Button></Link>
          <Link href="/signin"><Button variant="secondary">Sign In</Button></Link>
        </div>
      </div>
      <div className="pointer-events-none absolute -right-12 -top-12 h-48 w-48 rounded-full bg-amber-200/50 blur-3xl"/>
      <div className="pointer-events-none absolute -bottom-16 -left-16 h-56 w-56 rounded-full bg-amber-100/60 blur-3xl"/>
    </section>
  );
}
