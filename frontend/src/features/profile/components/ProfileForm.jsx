"use client";

import { useEffect, useMemo, useState } from 'react';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { updateProfile } from '@/features/profile/services/profileService';
import { useAuth } from '@/hooks/useAuth';

function splitName(name) {
  if (!name) return { firstName: '', lastName: '' };
  const parts = String(name).trim().split(/\s+/);
  if (parts.length === 1) return { firstName: parts[0] || '', lastName: '' };
  return { firstName: parts.slice(0, -1).join(' '), lastName: parts.slice(-1).join('') };
}

export default function ProfileForm({ user }) {
  const { refresh } = useAuth();
  const initial = useMemo(() => {
    if (!user) return { firstName: '', lastName: '', email: '' };
    // Prefer explicit fields if backend provides them; fall back to name parsing
    const { firstName: fn, lastName: ln } = user;
    const parsed = (!fn && !ln && user.name) ? splitName(user.name) : { firstName: fn || '', lastName: ln || '' };
    return {
      firstName: parsed.firstName || '',
      lastName: parsed.lastName || '',
      email: user.email || '',
    };
  }, [user]);

  const [values, setValues] = useState(initial);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    setValues(initial);
  }, [initial]);

  function onChange(e) {
    const { name, value } = e.target;
    setValues((v) => ({ ...v, [name]: value }));
  }

  async function onSubmit(e) {
    e.preventDefault();
    setSaving(true);
    setMessage(null);
    setError(null);
    try {
      await updateProfile({ firstName: values.firstName, lastName: values.lastName });
      try { await refresh(); } catch (_) {}
      setMessage('Profile saved successfully.');
    } catch (err) {
      const msg = err?.response?.data?.message || 'Failed to save profile.';
      setError(msg);
    } finally {
      setSaving(false);
    }
  }

  return (
    <Card title="Profile">
      <form onSubmit={onSubmit} className="space-y-4">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Input
            id="firstName"
            name="firstName"
            label="First name"
            value={values.firstName}
            onChange={onChange}
            autoComplete="given-name"
          />
          <Input
            id="lastName"
            name="lastName"
            label="Last name"
            value={values.lastName}
            onChange={onChange}
            autoComplete="family-name"
          />
        </div>

        <Input
          id="email"
          name="email"
          label="Email"
          value={values.email}
          readOnly
          className="opacity-90"
          hint="Email changes are not supported here."
          autoComplete="email"
        />

        {message ? <p className="text-sm text-green-700">{message}</p> : null}
        {error ? <p className="text-sm text-red-600">{error}</p> : null}

        <div className="flex items-center justify-end gap-3">
          <Button type="submit" disabled={saving}>
            {saving ? 'Saving…' : 'Save changes'}
          </Button>
        </div>
      </form>
    </Card>
  );
}

