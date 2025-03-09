# MosaicUI

<div align="center">

![MosaicUI Logo](https://via.placeholder.com/300x100?text=MosaicUI)

**A micro-frontend framework with JSON-driven UI for building modular web applications**

[![npm version](https://img.shields.io/npm/v/@mosaicui/core.svg?style=flat)](https://www.npmjs.com/package/@mosaicui/core)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

</div>

## Features

- 🧩 **Micro-frontend architecture** - Build applications as independent, composable modules
- 🔄 **JSON-driven rendering** - Define UIs with simple JSON layouts
- 🔌 **Pluggable component system** - Register and use components dynamically
- 🌐 **Server-driven UI** - Update layouts from the backend without deploying frontend code
- 🎭 **Framework agnostic** - Works with React, Vue, and Angular
- 🔧 **Webpack Module Federation** - First-class support for module federation

## Installation

```bash
# Install core package
npm install @mosaicui/core

# Install React integration (optional)
npm install @mosaicui/react

# Install webpack plugin (recommended for module federation)
npm install @mosaicui/webpack-plugin --save-dev
```

## Quick Start

### 1. Register Components

```jsx
// src/components/index.js
import { registerComponents } from '@mosaicui/core';
import Header from './Header';
import Sidebar from './Sidebar';
import ProductCard from './ProductCard';
import Footer from './Footer';

// Register your components
registerComponents({
  Header,
  Sidebar,
  ProductCard,
  Footer
});
```

### 2. Create JSON Layout

```json
// src/layouts/homepage.json
{
  "component": "Layout",
  "children": [
    {
      "component": "Header",
      "props": {
        "title": "Welcome to Our Store",
        "userName": "$context.user.name"
      }
    },
    {
      "component": "Content",
      "children": [
        {
          "component": "ProductList",
          "props": {
            "products": "$context.products",
            "layout": "grid"
          }
        }
      ]
    },
    {
      "component": "Footer",
      "props": {
        "showSocialLinks": true
      }
    }
  ]
}
```

### 3. Render UI from JSON

```jsx
// src/App.jsx
import React from 'react';
import { DynamicRenderer } from '@mosaicui/core';
import homepageLayout from './layouts/homepage.json';
import './components'; // Register components

function App() {
  const context = {
    user: {
      name: 'John',
      role: 'customer'
    },
    products: [
      { id: 1, name: 'Product A', price: 19.99 },
      { id: 2, name: 'Product B', price: 29.99 }
    ]
  };

  return (
    <DynamicRenderer 
      layout={homepageLayout} 
      context={context} 
    />
  );
}

export default App;
```

## Webpack Integration

### MosaicUI Webpack Plugin

The `MosaicUIPlugin` simplifies setting up micro-frontend architecture by wrapping Webpack's Module Federation plugin with MosaicUI-specific features:

```js
// webpack.config.js
const { MosaicUIPlugin } = require('@mosaicui/webpack-plugin');

module.exports = {
  // ... other webpack config
  plugins: [
    new MosaicUIPlugin({
      // App identity for Module Federation
      appName: 'catalog-app',
      
      // Components to expose to other apps
      exposes: {
        './ProductCard': './src/components/ProductCard',
        './CategoryList': './src/components/CategoryList'
      },
      
      // Remote apps to consume
      remotes: {
        checkout: 'checkout@https://checkout.example.com/remoteEntry.js',
        account: 'account@https://account.example.com/remoteEntry.js'
      },
      
      // Where to find layouts
      layouts: './src/layouts',
      
      // Automatically register discovered components (optional)
      autoRegisterComponents: true,
      componentsDir: './src/components',
      
      // Additional modules to share
      shared: ['lodash', 'styled-components']
    })
  ]
};
```

#### Key Features

1. **Module Federation Setup**: Automatically configures Webpack's Module Federation plugin with sensible defaults
   
2. **Layout Registration**: Automatically registers JSON layouts from specified directories
   
3. **Component Discovery**: Optionally scans for and registers components automatically
   
4. **Development Utilities**: Adds development-time tools like component explorers and layout inspectors
   
5. **Optimization**: Configures optimal bundle splitting and sharing strategies for micro-frontends

#### Configuration Options

| Option | Type | Description |
|--------|------|-------------|
| `appName` | `string` | Name of your application for Module Federation |
| `exposes` | `object` | Components to expose to other applications |
| `remotes` | `object` | Remote applications to consume components from |
| `layouts` | `string` | Path to directory containing JSON layouts |
| `autoRegisterComponents` | `boolean` | Whether to auto-discover and register components |
| `componentsDir` | `string` | Path to components directory for auto-registration |
| `shared` | `array` | Additional dependencies to share between applications |
| `devTools` | `boolean` | Enable development tools (default: `true` in development) |

#### Benefits

The plugin simplifies several aspects of MosaicUI development:

- **Reduced boilerplate**: Handles Module Federation setup automatically
- **Standardization**: Ensures consistent configuration across projects
- **Automation**: Handles layout and component registration
- **Performance**: Optimizes bundle splitting for micro-frontends
- **Developer experience**: Adds helpful development tools

By using this plugin, developers can focus on building components and layouts rather than dealing with the configuration complexities of micro-frontend architecture.

## Using Remote Components

```jsx
import React, { lazy, Suspense } from 'react';
import { DynamicRenderer } from '@mosaicui/core';

// Load remote components
const RemoteProductDetail = lazy(() => import('shop/ProductDetail'));

// Register the remote component
registerComponents({
  ProductDetail: RemoteProductDetail
});

// Use in JSON layout
const productDetailLayout = {
  component: "Layout",
  children: [
    {
      component: "ProductDetail",
      props: {
        productId: "$context.productId"
      }
    }
  ]
};

function ProductPage({ productId }) {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <DynamicRenderer 
        layout={productDetailLayout} 
        context={{ productId }} 
      />
    </Suspense>
  );
}
```

## Server-Driven UI

```jsx
import React from 'react';
import { useRemoteLayout } from '@mosaicui/react';
import { DynamicRenderer } from '@mosaicui/core';
import './components';

function App() {
  const { layout, loading, error } = useRemoteLayout('https://api.example.com/ui/homepage');
  const context = { /* your context */ };

  if (loading) return <div>Loading layout...</div>;
  if (error) return <div>Error loading layout: {error}</div>;

  return <DynamicRenderer layout={layout} context={context} />;
}
```

## Packages

MosaicUI consists of several packages:

- `@mosaicui/core` - Core rendering engine and component registry
- `@mosaicui/react` - React-specific utilities and hooks
- `@mosaicui/layout-registry` - Layout management and versioning
- `@mosaicui/webpack-plugin` - Webpack integration for module federation
- `@mosaicui/marketplace` - Component discovery and sharing (optional)

## Advanced Features

### Component Registry

```jsx
import { ComponentRegistry } from '@mosaicui/core';

// Create a scoped registry
const myRegistry = new ComponentRegistry();

// Register components
myRegistry.register('CustomComponent', MyComponent);

// Use the scoped registry
<DynamicRenderer 
  layout={layout} 
  context={context}
  componentRegistry={myRegistry} 
/>
```

### Context Processing

MosaicUI automatically processes context variables in props:

```json
{
  "component": "UserProfile",
  "props": {
    "name": "$context.user.name",           // Simple path
    "role": "$context.user.role || 'Guest'" // With fallback
  }
}
```

### Layout Composition

Combine layouts for better organization:

```jsx
import { composeLayouts } from '@mosaicui/core';

const baseLayout = {
  component: "Layout",
  children: [
    { component: "Header" },
    { component: "Content", props: { id: "content-slot" } },
    { component: "Footer" }
  ]
};

const pageLayout = {
  contentSlot: "content-slot",
  content: {
    component: "ProductList",
    props: { products: "$context.products" }
  }
};

const finalLayout = composeLayouts(baseLayout, pageLayout);
```

### Runtime Layout Modifications

Transform layouts at runtime:

```jsx
import { transformLayout } from '@mosaicui/core';

// Add analytics to all buttons
const enhancedLayout = transformLayout(layout, {
  predicate: node => node.component === 'Button',
  transform: node => ({
    ...node,
    props: {
      ...node.props,
      onClick: '$context.trackClick'
    }
  })
});
```

## Architecture Patterns

MosaicUI supports several micro-frontend patterns:

### Hub-and-Spoke

Applications serve as both central hubs and specialized components.

### Federated Marketplace

Components are published to a central registry for discovery.

### Nested Composition

Micro-frontends can contain other micro-frontends in a hierarchical structure.

### Event-Driven Communication

Applications communicate through events rather than direct imports.

### Server-Driven UI

Backend services control UI layout and behavior.

## Best Practices

### Component Design

- Design components to be self-contained and reusable
- Use consistent prop naming conventions
- Keep components pure and predictable
- Avoid component-specific global state
- Use clear component naming that reflects purpose

### Layout Management

- Version layouts for backward compatibility
- Validate layouts against component requirements
- Maintain layout fragments for reusable patterns
- Store layouts in a central registry for discovery
- Design layouts with responsive behavior in mind

### Performance

- Apply code splitting to lazy load components
- Use memoization for expensive computations
- Keep context objects small and focused
- Implement proper error boundaries
- Measure and monitor component rendering time

### Testing

- Unit test components in isolation
- Test layout rendering with mock components
- Validate layouts against component contracts
- Test context variable processing
- Use integration tests for component interactions

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## License

MIT © MosaicUI