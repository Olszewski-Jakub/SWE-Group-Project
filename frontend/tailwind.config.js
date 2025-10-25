/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        'brand-brown': '#7B542F',
        'brand-gold': '#B6771D',
        'brand-primary': '#FF9D00',
        'brand-light': '#FFCF71',
      }
    },
  },
  plugins: [],
  darkMode: 'class',
};