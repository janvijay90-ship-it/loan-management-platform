package com.loan.gateway.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private final StringRedisTemplate redisTemplate;
    private final int maxRequestsPerMinute;

    public RateLimiterService(StringRedisTemplate redisTemplate,
                              @Value("${gateway.rate-limit.requests-per-minute:60}") int maxRequestsPerMinute) {
        this.redisTemplate = redisTemplate;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }

    public boolean isAllowed(String clientIdentifier) {
        String key = RATE_LIMIT_PREFIX + clientIdentifier;

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(60));
            }

            if (count != null && count > maxRequestsPerMinute) {
                log.warn("Rate limit exceeded for client: {}. Current count: {}, Max allowed: {}", 
                        clientIdentifier, count, maxRequestsPerMinute);
                return false;
            }
            return true;
        } catch (Exception ex) {
            // Fail open if Redis is temporarily unavailable in dev
            log.warn("Redis rate limiter unavailable: {}. Allowing request by default.", ex.getMessage());
            return true;
        }
    }
}
