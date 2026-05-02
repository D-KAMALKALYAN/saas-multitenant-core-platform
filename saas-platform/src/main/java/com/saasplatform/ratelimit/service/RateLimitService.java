package com.saasplatform.ratelimit.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final ConcurrentHashMap<String , Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String tenantSlug, String plan){
        return buckets.computeIfAbsent(tenantSlug , key -> createBucket(plan));
    }

    private Bucket createBucket(String plan){
        long limit = switch (plan.toUpperCase()){
            case "PRO" -> 500L;
            case "ENTERPRISE" -> 2000L;
            default -> 3L;
        };

        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit)
                        .refillGreedy(limit , Duration.ofMinutes(1))
                        .build())
                .build();
    }




}
