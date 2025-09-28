import '../styles/globals.css';
import { AuthProvider } from '../features/auth/AuthContext';
import Navbar from '../components/layout/Navbar';
import EnvBadge from '../components/common/EnvBadge';

export const metadata = {
  title: 'Copper Cup Coffee',
  description: 'Copper Cup Coffee — artisanal roasts with a smooth shopping experience',
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>
        <AuthProvider>
          <Navbar />
          <main className="mx-auto max-w-7xl p-4 sm:p-6 lg:p-8">{children}</main>
          <EnvBadge />
        </AuthProvider>
      </body>
    </html>
  );
}
