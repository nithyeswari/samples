@Configuration
@EnableCaching
public class CaffeineConfig extends CachingConfigurerSupport {
    
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
    
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
            .maximumSize(maximumSize)
            .expireAfterWrite(Duration.ofMinutes(defaultExpirationInMinutes))
            .recordStats();
    }
    
    // Custom cache with Caffeine for async loading
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
            throw new RuntimeException("Failed to load data for key: " + key, e);
        }
    }
}

@Service
@Slf4j
public class CaffeineCacheService {
    private final AsyncLoadingCache<String, JsonNode> asyncCache;
    private final CacheMetricsCollector metricsCollector;
    
    public CaffeineCacheService(
            AsyncLoadingCache<String, JsonNode> asyncCache,
            CacheMetricsCollector metricsCollector) {
        this.asyncCache = asyncCache;
        this.metricsCollector = metricsCollector;
    }
    
    // Async get with Caffeine
    public CompletableFuture<JsonNode> getData(String key) {
        return asyncCache.get(key)
            .thenApply(data -> {
                metricsCollector.recordCacheHit(key);
                return data;
            })
            .exceptionally(throwable -> {
                metricsCollector.recordCacheError(key);
                log.error("Error loading data for key {}: {}", key, throwable.getMessage());
                return null;
            });
    }
    
    // Bulk loading with Caffeine
    public CompletableFuture<Map<String, JsonNode>> getBulkData(Set<String> keys) {
        return asyncCache.getAll(keys)
            .thenApply(dataMap -> {
                dataMap.forEach((key, value) -> metricsCollector.recordCacheHit(key));
                return dataMap;
            })
            .exceptionally(throwable -> {
                keys.forEach(key -> metricsCollector.recordCacheError(key));
                log.error("Error loading bulk data: {}", throwable.getMessage());
                return Collections.emptyMap();
            });
    }
    
    // Manual cache manipulation
    public void invalidateKey(String key) {
        asyncCache.synchronous().invalidate(key);
    }
    
    public void refreshKey(String key) {
        asyncCache.synchronous().refresh(key);
    }
    
    // Get cache statistics
    public CacheStats getCacheStats() {
        return asyncCache.synchronous().stats();
    }
}

@Component
@Slf4j
public class CacheMetricsCollector {
    private final Counter cacheHits;
    private final Counter cacheMisses;
    private final Counter cacheErrors;
    
    public CacheMetricsCollector(MeterRegistry registry) {
        this.cacheHits = registry.counter("cache.hits");
        this.cacheMisses = registry.counter("cache.misses");
        this.cacheErrors = registry.counter("cache.errors");
    }
    
    public void recordCacheHit(String key) {
        cacheHits.increment();
        log.debug("Cache hit for key: {}", key);
    }
    
    public void recordCacheMiss(String key) {
        cacheMisses.increment();
        log.debug("Cache miss for key: {}", key);
    }
    
    public void recordCacheError(String key) {
        cacheErrors.increment();
        log.warn("Cache error for key: {}", key);
    }
}

@RestController
@RequestMapping("/api/cache")
public class CaffeineCacheController {
    private final CaffeineCacheService cacheService;
    
    @Autowired
    public CaffeineCacheController(CaffeineCacheService cacheService) {
        this.cacheService = cacheService;
    }
    
    @GetMapping("/{key}")
    public CompletableFuture<ResponseEntity<JsonNode>> getData(@PathVariable String key) {
        return cacheService.getData(key)
            .thenApply(data -> data != null 
                ? ResponseEntity.ok(data)
                : ResponseEntity.notFound().build()
            );
    }
    
    @PostMapping("/bulk")
    public CompletableFuture<ResponseEntity<Map<String, JsonNode>>> getBulkData(
            @RequestBody Set<String> keys) {
        return cacheService.getBulkData(keys)
            .thenApply(ResponseEntity::ok);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<CacheStats> getCacheStats() {
        return ResponseEntity.ok(cacheService.getCacheStats());
    }
    
    @PostMapping("/{key}/refresh")
    public ResponseEntity<Void> refreshKey(@PathVariable String key) {
        cacheService.refreshKey(key);
        return ResponseEntity.ok().build();
    }
}