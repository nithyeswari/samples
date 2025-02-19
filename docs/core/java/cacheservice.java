// 1. Configuration Properties Class with Environment Support
package com.example.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {
    
    private String provider = "caffeineCacheStrategy"; // Default to pod-level caching
    private String environment = "pod"; // Options: pod, distributed
    private final Ttl ttl = new Ttl();
    private final Refresh refresh = new Refresh();
    private final Redis redis = new Redis();
    private final Hazelcast hazelcast = new Hazelcast();
    
    public static class Ttl {
        private long seconds = 600;
        
        public long getSeconds() {
            return seconds;
        }
        
        public void setSeconds(long seconds) {
            this.seconds = seconds;
        }
    }
    
    public static class Refresh {
        // Default: midnight every day
        private String cron = "0 0 0 * * ?";
        private boolean enabled = true;
        private boolean autoRefreshOnEviction = true;
        
        public String getCron() {
            return cron;
        }
        
        public void setCron(String cron) {
            this.cron = cron;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public boolean isAutoRefreshOnEviction() {
            return autoRefreshOnEviction;
        }
        
        public void setAutoRefreshOnEviction(boolean autoRefreshOnEviction) {
            this.autoRefreshOnEviction = autoRefreshOnEviction;
        }
    }
    
    public static class Redis {
        private boolean embedded = true; // For pod-level, use embedded Redis
        private String host = "localhost";
        private int port = 6379;
        private String password = "";
        private int database = 0;
        private Cluster cluster = new Cluster();
        
        public static class Cluster {
            private boolean enabled = false;
            private String[] nodes = {};
            private int maxRedirects = 3;
            
            public boolean isEnabled() {
                return enabled;
            }
            
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
            
            public String[] getNodes() {
                return nodes;
            }
            
            public void setNodes(String[] nodes) {
                this.nodes = nodes;
            }
            
            public int getMaxRedirects() {
                return maxRedirects;
            }
            
            public void setMaxRedirects(int maxRedirects) {
                this.maxRedirects = maxRedirects;
            }
        }
        
        public boolean isEmbedded() {
            return embedded;
        }
        
        public void setEmbedded(boolean embedded) {
            this.embedded = embedded;
        }
        
        public String getHost() {
            return host;
        }
        
        public void setHost(String host) {
            this.host = host;
        }
        
        public int getPort() {
            return port;
        }
        
        public void setPort(int port) {
            this.port = port;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public int getDatabase() {
            return database;
        }
        
        public void setDatabase(int database) {
            this.database = database;
        }
        
        public Cluster getCluster() {
            return cluster;
        }
    }
    
    public static class Hazelcast {
        private boolean embedded = true; // For pod-level, use embedded Hazelcast
        private String[] serverAddresses = {};
        private String instanceName = "hazelcast-cache";
        private boolean kubernetesEnabled = false;
        private String kubernetesNamespace = "default";
        private String kubernetesServiceName = "hazelcast-service";
        
        public boolean isEmbedded() {
            return embedded;
        }
        
        public void setEmbedded(boolean embedded) {
            this.embedded = embedded;
        }
        
        public String[] getServerAddresses() {
            return serverAddresses;
        }
        
        public void setServerAddresses(String[] serverAddresses) {
            this.serverAddresses = serverAddresses;
        }
        
        public String getInstanceName() {
            return instanceName;
        }
        
        public void setInstanceName(String instanceName) {
            this.instanceName = instanceName;
        }
        
        public boolean isKubernetesEnabled() {
            return kubernetesEnabled;
        }
        
        public void setKubernetesEnabled(boolean kubernetesEnabled) {
            this.kubernetesEnabled = kubernetesEnabled;
        }
        
        public String getKubernetesNamespace() {
            return kubernetesNamespace;
        }
        
        public void setKubernetesNamespace(String kubernetesNamespace) {
            this.kubernetesNamespace = kubernetesNamespace;
        }
        
        public String getKubernetesServiceName() {
            return kubernetesServiceName;
        }
        
        public void setKubernetesServiceName(String kubernetesServiceName) {
            this.kubernetesServiceName = kubernetesServiceName;
        }
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public Ttl getTtl() {
        return ttl;
    }
    
    public Refresh getRefresh() {
        return refresh;
    }
    
    public Redis getRedis() {
        return redis;
    }
    
    public Hazelcast getHazelcast() {
        return hazelcast;
    }
}

// 2. Redis Configuration Factory
package com.example.cache.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;

@Configuration
public class RedisConfigFactory {
    
    @Autowired
    private CacheProperties cacheProperties;

    @Bean
    @Profile("!distributed-redis")
    public RedisConnectionFactory podLevelRedisConnectionFactory() {
        // For pod-level caching, connect to localhost Redis (sidecar if in K8s)
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(cacheProperties.getRedis().getHost());
        config.setPort(cacheProperties.getRedis().getPort());
        
        String password = cacheProperties.getRedis().getPassword();
        if (password != null && !password.isEmpty()) {
            config.setPassword(password);
        }
        
        config.setDatabase(cacheProperties.getRedis().getDatabase());
        
        return new LettuceConnectionFactory(config);
    }
    
    @Bean
    @Profile("distributed-redis")
    public RedisConnectionFactory distributedRedisConnectionFactory() {
        // For distributed caching, connect to a Redis cluster
        if (cacheProperties.getRedis().getCluster().isEnabled()) {
            RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(
                    Arrays.asList(cacheProperties.getRedis().getCluster().getNodes()));
            clusterConfig.setMaxRedirects(cacheProperties.getRedis().getCluster().getMaxRedirects());
            
            String password = cacheProperties.getRedis().getPassword();
            if (password != null && !password.isEmpty()) {
                clusterConfig.setPassword(password);
            }
            
            return new LettuceConnectionFactory(clusterConfig);
        } else {
            // Fallback to standalone configuration for distributed but non-clustered Redis
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(cacheProperties.getRedis().getHost());
            config.setPort(cacheProperties.getRedis().getPort());
            
            String password = cacheProperties.getRedis().getPassword();
            if (password != null && !password.isEmpty()) {
                config.setPassword(password);
            }
            
            config.setDatabase(cacheProperties.getRedis().getDatabase());
            
            return new LettuceConnectionFactory(config);
        }
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Set up serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}

// 3. Hazelcast Configuration Factory
package com.example.cache.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class HazelcastConfigFactory {
    
    @Autowired
    private CacheProperties cacheProperties;

    @Bean
    @Profile("!distributed-hazelcast")
    public HazelcastInstance podLevelHazelcastInstance() {
        // For pod-level caching, create embedded Hazelcast instance
        Config config = new Config();
        config.setInstanceName(cacheProperties.getHazelcast().getInstanceName());
        
        // Disable clustering features for pod-level
        NetworkConfig networkConfig = config.getNetworkConfig();
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(false);
        joinConfig.getKubernetesConfig().setEnabled(false);
        
        // Configure simple map with TTL
        MapConfig mapConfig = new MapConfig();
        mapConfig.setName("*")
                .setTimeToLiveSeconds((int) cacheProperties.getTtl().getSeconds())
                .setEvictionConfig(
                    new EvictionConfig()
                        .setSize(10000)
                        .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
                        .setEvictionPolicy(EvictionPolicy.LRU)
                );
        
        config.addMapConfig(mapConfig);
        
        return Hazelcast.newHazelcastInstance(config);
    }
    
    @Bean
    @Profile("distributed-hazelcast")
    public HazelcastInstance distributedHazelcastInstance() {
        if (cacheProperties.getHazelcast().isEmbedded()) {
            // Distributed but still embedded (clustering multiple app instances)
            Config config = new Config();
            config.setInstanceName(cacheProperties.getHazelcast().getInstanceName());
            
            // Configure network for cluster discovery
            NetworkConfig networkConfig = config.getNetworkConfig();
            JoinConfig joinConfig = networkConfig.getJoin();
            
            // Disable multicast
            joinConfig.getMulticastConfig().setEnabled(false);
            
            if (cacheProperties.getHazelcast().isKubernetesEnabled()) {
                // Enable Kubernetes discovery
                joinConfig.getKubernetesConfig()
                        .setEnabled(true)
                        .setProperty("namespace", cacheProperties.getHazelcast().getKubernetesNamespace())
                        .setProperty("service-name", cacheProperties.getHazelcast().getKubernetesServiceName());
            } else {
                // Use TCP-IP discovery with explicit addresses
                TcpIpConfig tcpIpConfig = joinConfig.getTcpIpConfig();
                tcpIpConfig.setEnabled(true);
                for (String address : cacheProperties.getHazelcast().getServerAddresses()) {
                    tcpIpConfig.addMember(address);
                }
            }
            
            // Configure map with TTL
            MapConfig mapConfig = new MapConfig();
            mapConfig.setName("*")
                    .setTimeToLiveSeconds((int) cacheProperties.getTtl().getSeconds())
                    .setBackupCount(1) // Enable backups for distributed mode
                    .setEvictionConfig(
                        new EvictionConfig()
                            .setSize(10000)
                            .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
                            .setEvictionPolicy(EvictionPolicy.LRU)
                    );
            
            config.addMapConfig(mapConfig);
            
            return Hazelcast.newHazelcastInstance(config);
        } else {
            // Client mode - connecting to external Hazelcast cluster
            ClientConfig clientConfig = new ClientConfig();
            
            if (cacheProperties.getHazelcast().isKubernetesEnabled()) {
                // Use Kubernetes discovery
                clientConfig.getNetworkConfig().getKubernetesConfig()
                        .setEnabled(true)
                        .setProperty("namespace", cacheProperties.getHazelcast().getKubernetesNamespace())
                        .setProperty("service-name", cacheProperties.getHazelcast().getKubernetesServiceName());
            } else {
                // Use explicit server addresses
                clientConfig.getNetworkConfig().addAddress(cacheProperties.getHazelcast().getServerAddresses());
            }
            
            return HazelcastClient.newHazelcastClient(clientConfig);
        }
    }
}

// 4. Environment-Based Configuration Resolver
package com.example.cache.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CacheEnvironmentConfigurer {

    @Autowired
    private CacheProperties cacheProperties;
    
    @Autowired
    private ConfigurableEnvironment environment;
    
    @PostConstruct
    public void configureEnvironment() {
        String cacheEnvironment = cacheProperties.getEnvironment();
        String cacheProvider = cacheProperties.getProvider();
        
        List<String> activeProfiles = new ArrayList<>(Arrays.asList(environment.getActiveProfiles()));
        
        // Remove any existing cache profile
        activeProfiles.removeIf(profile -> 
            profile.startsWith("distributed-") || "pod-level".equals(profile));
        
        if ("distributed".equals(cacheEnvironment)) {
            // Add provider-specific distributed profile
            if ("redisCacheStrategy".equals(cacheProvider)) {
                activeProfiles.add("distributed-redis");
            } else if ("hazelcastCacheStrategy".equals(cacheProvider)) {
                activeProfiles.add("distributed-hazelcast");
            }
        } else {
            // Default to pod-level
            activeProfiles.add("pod-level");
        }
        
        environment.setActiveProfiles(activeProfiles.toArray(new String[0]));
    }
}

// 5. Pod-Level Application Properties (application-pod-level.yml)
/*
cache:
  environment: pod
  provider: caffeineCacheStrategy  # In-memory cache for pod-level
  ttl:
    seconds: 600
  refresh:
    enabled: true
    cron: "0 0 0 * * ?"  # Midnight every day
    autoRefreshOnEviction: true
  redis:
    embedded: true
    host: localhost
    port: 6379
  hazelcast:
    embedded: true
*/

// 6. Distributed Redis Application Properties (application-distributed-redis.yml)
/*
cache:
  environment: distributed
  provider: redisCacheStrategy
  ttl:
    seconds: 1800  # Longer TTL for distributed cache
  refresh:
    enabled: true
    cron: "0 0 0 * * ?"
    autoRefreshOnEviction: true
  redis:
    embedded: false
    host: redis-cluster  # K8s service name
    port: 6379
    cluster:
      enabled: true
      nodes:
        - redis-cluster-0.redis-cluster:6379
        - redis-cluster-1.redis-cluster:6379
        - redis-cluster-2.redis-cluster:6379
      maxRedirects: 3
*/

// 7. Distributed Hazelcast Application Properties (application-distributed-hazelcast.yml)
/*
cache:
  environment: distributed
  provider: hazelcastCacheStrategy
  ttl:
    seconds: 1800
  refresh:
    enabled: true
    cron: "0 0 0 * * ?"
    autoRefreshOnEviction: true
  hazelcast:
    embedded: false  # Client mode
    kubernetesEnabled: true
    kubernetesNamespace: default
    kubernetesServiceName: hazelcast-service
*/

// 8. Cache Provider Selection Service
package com.example.cache.service;

import com.example.cache.config.CacheProperties;
import com.example.cache.strategy.CacheStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CacheProviderSelector {
    
    private final Map<String, CacheStrategy> strategies = new HashMap<>();
    private final CacheProperties cacheProperties;
    
    @Autowired
    public CacheProviderSelector(
            CacheProperties cacheProperties,
            @Qualifier("redisCacheStrategy") CacheStrategy redisStrategy,
            @Qualifier("hazelcastCacheStrategy") CacheStrategy hazelcastStrategy,
            @Qualifier("caffeineCacheStrategy") CacheStrategy caffeineStrategy) {
        
        this.cacheProperties = cacheProperties;
        
        // Register all available strategies
        strategies.put("redisCacheStrategy", redisStrategy);
        strategies.put("hazelcastCacheStrategy", hazelcastStrategy);
        strategies.put("caffeineCacheStrategy", caffeineStrategy);
    }
    
    public CacheStrategy getCurrentStrategy() {
        String providerName = cacheProperties.getProvider();
        CacheStrategy strategy = strategies.get(providerName);
        
        if (strategy == null) {
            // Default to Caffeine for pod-level or Redis for distributed if provider not found
            if ("distributed".equals(cacheProperties.getEnvironment())) {
                return strategies.get("redisCacheStrategy");
            } else {
                return strategies.get("caffeineCacheStrategy");
            }
        }
        
        return strategy;
    }
}

// 9. Update CacheService to use the selector
package com.example.cache.service;

import com.example.cache.config.CacheProperties;
import com.example.cache.refresh.CacheEntryRefreshHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
public class CacheService {
    
    private final CacheProviderSelector cacheProviderSelector;
    private final CacheProperties cacheProperties;
    private final List<CacheEntryRefreshHandler> refreshHandlers;
    
    @Autowired
    public CacheService(
            CacheProviderSelector cacheProviderSelector,
            CacheProperties cacheProperties,
            List<CacheEntryRefreshHandler> refreshHandlers) {
        
        this.cacheProviderSelector = cacheProviderSelector;
        this.cacheProperties = cacheProperties;
        this.refreshHandlers = refreshHandlers;
    }
    
    @PostConstruct
    public void initialize() {
        // Schedule cache refresh based on cron expression from config
        cacheProviderSelector.getCurrentStrategy()
            .scheduleCacheRefresh(cacheProperties.getRefresh().getCron());
        
        // Register all refresh handlers with current strategy
        refreshHandlers.forEach(
            cacheProviderSelector.getCurrentStrategy()::registerRefreshHandler
        );
    }
    
    // Rest of methods delegate to current strategy
    public void set(String cacheName, String key, Object value) {
        cacheProviderSelector.getCurrentStrategy().set(
            cacheName, 
            key, 
            value, 
            cacheProperties.getTtl().getSeconds(), 
            TimeUnit.SECONDS
        );
    }
    
    public void setWithAutoRefresh(String cacheName, String key, Object value, 
                                 long ttl, TimeUnit timeUnit, boolean autoRefresh) {
        cacheProviderSelector.getCurrentStrategy()
            .setWithAutoRefresh(cacheName, key, value, ttl, timeUnit, autoRefresh);
    }
    
    // Other delegating methods...
}