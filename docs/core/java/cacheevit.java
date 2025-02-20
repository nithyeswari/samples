// CacheStrategy.java
public enum CacheStrategy {
    LRU,
    FIFO
}

// CacheEntry.java
public class CacheEntry<T> {
    private T value;
    private long expiresAt;
    private volatile boolean isRefreshing;
    private volatile boolean isBeingEvicted;
    private volatile EventType currentEvent;

    public enum EventType {
        NONE,
        REFRESH,
        EVICTION
    }

    public CacheEntry(T value, long expiresAt) {
        this.value = value;
        this.expiresAt = expiresAt;
        this.isRefreshing = false;
        this.isBeingEvicted = false;
        this.currentEvent = EventType.NONE;
    }

    // Getters and setters
    public T getValue() { return value; }
    public void setValue(T value) { this.value = value; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public boolean isRefreshing() { return isRefreshing; }
    public void setRefreshing(boolean refreshing) { isRefreshing = refreshing; }
}

// CacheEvictionListener.java
public interface CacheEvictionListener<T> {
    void onEvict(String key, T value);
}

// DataReloader.java
public interface DataReloader<T> {
    CompletableFuture<T> reload(String key);
}

// CacheConfiguration.java
@Configuration
public class CacheConfiguration {
    @Bean
    public <T> CacheService<T> cacheService(
            @Value("${cache.strategy:LRU}") CacheStrategy strategy,
            @Value("${cache.defaultTTL:60000}") long defaultTTL,
            @Value("${cache.maxSize:1000}") int maxSize) {
        return new CacheService<>(strategy, defaultTTL, maxSize);
    }
}

// CacheService.java
@Service
public class CacheService<T> {
    private final Map<String, CacheEntry<T>> cache;
    private final LinkedList<String> evictionQueue;
    private final CacheStrategy strategy;
    private final long defaultTTL;
    private final int maxSize;
    
    @Autowired(required = false)
    private CacheEvictionListener<T> evictionListener;
    
    @Autowired(required = false)
    private DataReloader<T> dataReloader;
    
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public CacheService(CacheStrategy strategy, long defaultTTL, int maxSize) {
        this.cache = new ConcurrentHashMap<>();
        this.evictionQueue = new LinkedList<>();
        this.strategy = strategy;
        this.defaultTTL = defaultTTL;
        this.maxSize = maxSize;
    }

