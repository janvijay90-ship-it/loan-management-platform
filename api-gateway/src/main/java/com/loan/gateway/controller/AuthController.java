package com.loan.gateway.controller;

import com.loan.gateway.security.JwtTokenUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;

    public AuthController(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> generateToken(@RequestBody(required = false) Map<String, String> request) {
        String username = (request != null && request.containsKey("username")) ? request.get("username") : "client-user";
        String role = (request != null && request.containsKey("role")) ? request.get("role") : "CUSTOMER";

        String token = jwtTokenUtil.generateToken(username, role);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", token);
        response.put("tokenType", "Bearer");
        response.put("expiresInSeconds", 86400);
        response.put("username", username);

        return ResponseEntity.ok(response);
    }
}
