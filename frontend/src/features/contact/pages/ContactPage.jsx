import ContactFormWireframe from '@/features/contact/components/ContactFormWireframe';
import StoreInfoWireframe from '@/features/contact/components/StoreInfoWireframe';

export default function ContactPage() {
  return (
    <section className="space-y-8">
      <header className="space-y-3">
        <h1 className="text-2xl font-bold text-stone-900">Contact</h1>
        <p className="text-stone-600">Wireframe — contact form and store info.</p>
      </header>
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <ContactFormWireframe />
        <StoreInfoWireframe />
      </div>
    </section>
  );
}
