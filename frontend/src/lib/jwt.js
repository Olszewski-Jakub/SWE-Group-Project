// Lightweight JWT helpers (no external deps)

export function decodeBase64Url(base64Url) {
  try {
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
    if (typeof window !== 'undefined') {
      return decodeURIComponent(
        atob(padded)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
    }
    return Buffer.from(padded, 'base64').toString('utf8');
  } catch (_) {
    return '';
  }
}

export function parseJwt(token) {
  if (!token || typeof token !== 'string') return null;
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  try {
    const payload = JSON.parse(decodeBase64Url(parts[1]) || '{}');
    return payload;
  } catch (_) {
    return null;
  }
}

export function getJwtExp(token) {
  const payload = parseJwt(token);
  const exp = payload?.exp;
  return typeof exp === 'number' ? exp : null;
}

export function getJwtRoles(token) {
  const payload = parseJwt(token);
  const roles = payload?.roles || payload?.role || payload?.authorities || [];
  if (Array.isArray(roles)) return roles;
  if (typeof roles === 'string') return roles.split(',').map((r) => r.trim()).filter(Boolean);
  return [];
}

