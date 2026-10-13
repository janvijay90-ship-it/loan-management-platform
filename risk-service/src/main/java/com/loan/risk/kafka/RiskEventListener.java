package com.loan.risk.kafka;

import com.loan.risk.service.RiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiskEventListener {

    private final RiskService riskService;

    @KafkaListener(topics = "loan-submitted-topic", groupId = "risk-service-group")
    public void handleLoanSubmittedEvent(Map<String, Object> eventPayload) {
        log.info("Received loan-submitted event: {}", eventPayload);

        String loanReferenceId = (String) eventPayload.get("loanReferenceId");
        String customerId = (String) eventPayload.get("customerId");
        Integer creditScore = (Integer) eventPayload.get("creditScore");
        BigDecimal requestedAmount = new BigDecimal(eventPayload.get("requestedAmount").toString());

        riskService.evaluateRisk(loanReferenceId, customerId, creditScore, requestedAmount);
    }
}