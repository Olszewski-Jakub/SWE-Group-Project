# Next.js Frontend-Only Architecture Prompt

You are a senior frontend developer.  
Your task is to create a **professional, scalable Next.js frontend-only architecture** that communicates with a REST API.  
The output should be production-ready code with the following requirements:

---

## Requirements

- **Language**: JavaScript (not TypeScript)
- **Framework**: Next.js (App Router)
- **Styling**: Tailwind CSS
- **State Management**: React Context API
- **Authentication**:
  - Access and Refresh tokens
  - Tokens stored in cookies (refresh in HttpOnly cookie, access in memory or readable cookie)
  - Automatic token refresh via API endpoint (`/auth/refresh`)
- **API Client**: Axios
  - Centralized instance in `/lib/apiClient.js`
  - Request interceptor: attach `Authorization: Bearer <accessToken>`
  - Response interceptor: on `401`, call refresh endpoint, update token, retry original request
  - If refresh fails → logout and redirect to `/login`
- **Routing**:
  - Use Next.js App Router (`src/app/`)
  - Prefer static routes (e.g. `/dashboard`, `/profile`)
  - Use `<Link>` for navigation with prefetching
  - Active route highlighting with `usePathname`
  - Protected routes redirect unauthenticated users to `/login`
- **Navigation**:
  - Professional `Navbar` component with Tailwind styling
  - Shows links, active states, and user login/logout status
- **Feature Modules**:
  - Organize by feature (`features/auth`, `features/dashboard`, etc.)
  - Each feature contains components, context, hooks, and services
- **Components**:
  - `/components/ui/` → shared small components (Button, Input, etc.)
  - `/components/layout/` → layout components (Navbar, Footer)
  - `/components/common/` → generic reusable components (Modal, Loader, etc.)

---

## Target File Structure

```plaintext
src/
├── app/
│   ├── layout.js
│   ├── page.js
│   ├── login/page.js
│   ├── dashboard/page.js
│   └── ...
├── components/
│   ├── layout/Navbar.js
│   ├── ui/Button.js
│   ├── common/Modal.js
│   └── ...
├── features/
│   ├── auth/
│   │   ├── AuthContext.js
│   │   ├── LoginForm.js
│   │   └── ...
│   └── dashboard/
│       └── ...
├── lib/
│   ├── apiClient.js
│   └── authService.js
├── hooks/
│   └── useAuth.js
├── utils/
│   └── helpers.js
├── styles/
│   └── globals.css
└── public/
    └── ...
```

---

## Deliverables

1. **AuthContext** in `features/auth/AuthContext.js`  
   - `login(credentials)` → calls `/auth/login`, sets user, stores tokens  
   - `logout()` → calls `/auth/logout`, clears state  
   - Expose `user`, `login`, `logout`  

2. **API Client** in `lib/apiClient.js`  
   - Configured Axios instance with interceptors for token attach & refresh  

3. **Navbar** in `components/layout/Navbar.js`  
   - Tailwind styled, shows links  
   - Active link highlighting with `usePathname`  
   - Shows login/logout depending on context  

4. **Protected Route Example** in `app/dashboard/page.js`  
   - Uses `AuthContext`  
   - Redirects to `/login` if user is not authenticated  

5. **Login Page** in `app/login/page.js`  
   - Renders `LoginForm` component  
   - Calls `login` from `AuthContext`  

---

## Goals

- Clean, **scalable architecture**
- Separation of concerns (features, components, lib, hooks, utils)
- Secure auth flow with cookies + token refresh
- Professional navigation with active states
- Consistent styling with Tailwind
