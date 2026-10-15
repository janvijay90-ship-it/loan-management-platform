package com.loan.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;

@RestController
public class GatewayProxyController {

    private static final Logger log = LoggerFactory.getLogger(GatewayProxyController.class);

    private final RestTemplate restTemplate;
    private final String loanServiceUrl;
    private final String disbursementServiceUrl;

    public GatewayProxyController(RestTemplate restTemplate,
                                  @Value("${services.loan-service.url:http://localhost:8081}") String loanServiceUrl,
                                  @Value("${services.disbursement-service.url:http://localhost:8083}") String disbursementServiceUrl) {
        this.restTemplate = restTemplate;
        this.loanServiceUrl = loanServiceUrl;
        this.disbursementServiceUrl = disbursementServiceUrl;
    }

    @RequestMapping("/api/v1/loans/**")
    public ResponseEntity<?> routeLoanService(HttpServletRequest request, 
                                             @RequestBody(required = false) byte[] body, 
                                             HttpMethod method) {
        return forwardRequest(request, body, method, loanServiceUrl);
    }

    @RequestMapping("/api/v1/disbursements/**")
    public ResponseEntity<?> routeDisbursementService(HttpServletRequest request, 
                                                     @RequestBody(required = false) byte[] body, 
                                                     HttpMethod method) {
        return forwardRequest(request, body, method, disbursementServiceUrl);
    }

    private ResponseEntity<?> forwardRequest(HttpServletRequest request, 
                                             byte[] body, 
                                             HttpMethod method, 
                                             String targetBaseUrl) {
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String targetUrl = targetBaseUrl + path + (queryString != null ? "?" + queryString : "");

        log.info("Gateway routing {} {} -> {}", method, path, targetUrl);

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.equalsIgnoreCase(HttpHeaders.HOST) && 
                !headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                headers.addAll(headerName, Collections.list(request.getHeaders(headerName)));
            }
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(URI.create(targetUrl), method, entity, byte[].class);
        } catch (HttpStatusCodeException ex) {
            log.error("Downstream service responded with error status: {}", ex.getStatusCode());
            return ResponseEntity.status(ex.getStatusCode())
                    .headers(ex.getResponseHeaders())
                    .body(ex.getResponseBodyAsByteArray());
        } catch (Exception ex) {
            log.error("Gateway error communicating with downstream service {}: {}", targetUrl, ex.getMessage());
            return ResponseEntity.status(502)
                    .body("{\"error\": \"Bad Gateway\", \"message\": \"Failed to connect to downstream service.\"}");
        }
    }
}
