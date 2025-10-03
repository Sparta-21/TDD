package com.sparta.tdd.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 로컬 개발 환경 -> Caffeine Cache 사용
// CacheManager 추상화로 인해 Redis 확장에도 열려있음
// 캐시 타입이 많아지면 추후 enum으로 CacheType 관리 고려
@Configuration
@EnableCaching
public class CacheConfig {

    private static final long MAXIMUM_CACHE_SIZE = 5000L;

    @Bean
    public CacheManager cacheManager(Cache accessTokenBlacklistCache, Cache refreshTokenBlacklistCache) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(accessTokenBlacklistCache, refreshTokenBlacklistCache));
        return cacheManager;
    }

    @Bean
    public Cache accessTokenBlacklistCache() {
        return new CaffeineCache("accessTokenBlacklist", Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.HOURS)
            .maximumSize(MAXIMUM_CACHE_SIZE)
            .build());
    }

    @Bean
    public Cache refreshTokenBlacklistCache() {
        return new CaffeineCache("refreshTokenBlacklist", Caffeine.newBuilder()
            .expireAfterWrite(7, TimeUnit.DAYS)
            .maximumSize(MAXIMUM_CACHE_SIZE)
            .build());
    }
}
