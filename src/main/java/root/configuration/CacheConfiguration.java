package root.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import root.configuration.properties.CacheProperties;

import java.util.concurrent.TimeUnit;

import static root.configuration.properties.CacheProperties.ADDITIONAL_DATA_CACHE_NAME;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager(CacheProperties cacheProperties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        registerCache(ADDITIONAL_DATA_CACHE_NAME, cacheProperties, cacheManager);
        return cacheManager;
    }

    private void registerCache(String cacheName,
                               CacheProperties cacheProperties,
                               CaffeineCacheManager cacheManager) {
        var cacheDurationInSeconds = cacheProperties.getDurationInSeconds(cacheName);
        var cache = Caffeine.newBuilder()
                .expireAfterWrite(cacheDurationInSeconds, TimeUnit.SECONDS)
                .recordStats()
                .build();
        cacheManager.registerCustomCache(cacheName, cache);
    }
}
