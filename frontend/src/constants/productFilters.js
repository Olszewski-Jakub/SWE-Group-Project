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
  // Placeholder for categories to be replaced with actual categories later
  {
    name: 'roast',
    label: 'Roast Level',
    options: [
      { value: 'none', label: 'No Roast' },
      { value: 'medium', label: 'Medium Roast' },
      { value: 'dark', label: 'Dark Roast' }
    ]
  },
  {
    name: 'origin',
    label: 'Origin',
    options: [
      { value: 'Brazil', label: 'Brazil' },
      { value: 'Ethiopia', label: 'Ethiopia' },
      { value: 'Colombia', label: 'Colombia' }
    ]
  },
  {
    name: 'size_ml',
    label: 'Size',
    options: [
      { value: '350', label: '350ml' },
      { value: '500', label: '500ml' }
    ]
  },
];