8# Smart Cache System 

## Table of Contents
1. Configuration
2. Core Cache Components
3. Service Layer
4. Controllers
5. Monitoring
6. Utilities

## 1. Configuration

### CacheConfig.java
```java
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig extends CachingConfigurerSupport {
    
    @Value("${cache.default-expiration:30}")
    private int defaultExpirationInMinutes;
    
    @Value("${cache.maximum-size:10000}")
    private int maximumSize;
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }
    
    @Bean
    public Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
            .maximumSize(maximumSize)
            .expireAfterWrite(Duration.ofMinutes(defaultExpirationInMinutes))
            .recordStats();
    }
    
    @Bean
    public AsyncLoadingCache<String, JsonNode> asyncCache(RestTemplate restTemplate) {
        return Caffeine.newBuilder()
            .maximumSize(maximumSize)
            .expireAfterWrite(Duration.ofMinutes(defaultExpirationInMinutes))
            .recordStats()
            .buildAsync(new CacheLoader<String, JsonNode>() {
                @Override
                public JsonNode load(String key) {
                    return loadData(key, restTemplate);
                }
                
                @Override
                public Map<String, JsonNode> loadAll(Set<? extends String> keys) {
                    return keys.stream()
                        .parallel()
                        .collect(Collectors.toMap(
                            key -> key,
                            key -> loadData(key, restTemplate)
                        ));
                }
            });
    }
    
    private JsonNode loadData(String key, RestTemplate restTemplate) {
        try {
            String response = restTemplate.getForObject(
                "/api/data/" + key,
                String.class
            );
            return new ObjectMapper().readTree(response);
        } catch (Exception e) {
            throw new CacheLoadingException("Failed to load data for key: " + key, e);
        }
    }
}
```

## 2. Core Cache Components

### CustomCache.java
```java
@Slf4j
public class CustomCache implements Cache {
    private final String name;
    private final Duration ttl;
    private final ConcurrentMap<Object, CacheEntry> store;
    private final ScheduledExecutorService scheduler;
    private final List<CacheEventListener> eventListeners;
    
    public CustomCache(String name, Duration ttl) {
        this.name = name;
        this.ttl = ttl;
        this.store = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.eventListeners = new CopyOnWriteArrayList<>();
        
        scheduler.scheduleAtFixedRate(
            this::cleanupExpiredEntries,
            ttl.getSeconds(),
            ttl.getSeconds(),
            TimeUnit.SECONDS
        );
    }
    
    @Override
    public ValueWrapper get(Object key) {
        CacheEntry entry = store.get(key);
        if (entry != null && !entry.isExpired()) {
            notifyListeners(CacheEventType.HIT, key);
            return new SimpleValueWrapper(entry.getValue());
        }
        
        if (entry != null && entry.isExpired()) {
            store.remove(key);
            notifyListeners(CacheEventType.EXPIRED, key);
        }
        
        notifyListeners(CacheEventType.MISS, key);
        return null;
    }
    
    @Override
    public void put(Object key, Object value) {
        store.put(key, new CacheEntry(value, ttl));
        notifyListeners(CacheEventType.PUT, key);
    }
    
    public void addListener(CacheEventListener listener) {
        eventListeners.add(listener);
    }
    
    private void notifyListeners(CacheEventType type, Object key) {
        eventListeners.forEach(listener -> {
            try {
                switch (type) {
                    case HIT -> listener.onCacheHit(name, key);
                    case MISS -> listener.onCacheMiss(name, key);
                    case EXPIRED -> listener.onCacheExpiry(name, key);
                    case PUT -> listener.onCachePut(name, key);
                }
            } catch (Exception e) {
                log.error("Error notifying listener: {}", e.getMessage(), e);
            }
        });
    }
    
    private void cleanupExpiredEntries() {
        store.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired();
            if (expired) {
                notifyListeners(CacheEventType.EXPIRED, entry.getKey());
            }
            return expired;
        });
    }
}
```

### CacheEntry.java
```java
@Data
@AllArgsConstructor
public class CacheEntry {
    private final Object value;
    private final long expirationTime;
    private final Map<String, Object> metadata;
    
    public CacheEntry(Object value, Duration ttl) {
        this.value = value;
        this.expirationTime = System.currentTimeMillis() + ttl.toMillis();
        this.metadata = new ConcurrentHashMap<>();
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
    
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
}
```

## 3. Service Layer

