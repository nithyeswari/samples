# Understanding Experience APIs (BFF Pattern)

## Core Benefits

### 1. Optimized Data Transfer
```javascript
// Without Experience API - Multiple API calls required
const DashboardWithoutBFF = () => {
  const [userData, setUserData] = useState(null);
  const [preferences, setPreferences] = useState(null);
  const [notifications, setNotifications] = useState(null);

  useEffect(() => {
    // Multiple API calls
    const fetchData = async () => {
      const user = await fetch('/api/user');
      const prefs = await fetch('/api/preferences');
      const notifs = await fetch('/api/notifications');
      
      setUserData(user);
      setPreferences(prefs);
      setNotifications(notifs);
    };
    fetchData();
  }, []);
};

// With Experience API - Single optimized call
const DashboardWithBFF = () => {
  const [dashboardData, setDashboardData] = useState(null);

  useEffect(() => {
    // Single API call returning optimized payload
    const fetchData = async () => {
      const data = await fetch('/api/dashboard');
      setDashboardData(data);
    };
    fetchData();
  }, []);
};
```

### 2. Device-Specific Optimization

```javascript
// Experience API Implementation
class DashboardExperienceAPI {
  async getDashboardData(device) {
    const baseData = await this.fetchBaseData();
    
    switch(device) {
      case 'mobile':
        return this.optimizeForMobile(baseData);
      case 'tablet':
        return this.optimizeForTablet(baseData);
      case 'desktop':
        return this.optimizeForDesktop(baseData);
      default:
        return baseData;
    }
  }

  optimizeForMobile(data) {
    return {
      ...data,
      images: data.images.map(img => this.compressImage(img)),
      layout: 'single-column',
      features: data.features.filter(f => f.supportsMobile)
    };
  }
}
```

### 3. Error Handling and Response Transformation

```javascript
// Experience API Error Handling
class ExperienceAPIHandler {
  async handleRequest(req, res) {
    try {
      // Aggregate multiple backend services
      const [userData, orderData, analyticsData] = await Promise.all([
        userService.getData(),
        orderService.getData(),
        analyticsService.getData()
      ]);

      // Transform response for client
      const transformedData = this.transformResponse({
        user: userData,
        orders: orderData,
        analytics: analyticsData
      });

      return res.json(transformedData);

    } catch (error) {
      // Standardized error handling
      return res.status(500).json({
        error: this.normalizeError(error),
        userMessage: 'Unable to fetch dashboard data',
        correlationId: req.correlationId
      });
    }
  }

  normalizeError(error) {
    // Standardize error format across different services
    return {
      code: error.code || 'UNKNOWN_ERROR',
      message: error.message,
      source: error.service || 'unknown',
      timestamp: new Date().toISOString()
    };
  }
}
```

### 4. Version Management and API Evolution

```javascript
// Experience API Version Management
class VersionedExperienceAPI {
  async handleRequest(version, req) {
    switch(version) {
      case 'v2':
        return this.handleV2Request(req);
      case 'v1':
        return this.handleV1Request(req);
      default:
        throw new Error('Unsupported API version');
    }
  }

  async handleV2Request(req) {
    // New optimized implementation
    const data = await this.fetchEnhancedData();
    return this.transformV2Response(data);
  }

  async handleV1Request(req) {
    // Legacy support
    const data = await this.fetchLegacyData();
    return this.transformV1Response(data);
  }
}
```

### 5. Security and Authentication

```javascript
// Experience API Security Layer
class SecureExperienceAPI {
  constructor() {
    this.tokenManager = new TokenManager();
    this.rateLimiter = new RateLimiter();
  }

  async handleRequest(req) {
    // Rate limiting
    if (!this.rateLimiter.allowRequest(req.ip)) {
      throw new RateLimitError();
    }

    // Token transformation
    const clientToken = req.headers.authorization;
    const serviceToken = await this.tokenManager
      .transformToken(clientToken, 'service-a');

    // Secure service call
    const serviceResponse = await fetch(SERVICE_URL, {
      headers: {
        Authorization: `Bearer ${serviceToken}`
      }
    });

    return this.secureResponse(serviceResponse);
  }
}
```

## Implementation Patterns

### 1. GraphQL Experience API

```javascript
const typeDefs = gql`
  type DashboardData {
    user: User
    recentOrders: [Order]
    notifications: [Notification]
    analytics: Analytics
  }

  type Query {
    dashboard(device: String!): DashboardData
  }
`;

const resolvers = {
  Query: {
    dashboard: async (_, { device }, context) => {
      const data = await Promise.all([
        context.dataSources.userAPI.getUser(),
        context.dataSources.orderAPI.getRecentOrders(),
        context.dataSources.notificationAPI.getNotifications(),
        context.dataSources.analyticsAPI.getAnalytics()
      ]);

      return optimizeForDevice(data, device);
    }
  }
};
```

### 2. REST Experience API

```javascript
class RestExperienceAPI {
  async getDashboardData(req, res) {
    const device = req.headers['user-agent'];
    const userType = req.user.type;

    // Parallel data fetching
    const [userData, analyticsData] = await Promise.all([
      this.userService.getData(req.user.id),
      this.analyticsService.getData(req.user.id)
    ]);

    // Custom response transformation
    const response = this.transformResponse({
      userData,
      analyticsData
    }, device, userType);

    return res.json(response);
  }
}
```

## Best Practices

### 1. Response Optimization
- Remove unnecessary fields
- Compress data structures
- Implement field selection
- Cache common responses

### 2. Error Handling
- Standardize error formats
- Implement retry mechanisms
- Provide meaningful error messages
- Include correlation IDs

### 3. Performance
- Implement caching strategies
- Use parallel requests
- Optimize payload size
- Monitor performance metrics

### 4. Security
- Implement rate limiting
- Validate input data
- Handle authentication/authorization
- Secure sensitive data

### 5. Monitoring
- Log API usage
- Track performance metrics
- Monitor error rates
- Implement health checks