    public CompletableFuture<Optional<T>> get(String key) {
        CacheEntry<T> entry = cache.get(key);
        
        if (entry == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        if (hasExpired(entry)) {
            return refreshKey(key)
                    .thenApply(v -> Optional.ofNullable(cache.get(key))
                            .map(CacheEntry::getValue));
        }

        updateEvictionQueue(key);
        return CompletableFuture.completedFuture(Optional.of(entry.getValue()));
    }

    public void set(String key, T value, long ttl) {
        long expiresAt = System.currentTimeMillis() + (ttl > 0 ? ttl : defaultTTL);
        
        lock.writeLock().lock();
        try {
            if (cache.size() >= maxSize) {
                evict();
            }
            
            cache.put(key, new CacheEntry<>(value, expiresAt));
            updateEvictionQueue(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void set(String key, T value) {
        set(key, value, defaultTTL);
    }

    @Async
    public CompletableFuture<Void> refreshKey(String key) {
        CacheEntry<T> entry = cache.get(key);
        if (entry == null || entry.getCurrentEvent() != CacheEntry.EventType.NONE || dataReloader == null) {
            return CompletableFuture.completedFuture(null);
        }

        synchronized (entry) {
            if (entry.getCurrentEvent() != CacheEntry.EventType.NONE) {
                return CompletableFuture.completedFuture(null);
            }
            entry.setCurrentEvent(CacheEntry.EventType.REFRESH);
            entry.setRefreshing(true);
        }

        return dataReloader.reload(key)
                .whenComplete((newValue, error) -> {
                    try {
                        if (error != null) {
                            delete(key);
                        } else if (newValue != null) {
                            set(key, newValue);
                        }
                    } finally {
                        synchronized (entry) {
                            entry.setRefreshing(false);
                            entry.setCurrentEvent(CacheEntry.EventType.NONE);
                        }
                    }
    }

    @Scheduled(fixedRateString = "${cache.refresh.interval:60000}")
    @Async
    public CompletableFuture<Void> refresh() {
        List<CompletableFuture<Void>> refreshTasks = new ArrayList<>();
        
        lock.readLock().lock();
        try {
            for (Map.Entry<String, CacheEntry<T>> entry : cache.entrySet()) {
                if (hasExpired(entry.getValue())) {
                    refreshTasks.add(refreshKey(entry.getKey()));
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        return CompletableFuture.allOf(refreshTasks.toArray(new CompletableFuture[0]));
    }

    @Async
    public CompletableFuture<Void> delete(String key) {
        lock.writeLock().lock();
        try {
            CacheEntry<T> entry = cache.get(key);
            if (entry == null) {
                return CompletableFuture.completedFuture(null);
            }

            synchronized (entry) {
                if (entry.getCurrentEvent() != CacheEntry.EventType.NONE) {
                    return CompletableFuture.completedFuture(null);
                }
                entry.setCurrentEvent(CacheEntry.EventType.EVICTION);
                entry.setBeingEvicted(true);
            }

            try {
                if (evictionListener != null) {
                    evictionListener.onEvict(key, entry.getValue());
                }
                cache.remove(key);
                evictionQueue.remove(key);
                
                // Trigger async reload after eviction
                return reloadAfterEviction(key);
            } finally {
                synchronized (entry) {
                    entry.setBeingEvicted(false);
                    entry.setCurrentEvent(CacheEntry.EventType.NONE);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private CompletableFuture<Void> reloadAfterEviction(String key) {
        if (dataReloader == null) {
            return CompletableFuture.completedFuture(null);
        }

        return dataReloader.reload(key)
            .thenAccept(newValue -> {
                if (newValue != null) {
                    set(key, newValue);
                }
            })
            .exceptionally(error -> {
                logger.error("Failed to reload data after eviction for key: " + key, error);
                return null;
            });
    }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            if (evictionListener != null) {
                cache.forEach((key, entry) -> evictionListener.onEvict(key, entry.getValue()));
            }
            cache.clear();
            evictionQueue.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean hasExpired(CacheEntry<T> entry) {
        return System.currentTimeMillis() > entry.getExpiresAt();
    }

    private void updateEvictionQueue(String key) {
        lock.writeLock().lock();
        try {
            switch (strategy) {
                case LRU:
                    evictionQueue.remove(key);
                    evictionQueue.addLast(key);
                    break;
                case FIFO:
                    if (!evictionQueue.contains(key)) {
                        evictionQueue.addLast(key);
                    }
                    break;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

        @Async
    private CompletableFuture<Void> evict() {
        lock.writeLock().lock();
        try {
            if (!evictionQueue.isEmpty()) {
                String keyToEvict = evictionQueue.removeFirst();
                return delete(keyToEvict);
            }
            return CompletableFuture.completedFuture(null);
        } finally {
            lock.writeLock().unlock();
        }
    }
}

// Example Controller
@RestController
@RequestMapping("/api/cache")
public class CacheController<T> {
    @Autowired
    private CacheService<T> cacheService;

    @PostMapping("/refresh/{key}")
    public CompletableFuture<ResponseEntity<Void>> refreshKey(@PathVariable String key) {
        return cacheService.refreshKey(key)
                .thenApply(v -> ResponseEntity.ok().build());
    }

    @PostMapping("/refresh")
    public CompletableFuture<ResponseEntity<Void>> refreshAll() {
        return cacheService.refresh()
                .thenApply(v -> ResponseEntity.ok().build());
    }
}

// Example application.properties
cache.strategy=LRU
cache.defaultTTL=60000
cache.maxSize=1000
cache.refresh.interval=60000

// Example implementation of DataReloader
@Component
public class ExampleDataReloader implements DataReloader<String> {
    @Override
    public CompletableFuture<String> reload(String key) {
        return CompletableFuture.supplyAsync(() -> {
            // Simulate async data reload
            return "Reloaded data for " + key;
        });
    }
}

// Example implementation of CacheEvictionListener
@Component
public class ExampleCacheEvictionListener implements CacheEvictionListener<String> {
    private static final Logger logger = LoggerFactory.getLogger(ExampleCacheEvictionListener.class);

    @Override
    public void onEvict(String key, String value) {
        logger.info("Evicting cache entry - key: {}, value: {}", key, value);
    }
}