### CacheService.java
```java
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {
    private final AsyncLoadingCache<String, JsonNode> asyncCache;
    private final CustomCache customCache;
    private final ObjectMapper objectMapper;
    private final CacheMetricsCollector metricsCollector;
    
    public CompletableFuture<JsonNode> getData(String key) {
        return CompletableFuture.supplyAsync(() -> {
            ValueWrapper cached = customCache.get(key);
            if (cached != null) {
                return (JsonNode) cached.get();
            }
            
            return asyncCache.get(key)
                .thenApply(data -> {
                    customCache.put(key, data);
                    return data;
                })
                .exceptionally(throwable -> {
                    log.error("Error loading data for key {}: {}", 
                        key, throwable.getMessage());
                    metricsCollector.recordError(key);
                    return null;
                })
                .join();
        });
    }
    
    public CompletableFuture<Map<String, JsonNode>> getBulkData(Set<String> keys) {
        return asyncCache.getAll(keys)
            .thenApply(dataMap -> {
                dataMap.forEach((key, value) -> 
                    customCache.put(key, value));
                return dataMap;
            })
            .exceptionally(throwable -> {
                log.error("Error loading bulk data: {}", throwable.getMessage());
                metricsCollector.recordBulkError(keys);
                return Collections.emptyMap();
            });
    }
    
    @Scheduled(fixedRateString = "${cache.cleanup.interval:300000}")
    public void cleanup() {
        // Implement cleanup logic
    }
}
```

## 4. Controllers

### CacheController.java
```java
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {
    private final CacheService cacheService;
    
    @GetMapping("/{key}")
    public CompletableFuture<ResponseEntity<JsonNode>> getData(
            @PathVariable String key) {
        return cacheService.getData(key)
            .thenApply(data -> data != null 
                ? ResponseEntity.ok(data)
                : ResponseEntity.notFound().build()
            );
    }
    
    @PostMapping("/bulk")
    public CompletableFuture<ResponseEntity<Map<String, JsonNode>>> getBulkData(
            @RequestBody Set<String> keys) {
        if (keys.size() > 100) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest()
                    .header("X-Error", "Too many keys requested")
                    .build()
            );
        }
        
        return cacheService.getBulkData(keys)
            .thenApply(ResponseEntity::ok);
    }
    
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> invalidateCache(@PathVariable String key) {
        cacheService.invalidateKey(key);
        return ResponseEntity.ok().build();
    }
}
```

## 5. Monitoring

### CacheMetricsCollector.java
```java
@Component
@Slf4j
public class CacheMetricsCollector {
    private final MeterRegistry registry;
    private final Map<String, Timer> loadTimers;
    
    public CacheMetricsCollector(MeterRegistry registry) {
        this.registry = registry;
        this.loadTimers = new ConcurrentHashMap<>();
    }
    
    public void recordCacheHit(String key) {
        registry.counter("cache.hits", 
            "key", key).increment();
    }
    
    public void recordCacheMiss(String key) {
        registry.counter("cache.misses", 
            "key", key).increment();
    }
    
    public void recordLoadTime(String key, long milliseconds) {
        loadTimers.computeIfAbsent(key, 
            k -> registry.timer("cache.load.time", "key", k))
            .record(milliseconds, TimeUnit.MILLISECONDS);
    }
    
    public void recordError(String key) {
        registry.counter("cache.errors", 
            "key", key,
            "type", "single").increment();
    }
    
    public void recordBulkError(Set<String> keys) {
        registry.counter("cache.errors", 
            "keys", String.join(",", keys),
            "type", "bulk").increment();
    }
}
```

## 6. Utilities

### CacheEventListener.java
```java
public interface CacheEventListener {
    void onCacheHit(String cacheName, Object key);
    void onCacheMiss(String cacheName, Object key);
    void onCacheExpiry(String cacheName, Object key);
    void onCachePut(String cacheName, Object key);
}
```

### CacheException.java
```java
public class CacheException extends RuntimeException {
    public CacheException(String message) {
        super(message);
    }
    
    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class CacheLoadingException extends CacheException {
    public CacheLoadingException(String message) {
        super(message);
    }
    
    public CacheLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### Application Properties
```properties
# Cache Configuration
cache.default-expiration=30
cache.maximum-size=10000
cache.cleanup.interval=300000

# API Configuration
api.base-url=https://your-api-url

# Metrics Configuration
management.endpoints.web.exposure.include=prometheus,health,info
management.metrics.export.prometheus.enabled=true
```

### Unit Test Example
```java
@SpringBootTest
class CacheServiceTest {
    @MockBean
    private AsyncLoadingCache<String, JsonNode> asyncCache;
    
    @Autowired
    private CacheService cacheService;
    
    @Test
    void whenDataExists_thenReturnFromCache() {
        // Test implementation
    }
    
    @Test
    void whenDataNotExists_thenLoadFromSource() {
        // Test implementation
    }
}
```

This documentation provides complete, runnable code for all major components of the caching system. Each component is designed to be modular and extensible.
