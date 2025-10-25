import ContactForm from '../components/ContactForm';
import StoreInfo from '../components/StoreInfo';

export default function ContactPage() {
  return (
    // Set a light background for the whole page
    <section className="bg-white p-8 sm:p-12">
      <div className="mx-auto max-w-7xl">
        {/* Page Header */}
        <header className="mb-12 text-center">
          <h1 className="text-4xl font-bold tracking-tight text-[#7B542F] sm:text-5xl">
            Get In Touch
          </h1>
          <p className="mt-4 text-lg text-[#B6771D]">
            We'd love to hear from you. Drop us a line or visit us in store.
          </p>
        </header>

        {/* Main content grid */}
        <div className="grid grid-cols-1 gap-12 lg:grid-cols-2">
          {/* Column 1: Contact Form */}
          <ContactForm />
          {/* Column 2: Store Info */}
          <StoreInfo />
        </div>
      </div>
    </section>
  );
}

