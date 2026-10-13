package com.loan.risk.service;

import com.loan.risk.dto.CibilScoreResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class CibilBureauServiceImpl implements CibilBureauService {

    private static final Logger log = LoggerFactory.getLogger(CibilBureauServiceImpl.class);
    private static final String REDIS_CIBIL_KEY_PREFIX = "cibil:";

    private final StringRedisTemplate redisTemplate;

    public CibilBureauServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public CibilScoreResult fetchCibilScore(String panNumber, String applicantName, String phoneNumber) {
        String normalizedPan = panNumber != null ? panNumber.trim().toUpperCase() : "UNKNOWN";
        String redisKey = REDIS_CIBIL_KEY_PREFIX + normalizedPan;

        // 1. Check Redis Cache (Enquiry deduplication / cost optimization)
        String cachedValue = redisTemplate.opsForValue().get(redisKey);
        if (cachedValue != null && cachedValue.contains(":")) {
            String[] parts = cachedValue.split(":");
            int score = Integer.parseInt(parts[0]);
            String bureauRef = parts[1];
            log.info("Redis CIBIL Cache HIT for PAN {}. Score: {}, Bureau Reference: {}", normalizedPan, score, bureauRef);
            return new CibilScoreResult(normalizedPan, score, bureauRef, true);
        }

        // 2. Cache MISS: Query Credit Bureau Engine
        log.info("Redis CIBIL Cache MISS for PAN {}. Initiating Credit Bureau enquiry for {} (Mobile: {})...", 
                normalizedPan, applicantName, phoneNumber);

        String bureauRef = "CIBIL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        int calculatedScore = computeBureauScore(normalizedPan);

        log.info("Credit Bureau enquiry successful. PAN: {}, Assigned Score: {}, Bureau Ref: {}", 
                normalizedPan, calculatedScore, bureauRef);

        // 3. Cache score in Redis with 30-day TTL (Standard Banking Inquiry Cache)
        redisTemplate.opsForValue().set(redisKey, calculatedScore + ":" + bureauRef, Duration.ofDays(30));

        return new CibilScoreResult(normalizedPan, calculatedScore, bureauRef, false);
    }

    /**
     * Deterministic Credit Bureau rule logic for development & testing:
     * - Allows easy testability using PAN suffix or keywords.
     * - Default score range: 300 - 850.
     */
    private int computeBureauScore(String pan) {
        if (pan.contains("PRIME") || pan.contains("GOOD")) {
            return 780;
        }
        if (pan.contains("SUBPRIME") || pan.contains("WARN")) {
            return 630;
        }
        if (pan.contains("POOR") || pan.contains("BAD")) {
            return 520;
        }

        // Use PAN numeric characters to determine score deterministically
        // Example: ABCDE1234F -> uses the 4 digits 1234
        String digits = pan.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) {
            char lastDigit = digits.charAt(digits.length() - 1);
            if (lastDigit >= '0' && lastDigit <= '4') {
                return 750 + (lastDigit - '0') * 10; // 750, 760, 770, 780, 790 (Prime: >= 650)
            } else if (lastDigit >= '5' && lastDigit <= '6') {
                return 620 + (lastDigit - '5') * 15; // 620, 635 (Manual review)
            } else {
                return 520 + (lastDigit - '7') * 20; // 520, 540, 560 (Rejected: < 600)
            }
        }

        return 720;
    }
}
