/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {

      // "dev:local": "NEXT_PUBLIC_ENVIRONMENT=local NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1 next dev --turbopack",
      //     "dev:dev": "NEXT_PUBLIC_ENVIRONMENT=dev NEXT_PUBLIC_API_BASE_URL=https://api.bushive.app/swe next dev --turbopack",
      const apiBase = process.env.NEXT_PUBLIC_API_BASE_URL || '';

      // Default fallback when no API base is provided or parsing fails
      const fallback = {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      };

      if (!apiBase) return [fallback];

      try {
        // Destructure origin and pathname from the URL
        const { origin, pathname } = new URL(apiBase);
        // Ensure pathname doesn't have a trailing slash (but keeps root '/')
        const basePath = (pathname || '').replace(/\/$/, '');

        // If the pathname is empty (i.e. apiBase was something like https://host),
        // fall back to '/api' as the source path to avoid rewiring the entire site root.
        const sourcePath = basePath === '' ? '/api' : basePath;

        const source = `${sourcePath}/:path*`;
        const destination = `${origin}${sourcePath}/:path*`;

        return [
          {
            source,
            destination,
          },
        ];
      } catch (err) {
        // If apiBase isn't a full URL (e.g. a path like '/api/v1'), handle gracefully
        const trimmed = apiBase.replace(/\/$/, '');
        const sourcePath = trimmed === '' ? '/api' : (trimmed.startsWith('/') ? trimmed : `/${trimmed}`);
        const destinationBase = trimmed === '' ? 'http://localhost:8080/api' : trimmed;

        return [
          {
            source: `${sourcePath}/:path*`,
            destination: `${destinationBase}/:path*`,
          },
        ];
      }
  },
};

export default nextConfig;