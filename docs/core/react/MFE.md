# Product and Order Management Setup

This project consists of two separate repositories:
1. Product Management Monorepo
2. Order Management MFE

## Repository 1: Product Management Monorepo

### Structure
```
product-management-monorepo/
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
│       ├── package.json
│       └── webpack.config.js
└── package.json
```

### Setup
```bash
# Clone product management repo
git clone https://github.com/your-org/product-management-monorepo.git
cd product-management-monorepo

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build
```

### Exposed Modules
- ProductService: CRUD operations for products
- ProductForm: Reusable product form component
- Product models and types

## Repository 2: Order Management MFE

### Structure
```
order-management-mfe/
├── src/
│   ├── models/
│   │   └── Order.ts
│   ├── services/
│   │   └── OrderService.ts
│   └── components/
│       └── CreateOrder.tsx
├── package.json
└── webpack.config.js
```

### Setup
```bash
# Clone order management repo
git clone https://github.com/your-org/order-management-mfe.git
cd order-management-mfe

# Install dependencies
npm install

# Start development server
npm run dev
```

### Configuration
Update webpack.config.js with the correct URL for the product management remote:

```javascript
module.exports = {
  plugins: [
    new ModuleFederationPlugin({
      remotes: {
        product_management: 'product_management@http://your-domain/remoteEntry.js'
      }
    })
  ]
}
```

## Integration Example

```typescript
// In Order Management MFE
import { ProductService } from 'product_management/ProductService';
import { Product } from 'product_management/models/Product';

const OrderComponent = () => {
  const productService = new ProductService('http://api.example.com');
  
  // Use product service
  const products = await productService.list();
  
  // Create order using products
  // ...
};
```

## Development Guidelines

1. Product Management:
   - Keep product logic in the monorepo
   - Expose necessary interfaces and services
   - Version components appropriately

2. Order Management:
   - Consume product modules via Module Federation
   - Handle order-specific business logic
   - Maintain loose coupling with product module

## Deployment

1. Deploy Product Management:
```bash
cd product-management-monorepo
npm run build
docker build -t product-management .
docker push product-management
```

2. Deploy Order Management:
```bash
cd order-management-mfe
npm run build
docker build -t order-management .
docker push order-management
```

## Environment Variables

### Product Management
```env
API_URL=http://api.example.com
PORT=3001
```

### Order Management
```env
PRODUCT_MANAGEMENT_URL=http://product-management.example.com
API_URL=http://api.example.com
PORT=3002
```

## Troubleshooting

1. Module Loading Issues:
   - Check remote URLs in webpack config
   - Verify product management is deployed and accessible
   - Check browser console for CORS issues

2. Integration Issues:
   - Verify product service API compatibility
   - Check type definitions
   - Validate environment variables

## Support

- Repository Issues
- Documentation
- Slack Channel: #product-order-support