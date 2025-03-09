# MosaicUI

<div align="center">

![MosaicUI Logo](https://via.placeholder.com/300x100?text=MosaicUI)

**A unified micro-frontend framework with JSON-driven UI for building modular web applications**

[![npm version](https://img.shields.io/npm/v/mosaicui.svg?style=flat)](https://www.npmjs.com/package/mosaicui)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

</div>

## Overview

MosaicUI is an all-in-one library that combines micro-frontend architecture with JSON-driven UI rendering. It allows you to build modular applications where the interface can be defined and modified using simple JSON structures, while components can be shared across multiple applications.

## Features

- 🧩 **Micro-frontend architecture** - Build applications as independent, composable modules
- 🔄 **JSON-driven rendering** - Define UIs with simple JSON layouts
- 🔌 **Pluggable component system** - Register and use components dynamically
- 🌐 **Server-driven UI** - Update layouts from the backend without deploying frontend code
- 🎭 **Framework agnostic** - Works with React, Vue, and Angular
- 🔧 **Webpack Module Federation** - Built-in integration with module federation
- 📦 **All-in-one package** - Core renderer, layout management, and webpack tools in a single library

## Installation

```bash
# Install the main package
npm install mosaicui

# Install peer dependencies if needed
npm install react react-dom webpack
```

## Quick Start

### 1. Register Components

```jsx
// src/components/index.js
import { registerComponents } from 'mosaicui';
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
import { DynamicRenderer } from 'mosaicui';
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

### 4. Setup Webpack for Micro-Frontend Support

```js
// webpack.config.js
const { MosaicUIPlugin } = require('mosaicui');

module.exports = {
  // ... other webpack config
  plugins: [
    new MosaicUIPlugin({
      appName: 'host',
      exposes: {
        './Header': './src/components/Header',
        './Footer': './src/components/Footer',
      },
      remotes: {
        shop: 'shop@https://shop.example.com/remoteEntry.js',
        checkout: 'checkout@https://checkout.example.com/remoteEntry.js'
      },
      // Auto-register layouts from a directory
      layouts: './src/layouts',
      // Auto-discover and register components
      autoRegisterComponents: true,
      componentsDir: './src/components'
    })
  ]
};
```

## API Reference

### Core Rendering

#### `DynamicRenderer`

Renders components based on a JSON layout definition.

```jsx
import { DynamicRenderer } from 'mosaicui';

// Basic usage
<DynamicRenderer layout={myLayout} context={myContext} />

// With custom component registry
<DynamicRenderer 
  layout={myLayout} 
  context={myContext} 
  componentRegistry={myCustomRegistry} 
/>
```

#### `ComponentRegistry`

Manages component registration and retrieval.

```jsx
import { ComponentRegistry, registerComponents } from 'mosaicui';

// Create a custom registry
const customRegistry = new ComponentRegistry();
customRegistry.register('MyComponent', MyComponent);

// Global registration
registerComponents({
  Header,
  Footer,
  ProductCard
});
```

### Layout Management

#### `registerLayout`

Registers a layout for later use.

```jsx
import { registerLayout } from 'mosaicui';

// Register a layout
registerLayout('homepage', homepageLayout);

// Use registered layout
<DynamicRenderer layoutName="homepage" context={context} />
```

#### `composeLayouts`

Combines multiple layouts into one.

```jsx
import { composeLayouts } from 'mosaicui';

const composedLayout = composeLayouts(baseLayout, {
  contentSlot: 'main-content',
  content: myContentLayout
});
```

#### `transformLayout`

Modifies a layout by applying a transformation function.

```jsx
import { transformLayout } from 'mosaicui';

// Add analytics tracking to all buttons
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

### React Utilities

#### `useRemoteLayout`

Hook for fetching layouts from a remote endpoint.

```jsx
import { useRemoteLayout, DynamicRenderer } from 'mosaicui';

function App() {
  const { layout, loading, error } = useRemoteLayout('/api/layouts/homepage');
  
  if (loading) return <div>Loading UI...</div>;
  if (error) return <div>Error: {error}</div>;
  
  return <DynamicRenderer layout={layout} context={context} />;
}
```

### Webpack Integration

#### `MosaicUIPlugin`

Webpack plugin for module federation and automatic component/layout registration.

```js
const { MosaicUIPlugin } = require('mosaicui');

// Basic usage
new MosaicUIPlugin({
  appName: 'my-app',
  exposes: { /* ... */ },
  remotes: { /* ... */ }
})

// Full configuration
new MosaicUIPlugin({
  appName: 'catalog-app',
  exposes: {
    './ProductCard': './src/components/ProductCard',
    './CategoryList': './src/components/CategoryList'
  },
  remotes: {
    checkout: 'checkout@https://checkout.example.com/remoteEntry.js',
    account: 'account@https://account.example.com/remoteEntry.js'
  },
  layouts: './src/layouts',
  autoRegisterComponents: true,
  componentsDir: './src/components',
  shared: ['lodash', 'styled-components'],
  devTools: true
})
```

## MosaicUI Plugin Implementation

The MosaicUI Webpack Plugin is included in the library and handles micro-frontend setup:

```javascript
// Inside mosaicui/src/webpack/MosaicUIPlugin.js
const { ModuleFederationPlugin } = require('webpack').container;
const path = require('path');
const fs = require('fs');
const glob = require('glob');

class MosaicUIPlugin {
  constructor(options) {
    this.options = Object.assign({
      // Defaults
      shared: [],
      devTools: process.env.NODE_ENV !== 'production',
      autoRegisterComponents: false
    }, options);
    
    // Validate required options
    if (!this.options.appName) {
      throw new Error('MosaicUIPlugin: appName is required');
    }
  }
  
  apply(compiler) {
    const { 
      appName, 
      exposes, 
      remotes, 
      shared, 
      layouts,
      autoRegisterComponents,
      componentsDir,
      devTools 
    } = this.options;
    
    // 1. Configure Module Federation
    new ModuleFederationPlugin({
      name: appName,
      filename: 'remoteEntry.js',
      exposes: exposes || {},
      remotes: remotes || {},
      shared: [
        { 
          react: { 
            singleton: true, 
            requiredVersion: false 
          }
        },
        { 
          'react-dom': { 
            singleton: true, 
            requiredVersion: false 
          }
        },
        'mosaicui',
        ...shared
      ]
    }).apply(compiler);
    
    // 2. Handle Layout Registration
    if (layouts) {
      const layoutsPath = path.resolve(compiler.context, layouts);
      
      if (fs.existsSync(layoutsPath)) {
        // Find all JSON layout files
        const layoutFiles = glob.sync(path.join(layoutsPath, '**/*.json'));
        
        // Generate registration code
        let registrationCode = `
          // Auto-generated layout registration
          import { registerLayout } from 'mosaicui';
          
        `;
        
        layoutFiles.forEach(file => {
          const relativePath = path.relative(compiler.context, file).replace(/\\/g, '/');
          const layoutName = path.basename(file, '.json');
          registrationCode += `registerLayout('${layoutName}', require('./${relativePath}'));\n`;
        });
        
        // Create a virtual module for registration
        const { RawSource } = compiler.webpack.sources;
        const virtualModuleName = 'mosaicui-layouts.js';
        
        compiler.hooks.thisCompilation.tap('MosaicUIPlugin', compilation => {
          compilation.hooks.processAssets.tap(
            {
              name: 'MosaicUIPlugin',
              stage: compiler.webpack.Compilation.PROCESS_ASSETS_STAGE_ADDITIONS
            },
            () => {
              compilation.emitAsset(
                virtualModuleName,
                new RawSource(registrationCode)
              );
            }
          );
        });
        
        // Add entry point to ensure the virtual module gets loaded
        if (compiler.options.entry.main) {
          const originalEntry = compiler.options.entry.main;
          compiler.options.entry.main = {
            import: [
              virtualModuleName,
              ...(Array.isArray(originalEntry.import) ? originalEntry.import : [originalEntry.import])
            ]
          };
        }
      }
    }
    
    // 3. Component Auto-Registration (implementation continued...)
    if (autoRegisterComponents && componentsDir) {
      // Implementation details for component discovery and registration
    }
  }
}
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

## Advanced Usage

### Using Remote Components

```jsx
import React, { lazy, Suspense } from 'react';
import { registerComponents, DynamicRenderer } from 'mosaicui';

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

### Custom Component Registry

```jsx
import { ComponentRegistry, DynamicRenderer } from 'mosaicui';

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

## Browser Support

MosaicUI supports all modern browsers and IE11 with appropriate polyfills.

## TypeScript Support

MosaicUI includes TypeScript definitions for all APIs.

```typescript
// Example TypeScript usage
import { DynamicRenderer, Layout, ComponentRegistry } from 'mosaicui';

interface MyContext {
  user: {
    name: string;
    role: string;
  };
  products: Array<{
    id: number;
    name: string;
    price: number;
  }>;
}

const myLayout: Layout = {
  component: 'Container',
  children: [...]
};

<DynamicRenderer<MyContext> 
  layout={myLayout} 
  context={myContext} 
/>;
```

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## License

MIT © MosaicUI