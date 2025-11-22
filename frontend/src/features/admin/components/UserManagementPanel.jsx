"use client";

export default function UserManagementPanel() {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-stone-900">User Management</h2>
      </div>

      <div className="rounded-lg border border-stone-200 bg-white/70 p-6">
        <div className="text-stone-700">This section is under construction.</div>
        <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="h-24 rounded-md border border-dashed border-stone-300 bg-stone-50" />
          <div className="h-24 rounded-md border border-dashed border-stone-300 bg-stone-50" />
        </div>
        <div className="mt-4 h-40 rounded-md border border-dashed border-stone-300 bg-stone-50" />
      </div>
    </div>
  );
}

