// A helper component for styled form inputs
function FormInput({ id, label, type = 'text', placeholder }) {
  return (
    <div>
      <label htmlFor={id} className="block text-sm font-semibold text-[#7B542F]">
        {label}
      </label>
      <div className="mt-1">
        <input
          type={type}
          name={id}
          id={id}
          placeholder={placeholder}
          className="block w-full rounded-md border-gray-300 shadow-sm focus:border-[#FF9D00] focus:ring-[#FF9D00] sm:text-sm"
        />
      </div>
    </div>
  );
}

// The main contact form
export default function ContactForm() {
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm sm:p-8">
      <h2 className="mb-6 text-2xl font-bold text-[#7B542F]">Send Us a Message</h2>
      
      <form action="#" method="POST" className="space-y-6">
        <FormInput id="name" label="Full Name" placeholder="Jane Doe" />
        <FormInput id="email" label="Email" type="email" placeholder="you@example.com" />
        
        {/* Textarea for the message */}
        <div>
          <label htmlFor="message" className="block text-sm font-semibold text-[#7B542F]">
            Message
          </label>
          <div className="mt-1">
            <textarea
              id="message"
              name="message"
              rows={4}
              placeholder="Your message..."
              className="block w-full rounded-md border-gray-300 shadow-sm focus:border-[#FF9D00] focus:ring-[#FF9D00] sm:text-sm"
            />
          </div>
        </div>

        {/* Submit Button */}
        <div>
          <button
            type="submit"
            className="w-full rounded-md bg-[#FF9D00] px-5 py-3 text-sm font-semibold text-white shadow-sm transition-all duration-200 hover:bg-[#B6771D] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#FF9D00]"
          >
            Send Message
          </button>
        </div>
      </form>
    </div>
  );
}
