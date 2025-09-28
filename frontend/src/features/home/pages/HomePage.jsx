import HeroSection from '@/features/home/components/HeroSection';
import FeaturedGrid from '@/features/home/components/FeaturedGrid';
import SplitSection from '@/features/home/components/SplitSection';
import NewsletterSection from '@/features/home/components/NewsletterSection';

export default function HomePage() {
  return (
    <div className="space-y-12">
      <HeroSection />
      <FeaturedGrid />
      <SplitSection />
      <NewsletterSection />
    </div>
  );
}
