/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        'brand-brown': '#7B542F',
        'brand-gold': '#B6771D',
        'brand-primary': '#FF9D00',
        'brand-light': '#FFCF71',
      },
      keyframes: {
        pop: {
          '0%': { opacity: '0', transform: 'translateY(8px) scale(0.95)' },
          '100%': { opacity: '1', transform: 'translateY(0) scale(1)' },
        },
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
      },
      animation: {
        pop: 'pop 180ms cubic-bezier(0.16,1,0.3,1) forwards',
        'fade-in': 'fade-in 120ms ease-out forwards',
      },
    },
  },
  plugins: [],
  darkMode: 'class',
};
