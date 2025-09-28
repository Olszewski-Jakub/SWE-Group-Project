import Card from '@/components/ui/Card';

export default function WelcomeCard({ name }) {
  return (
    <Card>
      <p className="text-stone-700">Welcome back{name ? `, ${name}` : ''}!</p>
    </Card>
  );
}
