package com.loan.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class JwtTokenUtil {

    private final String secret;

    public JwtTokenUtil(@Value("${gateway.jwt.secret:DefaultSecretKeyForLoanManagementPlatform2026!}") String secret) {
        this.secret = secret;
    }

    public String generateToken(String username, String role) {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        long exp = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 hours
        String payload = String.format("{\"sub\":\"%s\",\"role\":\"%s\",\"exp\":%d}", username, role, exp);

        String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        String content = encodedHeader + "." + encodedPayload;
        String signature = sign(content, secret);

        return content + "." + signature;
    }

    public boolean validateToken(String token) {
        if (token == null || !token.contains(".")) {
            return false;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        String content = parts[0] + "." + parts[1];
        String expectedSignature = sign(content, secret);

        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8), 
                parts[2].getBytes(StandardCharsets.UTF_8)
        );
    }

    public String getUsernameFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            int subIdx = payloadJson.indexOf("\"sub\":\"");
            if (subIdx != -1) {
                int endIdx = payloadJson.indexOf("\"", subIdx + 7);
                return payloadJson.substring(subIdx + 7, endIdx);
            }
        } catch (Exception ignored) {
        }
        return "anonymous";
    }

    private String sign(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating HMAC-SHA256", e);
        }
    }
}
