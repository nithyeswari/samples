# Micro Frontend Dynamic Loading Solution

This solution enables dynamic loading of remote Micro Frontend (MFE) bundles with automatic cache busting and version management. It allows remote applications to update independently without requiring host application rebuilds.

## Key Features

- Dynamic remote bundle loading
- Automatic cache busting using content hashes
- Version tracking through manifest files
- No host rebuilds required for remote updates
- Built-in error handling and retry mechanisms

## Architecture

### Components

1. **Manifest Generator**
   - Webpack plugin that generates manifest.json during remote app builds
   - Tracks bundle names, hashes, and versions
   - Provides a single source of truth for remote entry points

2. **Dynamic Remote Loader**
   - Runtime loader in the host application
   - Fetches manifest and loads appropriate remote bundles
   - Handles version checking and updates

3. **Version Checker**
   - Optional component for monitoring remote updates
   - Provides update notifications or auto-reload functionality

## Setup Instructions

### 1. Remote Application

#### Install Dependencies
```bash
npm install webpack webpack-cli @module-federation/runtime
```

#### Configure Webpack
```javascript
// webpack.config.js
const ModuleFederationPlugin = require('webpack/lib/container/ModuleFederationPlugin');
const ManifestGeneratorPlugin = require('./plugins/ManifestGeneratorPlugin');

module.exports = {
  output: {
    filename: '[name].[contenthash].js',
    publicPath: 'auto'
  },
  plugins: [
    new ModuleFederationPlugin({
      name: 'remote_app',
      filename: 'remoteEntry.[contenthash].js',
      exposes: {
        './RemoteComponent': './src/RemoteComponent'
      },
      shared: {
        react: { singleton: true },
        'react-dom': { singleton: true }
      }
    }),
    new ManifestGeneratorPlugin()
  ]
};
```

### 2. Host Application

#### Install Dependencies
```bash
npm install webpack webpack-cli @module-federation/runtime
```

#### Configure Dynamic Remote Loading
```javascript
// webpack.config.js
module.exports = {
  plugins: [
    new ModuleFederationPlugin({
      name: 'host',
      remotes: {
        remote_app: `promise new Promise(resolve => {
          loadRemoteEntry('remote_app')
            .then(manifest => resolve(window.remote_app));
        })`
      }
    })
  ]
};
```

## Usage

### Loading Remote Components

```javascript
// App.js
import React, { lazy, Suspense } from 'react';

const RemoteComponent = lazy(() => import('remote_app/RemoteComponent'));

function App() {
  return (
    <Suspense fallback="Loading...">
      <RemoteComponent />
    </Suspense>
  );
}
```

### Version Checking

```javascript
// versionCheck.js
const checker = new RemoteVersionChecker();
checker.startChecking('remote_app', 'http://remote-app.example.com');
```

## Deployment Considerations

1. **Cache Headers**
   - Set short cache time or no-cache for manifest.json
   - Use long cache times for content-hashed bundles
   ```nginx
   # Nginx configuration example
   location /manifest.json {
     add_header Cache-Control "no-cache";
   }
   location /static {
     expires 1y;
   }
   ```

2. **Error Handling**
   - Implement retry mechanisms for manifest fetching
   - Provide fallback UI for loading failures
   - Monitor remote loading performance

3. **Version Management**
   - Implement gradual rollout strategies
   - Consider using feature flags for new versions
   - Plan for backward compatibility

## Best Practices

1. **Shared Dependencies**
   - Use singleton mode for critical shared dependencies
   - Keep shared dependency versions in sync
   - Monitor bundle sizes

2. **Performance**
   - Implement lazy loading for remote components
   - Use code splitting effectively
   - Monitor and optimize bundle sizes

3. **Monitoring**
   - Track remote loading success rates
   - Monitor version mismatches
   - Set up alerts for loading failures

## Troubleshooting

### Common Issues

1. **Remote Loading Failures**
   - Check network connectivity
   - Verify manifest.json accessibility
   - Check for CORS issues

2. **Version Mismatches**
   - Verify shared dependency versions
   - Check manifest generation
   - Review version checking implementation

3. **Caching Issues**
   - Verify cache headers
   - Check browser cache settings
   - Review CDN configuration

## API Reference

### ManifestGeneratorPlugin

```javascript
new ManifestGeneratorPlugin({
  filename: 'manifest.json',  // Optional, defaults to manifest.json
  version: process.env.VERSION  // Optional, defaults to package.json version
})
```

### RemoteVersionChecker

```javascript
const checker = new RemoteVersionChecker({
  checkInterval: 60000,  // Optional, defaults to 60 seconds
  onVersionChange: (remoteName, version) => {}  // Optional callback
})
```

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.