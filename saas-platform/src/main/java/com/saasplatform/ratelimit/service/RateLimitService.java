package com.saasplatform.ratelimit.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {


    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    @Value("${app.rate-limit.plans.basic:100}")
    private long basicLimit;

    @Value("${app.rate-limit.plans.pro:500}")
    private long proLimit;

    @Value("${app.rate-limit.plans.enterprise:2000}")
    private long enterpriseLimit;

    private long getLimitForPlan(String plan) {

        if (plan == null) return basicLimit;

        return switch (plan.toUpperCase()) {
            case "PRO"        -> proLimit;
            case "ENTERPRISE" -> enterpriseLimit;
            default           -> basicLimit;
        };
    }


    public Bucket resolveBucket(String tenantSlug, String plan) {
        String cacheKey = tenantSlug + ":" + plan.toUpperCase();
        return buckets.get(cacheKey, key -> createBucket(plan));
    }

    private Bucket createBucket(String plan){
        long limit = getLimitForPlan(plan);

        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit)
                        .refillGreedy(limit , Duration.ofMinutes(1))
                        .build())
                .build();
    }




}
