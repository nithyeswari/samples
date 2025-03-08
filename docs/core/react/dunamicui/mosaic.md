# MosaicUI Framework

A comprehensive frontend architecture combining micro-frontends with schema-driven UI for building modular, scalable, and consistent web applications.

## Table of Contents

- [Overview](#overview)
- [Core Concepts](#core-concepts)
- [Architecture Patterns](#architecture-patterns)
  - [Micro-Frontend Patterns](#micro-frontend-patterns)
  - [Schema-Driven UI Patterns](#schema-driven-ui-patterns)
- [When to Use Each Approach](#when-to-use-each-approach)
- [Technical Implementation](#technical-implementation)
  - [Micro-Frontend Implementation](#micro-frontend-implementation)
  - [Schema-Driven UI Implementation](#schema-driven-ui-implementation)
  - [Runtime UI Composition](#runtime-ui-composition)
- [Getting Started](#getting-started)
- [Usage Examples](#usage-examples)
- [Development Workflow](#development-workflow)
- [Best Practices](#best-practices)
- [Advanced Techniques](#advanced-techniques)
- [Contributing](#contributing)
- [License](#license)

## Overview

MosaicUI is an advanced frontend framework that combines the flexibility of micro-frontends with the consistency of schema-driven UI. This dual approach enables organizations to balance team autonomy with design consistency, supporting both independent development and coordinated user experiences.

### Key Features

- **Dual-Role Applications**: Every application can both consume and expose components
- **Schema-Validated Components**: Components validate their inputs and outputs
- **Runtime UI Composition**: Dynamic assembly of interfaces from schemas
- **Framework Agnostic**: Support for React, Vue, Angular, and Web Components
- **Contract Testing**: Automated verification of integration points
- **Independent Deployment**: Teams can deploy on their own schedules

## Core Concepts

### Micro-Frontends

Micro-frontends extend microservice principles to frontend development. They allow:

- **Independent Development**: Teams work autonomously on separate parts
- **Technology Flexibility**: Different sections can use different frameworks
- **Targeted Deployments**: Update specific features without rebuilding everything
- **Team Scalability**: Multiple teams can work in parallel

### Schema-Driven UI

Schema-driven UI uses declarative schemas to define interfaces. This enables:

- **Consistency**: Enforced patterns across components
- **Validation**: Runtime checking of component usage
- **Documentation**: Self-documenting interfaces
- **Dynamic Rendering**: UIs that can adapt based on context

### Combined Approach

MosaicUI's unique strength is combining these approaches:

- Use micro-frontends where team autonomy is paramount
- Use schema-driven UI where consistency is critical
- Apply both approaches where appropriate

## Architecture Patterns

### Micro-Frontend Patterns

#### Hub-and-Spoke

Applications serve as both central hubs and specialized spokes.

```
┌──────────────┐     ┌──────────────┐
│              │     │              │
│  Dashboard   │◄────┤   Reports    │
│   (Hub)      │     │   (Spoke)    │
│              │     │              │
└──────┬───────┘     └──────────────┘
       │
       │
┌──────▼───────┐     ┌──────────────┐
│              │     │              │
│   Settings   │◄────┤ Notifications │
│   (Spoke)    │     │   (Spoke)    │
│              │     │              │
└──────────────┘     └──────────────┘
```

**Best for**: Applications where different sections need both centralized navigation and specialized functionality.

#### Federated Marketplace

A central registry where applications publish and discover components.

```
                ┌──────────────────┐
                │                  │
                │   Component      │
                │   Marketplace    │
                │                  │
                └─────┬──────┬─────┘
                      │      │
          ┌───────────┘      └───────────┐
          │                              │
┌─────────▼────────┐          ┌──────────▼─────────┐
│                  │  Import  │                    │
│  Application A   │◄─────────┤   Application B    │
│  (Publisher)     │          │   (Consumer)       │
│                  │          │                    │
└──────────────────┘          └────────────────────┘
```

**Best for**: Large organizations where component reuse across teams is important.

#### Nested Composition

Applications contain other applications in a hierarchical structure.

```
┌───────────────────────────────────────┐
│ Shell Application                     │
│                                       │
│  ┌─────────────────┐ ┌──────────────┐ │
│  │ Domain App A    │ │ Domain App B │ │
│  │                 │ │              │ │
│  │ ┌─────────────┐ │ │ ┌──────────┐ │ │
│  │ │ Feature A-1 │ │ │ │Feature B-1│ │ │
│  │ └─────────────┘ │ │ └──────────┘ │ │
│  └─────────────────┘ └──────────────┘ │
└───────────────────────────────────────┘
```

**Best for**: Complex applications with hierarchical domain structure.

#### Event-Driven Composition

Applications communicate through events rather than direct imports.

```
┌────────────────────────────────────────┐
│              Event Bus                 │
└───────┬─────────────┬─────────────┬────┘
        │             │             │
┌───────▼─────┐ ┌─────▼───────┐ ┌───▼───────────┐
│ Auth App    │ │ Profile App │ │ Analytics App │
│ (Publisher) │ │ (Both)      │ │ (Subscriber)  │
└─────────────┘ └─────────────┘ └───────────────┘
```

**Best for**: Applications where loose coupling between components is essential.

#### Domain-Driven Verticals

Each team owns a complete vertical slice of functionality.

```
┌───────────┐ ┌───────────┐ ┌───────────┐
│ Catalog   │ │ Checkout  │ │ User      │
│ Frontend  │ │ Frontend  │ │ Frontend  │
├───────────┤ ├───────────┤ ├───────────┤
│ Catalog   │ │ Checkout  │ │ User      │
│ Backend   │ │ Backend   │ │ Backend   │
└───────────┘ └───────────┘ └───────────┘
    Team A       Team B        Team C
```

**Best for**: Organizations with teams aligned to business domains.

#### API-Gateway Driven Composition

Frontend components are composed by an API gateway.

```
┌───────────────────┐
│                   │
│    API Gateway    │
│    (BFF + UI)     │
│                   │
└─────┬──────┬──────┘
      │      │
┌─────▼──┐ ┌─▼──────┐
│ UI     │ │ Data   │
│ Comps  │ │ APIs   │
└────────┘ └────────┘
```

**Best for**: Applications where backends need significant control over frontend composition.

#### Shadow DOM Isolation

Using web component technologies for strong isolation.

```
┌────────────────────────────────────┐
│ Container Application              │
│                                    │
│ ┌──────────────┐  ┌──────────────┐ │
│ │ #shadow-root │  │ #shadow-root │ │
│ │  Component A │  │  Component B │ │
│ │  (React)     │  │  (Vue)       │ │
│ └──────────────┘  └──────────────┘ │
└────────────────────────────────────┘
```

**Best for**: Applications integrating components from multiple sources where style isolation is critical.

### Schema-Driven UI Patterns

#### Contract-Driven Components

Components define contracts for their interfaces using schemas.

```javascript
// Component schema contract
{
  "type": "object",
  "properties": {
    "data": {
      "type": "object",
      "properties": {
        "id": { "type": "number" },
        "name": { "type": "string" }
      },
      "required": ["id", "name"]
    },
    "onUpdate": { "type": "function" }
  },
  "required": ["data"]
}
```

**Best for**: Ensuring consistent component usage across teams.

#### Runtime UI Composition

UIs are assembled at runtime from schema definitions.

```javascript
// UI schema
{
  "component": "Layout",
  "children": [
    {
      "component": "Header",
      "props": { "title": "Dashboard" }
    },
    {
      "component": "Content",
      "children": [
        {
          "component": "Card",
          "props": { "title": "Statistics" },
          "children": [...]
        }
      ]
    }
  ]
}
```

**Best for**: Applications where UI structure needs to be determined at runtime.

#### Server-Driven UI

Backend services control UI layout and behavior.

```
┌──────────────┐    ┌──────────────┐
│              │    │              │
│   Backend    │───►│  Frontend    │
│   (schema)   │    │  (renderer)  │
│              │    │              │
└──────────────┘    └──────────────┘
```

**Best for**: Applications where UI needs to adapt based on server-side state or permissions.

#### Adaptive Layouts

UIs that adapt based on user context and permissions.

```javascript
// Permission-based schema
{
  "component": "UserAdmin",
  "requiredPermission": "admin",
  "fallback": {
    "component": "RestrictedAccess",
    "props": { "message": "Admin access required" }
  }
}
```

**Best for**: Applications with complex permission requirements.

## When to Use Each Approach

### Micro-Frontends Excel When:

- **Team Autonomy**: Independent teams need to work at their own pace
- **Technology Diversity**: Different parts need different frameworks or libraries
- **Scale**: Large applications with many developers working simultaneously
- **Organizational Structure**: Teams aligned to business domains rather than technical layers
- **Gradual Migration**: Transitioning from monolith to microservices architecture
- **Independent Deployment**: Features need to be deployed independently
- **Technical Diversity**: Different parts of the application have different technical requirements

### Schema-Driven UI Excels When:

- **Consistency**: Uniform look and feel is essential across the application
- **Dynamic Interfaces**: UIs need to adapt based on user roles or context
- **Backend Control**: Server needs to drive what's displayed in the UI
- **Rapid Iteration**: UI layouts change frequently based on business needs
- **Form-Heavy Applications**: Applications with many forms and data entry screens
- **A/B Testing**: Different layouts need to be tested with different user groups
- **Personalization**: Interfaces need to adapt to user preferences
- **Permission-Based UIs**: Different users see different capabilities
- **Content Management**: Non-developers need to influence layouts
- **Dynamic UI Generation**: UI structure determined at runtime
- **Global UI Changes**: Changes need to be deployed quickly across the application

### Hybrid Approaches Work Best When:

- **Mixed Requirements**: Some parts need consistency, others need autonomy
- **Gradual Adoption**: Transitioning from one architecture to another
- **Complex Domain Rules**: Different sections have different technical constraints
- **Team Evolution**: Teams are at different maturity levels in adoption
- **Legacy Integration**: New architecture must coexist with legacy code

## Technical Implementation

### Micro-Frontend Implementation

#### Module Federation

Webpack 5's Module Federation is the primary technology for implementing micro-frontends:

```javascript
// webpack.config.js for a dual-role application
const { ModuleFederationPlugin } = require('webpack').container;

module.exports = {
  // webpack config...
  plugins: [
    new ModuleFederationPlugin({
      name: 'dualApp',
      
      // Exposing components (acting as remote)
      exposes: {
        './Button': './src/components/Button',
        './UserProfile': './src/components/UserProfile',
      },
      
      // Consuming remote components (acting as shell)
      remotes: {
        authApp: 'authApp@http://localhost:3001/remoteEntry.js',
        analyticsApp: 'analyticsApp@http://localhost:3002/remoteEntry.js',
      },
      
      shared: ['react', 'react-dom'],
    }),
  ],
};
```

#### Loading Remote Components

```javascript
// In your shell application
import React, { lazy, Suspense } from 'react';

// Loading remote components dynamically
const RemoteButton = lazy(() => import('authApp/Button'));
const RemoteUserProfile = lazy(() => import('authApp/UserProfile'));

function App() {
  return (
    <div>
      <h1>Shell Application</h1>
      <Suspense fallback={<div>Loading Button...</div>}>
        <RemoteButton onClick={() => console.log('Clicked')}>
          Login
        </RemoteButton>
      </Suspense>
      
      <Suspense fallback={<div>Loading Profile...</div>}>
        <RemoteUserProfile userId="123" />
      </Suspense>
    </div>
  );
}
```

### Schema-Driven UI Implementation

#### The `withSchema` Higher-Order Component

The `withSchema` function is a higher-order component that connects React components with their schema definitions:

```javascript
// src/components/UserProfile.js
import React from 'react';
import { withSchema } from '@modularui/core';

function UserProfile({ data, onUpdate }) {
  return (
    <div>
      <h2>{data.fullName}</h2>
      <p>Email: {data.email}</p>
      <button onClick={() => onUpdate(data.id)}>Update</button>
    </div>
  );
}

// Define component schema
const schema = {
  type: 'object',
  properties: {
    data: {
      type: 'object',
      properties: {
        id: { type: 'number' },
        fullName: { type: 'string' },
        email: { type: 'string' },
      },
      required: ['id', 'fullName', 'email']
    },
    onUpdate: { type: 'function' }
  },
  required: ['data', 'onUpdate'],
};

export default withSchema(UserProfile, schema);
```

#### What `withSchema` Does

The `withSchema` HOC provides several key functions:

1. **Schema Validation**: Validates props against the schema at runtime
   ```javascript
   // Inside withSchema HOC
   function WithSchema(props) {
     const validationResult = validateProps(props, userProfileSchema);
     
     if (!validationResult.valid) {
       console.error(
         "Invalid props passed to UserProfile",
         validationResult.errors
       );
     }
     
     return <UserProfile {...props} />;
   }
   ```

2. **Documentation**: Self-documents the component's interface

3. **Contract Enforcement**: Ensures consumers use the component correctly, providing errors like:
   ```
   ERROR: Invalid props passed to UserProfile: 
   Missing required property 'data.fullName'
   Expected: { id: number, fullName: string, email: string }
   Received: { id: 123, name: "John Doe", email: "john@example.com" }
   ```

4. **Schema Registry**: Registers the schema for discovery by other teams

### Runtime UI Composition

MosaicUI includes a powerful runtime UI composition engine for dynamic rendering from schemas:

```javascript
// Example of a UI schema
const dashboardSchema = {
  type: 'layout',
  component: 'DashboardLayout',
  children: [
    {
      type: 'component',
      component: 'Card',
      props: {
        title: 'User Stats',
        width: 'half'
      },
      children: [
        {
          type: 'component',
          component: 'UserStatsChart',
          props: {
            dataSource: '/api/user-stats',
            refreshInterval: 60000
          }
        }
      ]
    },
    {
      type: 'component',
      component: 'Card',
      props: {
        title: 'Recent Activity',
        width: 'half'
      },
      children: [
        {
          type: 'component',
          component: 'ActivityFeed',
          props: {
            limit: 5,
            showTimestamp: true
          }
        }
      ]
    }
  ]
};
```

#### Dynamic Renderer Implementation

```javascript
// The DynamicRenderer component
import React from 'react';
import { ComponentRegistry } from '@modularui/core';

function DynamicRenderer({ schema, context }) {
  if (!schema) return null;
  
  // Get the component from registry
  const Component = ComponentRegistry.get(schema.component);
  if (!Component) {
    console.error(`Component ${schema.component} not found in registry`);
    return null;
  }
  
  // Process props with context variables
  const processedProps = processProps(schema.props, context);
  
  // Recursively render children
  const children = Array.isArray(schema.children)
    ? schema.children.map((childSchema, index) => (
        <DynamicRenderer 
          key={index} 
          schema={childSchema} 
          context={context} 
        />
      ))
    : null;
  
  // Render the component with its children
  return <Component {...processedProps}>{children}</Component>;
}

// Helper to process props with context variables
function processProps(props, context) {
  if (!props) return {};
  
  return Object.entries(props).reduce((result, [key, value]) => {
    // Handle special syntax for context variables
    if (typeof value === 'string' && value.startsWith('$context.')) {
      const path = value.slice(9).split('.');
      result[key] = path.reduce(
        (obj, key) => (obj && obj[key] !== undefined ? obj[key] : null),
        context
      );
    } else {
      result[key] = value;
    }
    return result;
  }, {});
}
```

#### Server-Driven UI Example

```javascript
// Component to fetch and render UI from schema
function ServerDrivenUI({ endpoint, fallback }) {
  const [schema, setSchema] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    async function fetchSchema() {
      try {
        setLoading(true);
        const response = await fetch(endpoint);
        if (!response.ok) {
          throw new Error(`HTTP error ${response.status}`);
        }
        const schema = await response.json();
        setSchema(schema);
        setError(null);
      } catch (err) {
        setError(err.message);
        setSchema(null);
      } finally {
        setLoading(false);
      }
    }
    
    fetchSchema();
  }, [endpoint]);
  
  if (loading) return <div>Loading UI...</div>;
  if (error) return fallback || <div>Error loading UI: {error}</div>;
  if (!schema) return fallback || <div>No UI schema available</div>;
  
  return (
    <DynamicRenderer 
      schema={schema}
      context={{
        user: currentUser,
        permissions: userPermissions,
        theme: currentTheme
      }}
    />
  );
}

// Usage
function App() {
  return (
    <ServerDrivenUI 
      endpoint="/api/ui-schema/dashboard" 
      fallback={<DefaultDashboard />}
    />
  );
}
```

## Getting Started

### Prerequisites

- Node.js 18+ and npm/yarn
- Webpack 5+ for Module Federation
- Understanding of React (or your chosen framework)

### Installation

```bash
# Install core framework
npm install @mosaicui/core

# Install schema registry
npm install @mosaicui/schema-registry

# Install webpack plugin
npm install @mosaicui/webpack-plugin --save-dev
```

### Project Structure

```
my-app/
├── src/
│   ├── components/       # Local components
│   │   ├── Button.jsx    # Example component
│   │   └── UserProfile.jsx
│   ├── containers/       # Business logic containers
│   ├── remotes/          # Remote component wrappers
│   ├── schemas/          # Component schemas
│   │   ├── Button.schema.json
│   │   └── UserProfile.schema.json
│   └── index.js          # App entry point
├── webpack.config.js     # Federation configuration
└── package.json
```

### Basic Configuration

```javascript
// webpack.config.js
const { MosaicUIPlugin } = require('@mosaicui/webpack-plugin');

module.exports = {
  // ...webpack config
  plugins: [
    new MosaicUIPlugin({
      appName: 'my-app',
      exposes: {
        './UserProfile': './src/components/UserProfile',
        './ActivityFeed': './src/features/ActivityFeed',
      },
      remotes: {
        analytics: 'analytics@http://localhost:3001/remoteEntry.js',
        settings: 'settings@http://localhost:3002/remoteEntry.js',
      },
      schemas: './src/schemas',
    }),
  ],
};
```

## Usage Examples

### Creating a Remote Component

```javascript
// src/components/UserProfile.js
import React from 'react';
import { withSchema } from '@modularui/core';

function UserProfile({ data, onUpdate }) {
  // Component implementation
  return (
    <div>
      <h2>{data.fullName}</h2>
      <p>Role: {data.role}</p>
      <button onClick={() => onUpdate({...data, lastActive: new Date()})}>
        Update Last Active
      </button>
    </div>
  );
}

// Define schema
const schema = {
  type: 'object',
  properties: {
    data: {
      type: 'object',
      properties: {
        id: { type: 'number' },
        fullName: { type: 'string' },
        role: { type: 'string' },
      },
      required: ['id', 'fullName']
    },
    onUpdate: { type: 'function' }
  },
  required: ['data'],
};

export default withSchema(UserProfile, schema);
```

### Consuming a Remote Component

```javascript
// src/App.js
import React, { lazy, Suspense } from 'react';
import { SchemaProvider } from '@modularui/core';

const RemoteUserProfile = lazy(() => import('userApp/UserProfile'));

function App() {
  return (
    <SchemaProvider>
      <Suspense fallback={<div>Loading...</div>}>
        <RemoteUserProfile 
          data={{ 
            id: 123, 
            fullName: 'John Doe', 
            role: 'Admin' 
          }}
          onUpdate={(userData) => console.log('Updated:', userData)}
        />
      </Suspense>
    </SchemaProvider>
  );
}
```

### Creating a Dynamic UI

```javascript
// Dynamic UI with context-aware components
function DynamicDashboard({ user }) {
  // This schema could come from an API
  const dashboardSchema = {
    component: 'Layout',
    props: { type: 'dashboard' },
    children: [
      {
        component: 'Header',
        props: { 
          title: 'Welcome',
          userName: '$context.user.name'  // Dynamic value from context
        }
      },
      {
        component: 'Content',
        children: [
          // Show admin panel only if user has admin role
          user.role === 'admin' ? {
            component: 'AdminPanel',
            props: { userId: user.id }
          } : null,
          {
            component: 'RecentActivity',
            props: { limit: 5 }
          }
        ].filter(Boolean)  // Filter out null components
      }
    ]
  };

  return (
    <DynamicRenderer 
      schema={dashboardSchema}
      context={{ user }}
    />
  );
}
```

### Schema Registry

The schema registry provides a centralized repository for UI schemas:

```javascript
// src/schemas/index.js
import { registerSchema } from '@modularui/schema-registry';

// Register component schemas
registerSchema('UserProfile', require('./UserProfile.schema.json'));
registerSchema('ActivityFeed', require('./ActivityFeed.schema.json'));

// Register UI schemas
registerSchema('dashboard', require('./ui/dashboard.schema.json'));
registerSchema('settings', require('./ui/settings.schema.json'));
```

## Development Workflow

1. **Define Schemas**: Start by defining the schema contracts for your components
2. **Implement Components**: Build components to fulfill the schema contracts
3. **Register Schemas**: Add schemas to the central registry
4. **Configure Federation**: Set up webpack to expose and consume components
5. **Test Integration**: Verify components work correctly across application boundaries
6. **Deploy Independently**: Each team can deploy their own micro-frontend

### Testing Schema Compliance

```javascript
// test/UserProfile.test.js
import { testComponentContract } from '@mosaicui/testing';
import UserProfile from '../src/components/UserProfile';

describe('UserProfile', () => {
  it('should conform to its schema contract', () => {
    const result = testComponentContract(UserProfile);
    expect(result.valid).toBe(true);
  });
  
  it('should reject invalid props', () => {
    const invalidProps = {
      data: {
        id: '123', // Should be number, not string
        name: 'John' // Missing fullName
      }
    };
    
    const result = testComponentContract(UserProfile, invalidProps);
    expect(result.valid).toBe(false);
    expect(result.errors).toContain('data.id should be number');
    expect(result.errors).toContain('data should have required property fullName');
  });
});
```

## Best Practices

### Team Organization

- **Domain-Oriented Teams**: Organize teams around business domains rather than technologies
- **Cross-Functional Ownership**: Teams own full features, not just UI or backend components
- **Schema Governance**: Establish a process for evolving shared schemas
- **Component Standards**: Create shared component design standards
- **Integration Testing**: Invest in automated testing for integration points

### Component Design

- **Single Responsibility**: Keep components focused on one specific job
- **Clear Contracts**: Define explicit input/output contracts with schemas
- **Stateless When Possible**: Prefer stateless components for easier integration
- **Consistent Naming**: Use consistent naming conventions across teams
- **Documentation**: Include usage examples in component documentation

### Schema Management

- **Versioning**: Use semantic versioning for schema changes
- **Backward Compatibility**: Maintain backward compatibility where possible
- **Schema Review**: Review schema changes across teams
- **Testing**: Test schemas against actual component implementations
- **Schema Registry**: Centralize schema definitions for discoverability

### Deployment Strategies

- **Independent Pipelines**: Each micro-frontend has its own CI/CD pipeline
- **Versioned Artifacts**: Deploy versioned components to a registry
- **Canary Deployments**: Test changes with a small user subset first
- **Feature Flags**: Use feature flags to control feature availability
- **Rollback Plan**: Ensure easy rollback procedures for each deployment

## Advanced Techniques

### State Choreography

Implement decentralized state management where each micro-frontend maintains its own state but coordinates through events.

```javascript
// Shared event bus
class EventBus {
  constructor() {
    this.events = {};
  }
  
  subscribe(event, callback) {
    if (!this.events[event]) {
      this.events[event] = [];
    }
    this.events[event].push(callback);
    
    return () => {
      this.events[event] = this.events[event].filter(cb => cb !== callback);
    };
  }
  
  publish(event, data) {
    if (!this.events[event]) return;
    this.events[event].forEach(callback => callback(data));
  }
}

// Create a shared event bus
const globalEventBus = window.globalEventBus || new EventBus();
window.globalEventBus = globalEventBus;

// In micro-frontend A
function ProfileMicroFrontend() {
  const [userData, setUserData] = useState(null);
  
  useEffect(() => {
    // Listen for user data updates from other micro-frontends
    const unsubscribe = globalEventBus.subscribe('user:updated', (newUserData) => {
      setUserData(newUserData);
    });
    
    return unsubscribe;
  }, []);
  
  function updateUserProfile(newData) {
    // Update local state
    setUserData(newData);
    
    // Notify other micro-frontends
    globalEventBus.publish('user:updated', newData);
  }
  
  // Component rendering...
}

// In micro-frontend B
function NotificationsMicroFrontend() {
  useEffect(() => {
    // React to user updates from other micro-frontends
    const unsubscribe = globalEventBus.subscribe('user:updated', (userData) => {
      showNotification(`Profile updated: ${userData.fullName}`);
    });
    
    return unsubscribe;
  }, []);
  
  // Component rendering...
}
```

### Micro-Experience Portals

Portal-like experiences where each "portal" is a complete micro-frontend with its own user experience.

```javascript
// Container for multiple micro-experience portals
function PortalContainer() {
  const [activePortalId, setActivePortalId] = useState('dashboard');
  
  // Map of portal IDs to their component loaders
  const portalMap = {
    dashboard: () => import('dashboard/Portal'),
    analytics: () => import('analytics/Portal'),
    settings: () => import('settings/Portal'),
    help: () => import('help/Portal')
  };
  
  // Dynamically load the active portal
  const ActivePortal = lazy(portalMap[activePortalId]);
  
  return (
    <div className="portal-container">
      <nav className="portal-nav">
        {Object.keys(portalMap).map(portalId => (
          <button
            key={portalId}
            className={activePortalId === portalId ? 'active' : ''}
            onClick={() => setActivePortalId(portalId)}
          >
            {portalId}
          </button>
        ))}
      </nav>
      
      <div className="portal-content">
        <Suspense fallback={<div>Loading portal...</div>}>
          <ActivePortal />
        </Suspense>
      </div>
    </div>
  );
}
```

### Federation Marketplace

A central repository where teams can publish components for discovery by others.

```javascript
// Publishing a component to the marketplace
import { publishComponent } from '@modularui/marketplace';

publishComponent({
  name: 'UserProfile',
  version: '1.2.0',
  schema: require('./UserProfile.schema.json'),
  documentation: require('./UserProfile.docs.md'),
  tags: ['user', 'profile', 'admin'],
  team: 'user-management'
});

// Discovering components from the marketplace
import { discoverComponents } from '@mosaicui/marketplace';

const userComponents = await discoverComponents({
  tags: ['user'],
  minVersion: '1.0.0'
});
```

### Edge Composition

Assemble micro-frontends at CDN edge servers for improved performance.

```javascript
// Edge worker script
addEventListener('fetch', event => {
  event.respondWith(handleRequest(event.request))
})

async function handleRequest(request) {
  // Get user context from request
  const userContext = getUserContext(request);
  
  // Fetch base HTML
  const baseHtml = await fetchBaseHtml();
  
  // Determine which micro-frontends to include based on user context
  const microFrontends = determineMicroFrontends(userContext);
  
  // Inject micro-frontend references into HTML
  const composedHtml = injectMicroFrontends(baseHtml, microFrontends);
  
  return new Response(composedHtml, {
    headers: { 'content-type': 'text/html' },
  })
}
```

### Progressive Web App (PWA) Federation

Combine PWA capabilities with micro-frontends for offline-capable modular applications.

```javascript
// Service worker for micro-frontend
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open('my-micro-frontend-v1').then((cache) => {
      return cache.addAll([
        './index.js',
        './styles.css',
        './assets/logo.png'
      ]);
    })
  );
});

// Shell application service worker
self.addEventListener('fetch', (event) => {
  // Special handling for micro-frontend requests
  if (event.request.url.includes('/remoteEntry.js')) {
    event.respondWith(
      caches.match(event.request).then((cachedResponse) => {
        return cachedResponse || fetch(event.request).then((response) => {
          // Cache for offline use
          const responseClone = response.clone();
          caches.open('micro-frontends-cache').then((cache) => {
            cache.put(event.request, responseClone);
          });
          return response;
        }).catch(() => {
          // Return fallback for offline
          return caches.match('/offline-fallback.js');
        });
      })
    );
  }
});
```

### Feature Toggles Network

Distribute feature flag configurations across micro-frontend boundaries.

```javascript
// Feature toggle service
class FeatureToggleService {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
    this.features = {};
    this.subscribers = new Map();
  }
  
  async fetchFeatures() {
    const response = await fetch(`${this.baseUrl}/features`);
    this.features = await response.json();
    this.notifySubscribers();
  }
  
  isEnabled(featureKey) {
    return this.features[featureKey] === true;
  }
  
  subscribe(featureKey, callback) {
    if (!this.subscribers.has(featureKey)) {
      this.subscribers.set(featureKey, new Set());
    }
    this.subscribers.get(featureKey).add(callback);
    
    // Return unsubscribe function
    return () => {
      this.subscribers.get(featureKey).delete(callback);
    };
  }
  
  notifySubscribers() {
    for (const [featureKey, callbacks] of this.subscribers.entries()) {
      const isEnabled = this.isEnabled(featureKey);
      for (const callback of callbacks) {
        callback(isEnabled);
      }
    }
  }
}

// Usage in a micro-frontend
const featureService = new FeatureToggleService('/api');
featureService.subscribe('new-user-profile', (enabled) => {
  if (enabled) {
    loadNewUserProfileComponent();
  } else {
    loadLegacyUserProfileComponent();
  }
});