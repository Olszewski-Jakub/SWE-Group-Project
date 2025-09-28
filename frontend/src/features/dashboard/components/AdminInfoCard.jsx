import Card from '@/components/ui/Card';

export default function AdminInfoCard() {
  return (
    <Card>
      <p className="text-sm text-stone-600">This page is visible to ADMIN users only.</p>
    </Card>
  );
}

