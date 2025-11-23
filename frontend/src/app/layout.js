import '../styles/globals.css';
import { AuthProvider } from '../features/auth/AuthContext';
import Navbar from '../components/layout/Navbar';
import EnvBadge from '../components/common/EnvBadge';
import { CartProvider } from '../features/cart/CartContext';
import CartPanel from '../components/cart/CartPanel';

export const metadata = {
  title: 'StackOverFlowedCup',
  description: 'StackOverFlowedCup — artisanal roasts with a smooth shopping experience',
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>
        <AuthProvider>
          <CartProvider>
            <Navbar />
            <CartPanel />
            <main className="mx-auto max-w-7xl p-4 sm:p-6 lg:p-8">
              {children}
            </main>
            <EnvBadge />
          </CartProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
