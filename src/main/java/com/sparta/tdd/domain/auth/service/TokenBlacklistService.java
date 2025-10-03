package com.sparta.tdd.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final CacheManager cacheManager;

    public void addAccessTokenToBlacklist(String token) {
        Cache cache = cacheManager.getCache("accessTokenBlacklist");
        if (cache != null) {
            cache.put(token, true);
        }
    }

    public void addRefreshTokenToBlacklist(String token) {
        Cache cache = cacheManager.getCache("refreshTokenBlacklist");
        if (cache != null) {
            cache.put(token, true);
        }
    }

    public boolean isAccessTokenBlacklisted(String token) {
        Cache cache = cacheManager.getCache("accessTokenBlacklist");
        return cache != null && cache.get(token) != null;
    }

    public boolean isRefreshTokenBlacklisted(String token) {
        Cache cache = cacheManager.getCache("refreshTokenBlacklist");
        return cache != null && cache.get(token) != null;
    }
}