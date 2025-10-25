// A helper component for each info block
function InfoBlock({ title, children }) {
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm sm:p-8">
      <h2 className="mb-4 text-2xl font-bold text-[#7B542F]">{title}</h2>
      <div className="space-y-3 text-base text-[#7B542F]/90">
        {children}
      </div>
    </div>
  );
}

// A helper for icon list items
function InfoItem({ icon, text }) {
  return (
    <div className="flex items-center">
      <div className="flex-shrink-0">
        {/* Heroicon (outline) */}
        <svg className="h-6 w-6 text-[#B6771D]" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
          {icon}
        </svg>
      </div>
      <div className="ml-3">
        {text}
      </div>
    </div>
  );
}

// The main store info component
export default function StoreInfo() {
  return (
    <div className="space-y-12">
      {/* Location Block */}
      <InfoBlock title="Visit Our Café">
        <InfoItem 
          icon={<><path strokeLinecap="round" strokeLinejoin="round" d="M15 10.5a3 3 0 11-6 0 3 3 0 016 0z" /><path strokeLinecap="round" strokeLinejoin="round" d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 1115 0z" /></>}
          text="123 Coffee Lane, Galway, Ireland" 
        />
        <InfoItem 
          icon={<path strokeLinecap="round" strokeLinejoin="round" d="M2.25 6.75c0 8.284 6.716 15 15 15h2.25a2.25 2.25 0 002.25-2.25v-1.372c0-.516-.351-.966-.852-1.091l-4.423-1.106c-.44-.11-.902.055-1.173.417l-.97 1.293c-.282.376-.769.542-1.21.38a12.035 12.035 0 01-7.143-7.143c-.162-.441.004-.928.38-1.21l1.293-.97c.363-.271.527-.734.417-1.173L6.963 3.102a1.125 1.125 0 00-1.091-.852H4.5A2.25 2.25 0 002.25 4.5v2.25z" />}
          text="+353 91 123 456"
        />
        <InfoItem 
          icon={<path strokeLinecap="round" strokeLinejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75" />}
          text="hello@coppercup.ie"
        />
      </InfoBlock>

      {/* Opening Hours Block */}
      <InfoBlock title="Opening Hours">
        <div className="flex justify-between">
          <span>Monday - Friday</span>
          <span>7:00 AM - 6:00 PM</span>
        </div>
        <div className="flex justify-between">
          <span>Saturday</span>
          <span>8:00 AM - 5:00 PM</span>
        </div>
        <div className="flex justify-between">
          <span>Sunday & Holidays</span>
          <span className="text-[#B6771D] font-semibold">Closed</span>
        </div>
      </InfoBlock>
    </div>
  );
}
