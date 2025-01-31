# Product Monorepo and Order MFE

This project consists of two separate repositories that work together:
1. Product Management Monorepo - Core product functionality
2. Order MFE - Order management application consuming product modules

## Product Management Monorepo

Repository containing reusable product management modules.

### Structure
```
product-monorepo/
├── packages/
│   ├── core/
│   │   └── src/
│   │       └── types/
│   │           └── BaseModel.ts
│   └── product/
│       ├── src/
│       │   ├── models/
│       │   │   └── Product.ts
│       │   ├── services/
│       │   │   └── ProductService.ts
│       │   └── components/
│       │       └── ProductForm.tsx
│       ├── webpack.config.js
│       └── package.json
└── package.json
```

### Setup & Installation
```bash
# Clone repository
git clone git@github.com:your-org/product-monorepo.git

# Install dependencies
npm install

# Start development
npm run dev

# Build for production
npm run build
```

### Exposed Modules
The monorepo exposes the following modules via Module Federation:
```javascript
// Product types and interfaces
'./types': './src/types/index'

// Product services
'./services': './src/services/ProductService'

// React components
'./components': './src/components/index'
```

### Configuration
```javascript
// webpack.config.js
module.exports = {
  plugins: [
    new ModuleFederationPlugin({
      name: 'product_management',
      filename: 'remoteEntry.js',
      exposes: {
        './ProductService': './src/services/ProductService',
        './ProductForm': './src/components/ProductForm'
      }
    })
  ]
}
```

## Order Management MFE

Single-page application that consumes product modules from the monorepo.

### Structure
```
order-mfe/
├── src/
│   ├── pages/
│   │   ├── OrderList.tsx
│   │   └── CreateOrder.tsx
│   ├── components/
│   │   └── OrderForm.tsx
│   └── services/
│       └── OrderService.ts
├── webpack.config.js
└── package.json
```

### Setup & Installation
```bash
# Clone repository
git clone git@github.com:your-org/order-mfe.git

# Install dependencies
npm install

# Start development
npm start
```

### Configuration
```javascript
// webpack.config.js
module.exports = {
  plugins: [
    new ModuleFederationPlugin({
      name: 'order_app',
      remotes: {
        productModule: 'product_management@http://product-server.com/remoteEntry.js'
      }
    })
  ]
}
```

### Using Remote Modules
```typescript
// Import product components
const ProductSelector = React.lazy(() => import('productModule/ProductSelector'));

// Import product services
const { ProductService } = await import('productModule/services');

// Use in components
const CreateOrder = () => {
  return (
    <Suspense fallback="Loading...">
      <ProductSelector onSelect={handleProductSelect} />
    </Suspense>
  );
};
```

## Development Process

### 1. Start Product Monorepo
```bash
cd product-monorepo
npm run dev
# Serves at http://localhost:3001
```

### 2. Start Order MFE
```bash
cd order-mfe
npm start
# Serves at http://localhost:3000
```

### Environment Variables

Product Monorepo:
```env
PORT=3001
API_URL=http://api.example.com
```

Order MFE:
```env
PORT=3000
PRODUCT_MODULE_URL=http://localhost:3001
API_URL=http://api.example.com
```

## Deployment

### Product Monorepo
```bash
# Build
cd product-monorepo
npm run build

# Docker build
docker build -t product-monorepo .
docker run -p 3001:80 product-monorepo
```

### Order MFE
```bash
# Build
cd order-mfe
npm run build

# Docker build
docker build -t order-mfe .
docker run -p 3000:80 order-mfe
```

## Troubleshooting

### Common Issues

1. Remote Module Loading
- Check if Product Monorepo is running
- Verify PRODUCT_MODULE_URL is correct
- Check browser console for CORS issues

2. Type Errors
- Ensure types are properly exported from monorepo
- Check version compatibility

### Debug Steps

1. Verify Product Monorepo Accessibility:
```bash
curl http://product-server.com/remoteEntry.js
```

2. Check Module Federation Setup:
```javascript
// In browser console
window.productModule
```

## API Integration

### Product Service
```typescript
const productService = new ProductService('http://api.example.com');

// Create product
await productService.create({
  name: 'Product 1',
  price: 99.99
});

// List products
const products = await productService.list();
```

### Order Service
```typescript
const orderService = new OrderService('http://api.example.com');

// Create order with products
await orderService.create({
  products: selectedProducts,
  total: 299.97
});
```

## Contributing

### Product Monorepo
1. Create feature branch
2. Make changes
3. Run tests: `npm test`
4. Build: `npm run build`
5. Submit PR

### Order MFE
1. Create feature branch
2. Make changes
3. Test with Product Monorepo
4. Submit PR

## Support

- Documentation: `/docs`
- Issues: GitHub Issues
- Slack: #product-order-support

## License

MIT © Your Organization