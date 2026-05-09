package com.saasplatform.apikey.service;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.saasplatform.apikey.entity.ApiKeyInfo;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ApiKeyCacheService {

    private final Cache<String , ApiKeyInfo> cache =
            Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();

    public ApiKeyInfo get(String hash) {
        return cache.getIfPresent(hash);
    }

    public void put(String hash, ApiKeyInfo info) {
        cache.put(hash, info);
    }

    public void invalidate(String hash) {
        cache.invalidate(hash);
    }
}
