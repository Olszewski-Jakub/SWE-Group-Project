import '../styles/globals.css';
import { AuthProvider } from '../features/auth/AuthContext';
import Navbar from '../components/layout/Navbar';

export const metadata = {
  title: 'Acme App',
  description: 'Frontend-only Next.js with token auth',
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>
        <AuthProvider>
          <Navbar />
          <main className="mx-auto max-w-7xl p-4 sm:p-6 lg:p-8">{children}</main>
        </AuthProvider>
      </body>
    </html>
  );
}
