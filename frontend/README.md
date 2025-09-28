This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://github.com/vercel/next.js/tree/canary/packages/create-next-app).

## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `app/page.js`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.

---

## Copper Cup Coffee — Auth Frontend

- Base URL: set `NEXT_PUBLIC_API_BASE_URL` for the backend (defaults to `http://localhost:4000`).
- Tokens: access token is kept in memory and mirrored to `sessionStorage`; refresh token is never stored and is handled via HttpOnly cookie by the backend.
- Axios: all requests use a secure instance with `withCredentials` and automatic 401 refresh + single-flight queuing.
- Pages: `/signin`, `/signup`, `/dashboard` (protected), `/admin` (ADMIN only).
- Navbar: coffee-themed brand (Copper Cup), Home; Dashboard (when signed in); Admin (when `ADMIN`); right side shows Login or a user menu with Logout.

### Run

```bash
npm i
# Local (uses http://localhost:4000 backend)
npm run dev:local

# Dev (no API base URL set; uses same-origin or proxy)
npm run dev:dev

# Production build / start with environment badge
npm run build:prod && npm run start:prod
```

Environment badge appears bottom-right (LOCAL/DEV/PROD) based on `NEXT_PUBLIC_ENVIRONMENT`.
Configure backend URL via `NEXT_PUBLIC_API_BASE_URL` (only set for local by default).
