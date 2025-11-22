/**
 * Product filter constants
 */
export const CATEGORIES = [
  { value: '', label: 'All Categories' },
  // Placeholder for categories to be replaced with actual categories later
  { value: 'Coffee', label: 'Coffee' },
  { value: 'Tea', label: 'Tea' },
  { value: 'Beans', label: 'Beans' },
  { value: 'Mugs', label: 'Mugs' },
  { value: 'Bakery', label: 'Bakery' },
];

export const SORT_OPTIONS = [
  { value: 'DEFAULT', label: 'Default' },
  { value: 'PRICE_LOW_TO_HIGH', label: 'Price: Low to High' },
  { value: 'PRICE_HIGH_TO_LOW', label: 'Price: High to Low' },
  { value: 'NEWEST_FIRST', label: 'Newest' },
];

/**
 * Available attribute filters based on product attributes
 * Each attribute includes its name, label, and available values with display names
 */
export const ATTRIBUTE_FILTERS = [
  { name: 'roast', label: 'Roast', options: [
      { value: 'light', label: 'Light' },
      { value: 'medium', label: 'Medium' },
      { value: 'dark', label: 'Dark' },
    ] },
  { name: 'origin', label: 'Origin', options: [
      { value: 'Brazil', label: 'Brazil' },
      { value: 'Colombia', label: 'Colombia' },
      { value: 'Ethiopia', label: 'Ethiopia' },
      { value: 'Kenya', label: 'Kenya' },
    ] },
  { name: 'grind', label: 'Grind', options: [
      { value: 'whole_beans', label: 'Whole Beans' },
      { value: 'espresso', label: 'Espresso' },
      { value: 'filter', label: 'Filter' },
      { value: 'french_press', label: 'French Press' },
    ] },
  { name: 'size_ml', label: 'Size (ml)', options: [
      { value: '250', label: '250 ml' },
      { value: '350', label: '350 ml' },
      { value: '500', label: '500 ml' },
      { value: '1000', label: '1000 ml' },
    ] },
  { name: 'caffeine', label: 'Caffeine', options: [
      { value: 'regular', label: 'Regular' },
      { value: 'decaf', label: 'Decaf' },
    ] },
  { name: 'weight_g', label: 'Weight (g)', options: [
      { value: '250', label: '250 g' },
      { value: '350', label: '350 g' },
      { value: '500', label: '500 g' },
      { value: '1000', label: '1000 g' },
    ] },
  { name: 'count', label: 'Count', options: [
      { value: '1', label: '1' },
      { value: '10', label: '10' },
      { value: '12', label: '12' },
      { value: '16', label: '16' },
      { value: '20', label: '20' },
    ] },
];
