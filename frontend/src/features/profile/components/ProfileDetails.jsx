import Card from '@/components/ui/Card';

export default function ProfileDetails({ email, roles = [] }) {
  return (
    <Card>
      <div className="space-y-2 text-stone-700">
        <p><span className="font-medium text-stone-900">Email:</span> {email || '—'}</p>
        <p><span className="font-medium text-stone-900">Roles:</span> {roles.length ? roles.join(', ') : '—'}</p>
      </div>
    </Card>
  );
}
