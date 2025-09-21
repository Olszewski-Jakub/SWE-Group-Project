import Link from 'next/link';
import Button from '../components/ui/Button';

export default function HomePage() {
  return (
    <section className="space-y-6">
      <h1 className="text-2xl font-bold">Welcome to Acme</h1>
      <p className="text-gray-700 max-w-prose">
        This is a frontend-only Next.js app with a professional, scalable architecture and
        secure token-based authentication.
      </p>
      <div className="flex gap-3">
        <Link href="/dashboard"><Button>Go to Dashboard</Button></Link>
        <Link href="/login"><Button variant="secondary">Login</Button></Link>
      </div>
    </section>
  );
}
