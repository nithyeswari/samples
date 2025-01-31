# Enterprise MFE Architecture with Product Monorepo

## Overview

This project implements a micro-frontend architecture with two main parts:
1. Product Management Monorepo - Core reusable modules
2. Order Management MFE - Shell application with state management

## Repository Structure

```
├── product-monorepo/            # Repository 1: Product Management
│   ├── packages/
│   │   ├── core/
│   │   │   └── src/
│   │   │       └── types/
│   │   └── product/
│   │       ├── src/
│   │       │   ├── models/
│   │       │   ├── services/
│   │       │   └── components/
│   │       └── webpack.config.js
│   └── package.json
│
└── order-mfe/                   # Repository 2: Order Management
    ├── src/
    │   ├── shell/              # Shell Architecture
    │   │   ├── components/
    │   │   └── layouts/
    │   ├── state/              # Global State
    │   │   └── store.ts
    │   ├── pages/              # Route Pages
    │   └── App.tsx
    └── package.json
```

## Quick Start

### 1. Start Product Monorepo
```bash
cd product-monorepo
npm install
npm run dev    # Runs on http://localhost:3001
```

### 2. Start Order MFE
```bash
cd order-mfe
npm install
npm start     # Runs on http://localhost:3000
```

## Product Monorepo

### Core Features
- Reusable product components
- Shared services and utilities
- Type definitions
- Module Federation setup

### Module Federation Configuration
```javascript
// product-monorepo/packages/product/webpack.config.js
{
  plugins: [
    new ModuleFederationPlugin({
      name: 'product_management',
      filename: 'remoteEntry.js',
      exposes: {
        './ProductService': './src/services/ProductService',
        './ProductForm': './src/components/ProductForm',
        './types': './src/types/index'
      }
    })
  ]
}
```

## Order MFE Shell

### Shell Architecture
- Header with navigation
- Sidebar for main menu
- MainLayout wrapper
- Error boundaries
- Loading states

### State Management (Zustand)
```typescript
// order-mfe/src/state/store.ts
const useOrderStore = create((set) => ({
  cart: {
    items: [],
    total: 0
  },
  loading: false,
  error: null,
  
  addToCart: (product, quantity) => set(state => ({
    cart: {
      items: [...state.cart.items, { product, quantity }],
      total: calculateTotal(state.cart.items)
    }
  })),
  
  // Other actions...
}));
```

### Remote Module Integration
```typescript
// order-mfe/src/pages/CreateOrder.tsx
const ProductSelector = React.lazy(() => import('productModule/ProductSelector'));

const CreateOrder = () => {
  const { addToCart } = useOrderStore();
  
  return (
    <Suspense fallback="Loading...">
      <ProductSelector onSelect={addToCart} />
    </Suspense>
  );
};
```

## Development Guidelines

### 1. Product Module Development
```typescript
// Adding new product feature
// product-monorepo/packages/product/src/components/NewFeature.tsx
export const NewFeature = () => {
  // Implementation
};

// Expose in webpack.config.js
{
  exposes: {
    './NewFeature': './src/components/NewFeature'
  }
}
```

### 2. Order MFE Development
```typescript
// Using product feature in Order MFE
const NewFeature = React.lazy(() => import('productModule/NewFeature'));

// Add to routes
<Route path="/new-feature" element={<NewFeature />} />
```

### 3. State Management
```typescript
// Using store in components
const Component = () => {
  const { cart, addToCart } = useOrderStore();
  
  return (
    <div>
      Cart Items: {cart.items.length}
      <button onClick={() => addToCart(product, 1)}>Add</button>
    </div>
  );
};
```

## Deployment

### 1. Product Monorepo Deployment
```bash
# Build modules
cd product-monorepo
npm run build

# Docker deployment
docker build -t product-monorepo .
docker run -p 3001:80 product-monorepo
```

### 2. Order MFE Deployment
```bash
# Build application
cd order-mfe
npm run build

# Docker deployment
docker build -t order-mfe .
docker run -p 3000:80 order-mfe
```

## Configuration

### Product Monorepo
```env
PORT=3001
API_URL=http://api.example.com
```

### Order MFE
```env
PORT=3000
PRODUCT_MODULE_URL=http://localhost:3001
API_URL=http://api.example.com
```

## Best Practices

### 1. Module Federation
- Keep remotes granular
- Version exposed modules
- Handle loading states
- Implement error boundaries

### 2. State Management
- Keep state normalized
- Use selectors for derived data
- Handle async operations cleanly
- Implement proper error handling

### 3. Shell Architecture
- Maintain consistent layouts
- Implement proper navigation
- Handle loading states globally
- Manage errors at appropriate levels

## Error Handling

### 1. Remote Module Errors
```typescript
<ErrorBoundary>
  <Suspense fallback="Loading...">
    <RemoteComponent />
  </Suspense>
</ErrorBoundary>
```

### 2. State Errors
```typescript
try {
  await someOperation();
} catch (error) {
  useOrderStore.getState().setError(error.message);
}
```

## Testing

### 1. Product Module Tests
```typescript
// product-monorepo/packages/product/src/__tests__/ProductService.test.ts
describe('ProductService', () => {
  test('creates product', async () => {
    // Test implementation
  });
});
```

### 2. Order MFE Tests
```typescript
// order-mfe/src/__tests__/store.test.ts
describe('OrderStore', () => {
  test('adds to cart', () => {
    // Test implementation
  });
});
```

## Monitoring & Performance

### 1. Module Loading
- Track remote module load times
- Monitor chunk sizes
- Implement retry mechanisms

### 2. State Performance
- Monitor store updates
- Track component re-renders
- Implement performance tracking

## Support & Maintenance

### Documentation
- API Documentation: `/docs/api`
- Component Library: `/docs/components`
- State Management: `/docs/state`

### Support Channels
- GitHub Issues
- Slack: #mfe-support
- Email: support@example.com

## License

MIT © Your Organization