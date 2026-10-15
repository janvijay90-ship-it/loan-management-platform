package com.loan.risk.kafka;

import com.loan.risk.service.RiskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class RiskEventListener {

    private static final Logger log = LoggerFactory.getLogger(RiskEventListener.class);

    private final RiskService riskService;

    public RiskEventListener(RiskService riskService) {
        this.riskService = riskService;
    }

    @KafkaListener(topics = {"loan-submitted", "loan-submitted-topic"}, groupId = "risk-service-group")
    public void handleLoanSubmittedEvent(Map<String, Object> eventPayload) {
        log.info("Received loan-submitted event for risk evaluation: {}", eventPayload);

        String loanReferenceId = (String) eventPayload.get("loanReferenceId");
        String customerId = (String) eventPayload.get("customerId");
        String applicantName = (String) eventPayload.get("applicantName");
        String panNumber = (String) eventPayload.get("panNumber");
        String phoneNumber = (String) eventPayload.get("phoneNumber");

        Object amountObj = eventPayload.get("requestedAmount");
        BigDecimal requestedAmount = amountObj != null ? new BigDecimal(amountObj.toString()) : BigDecimal.ZERO;

        riskService.evaluateRisk(loanReferenceId, customerId, applicantName, panNumber, phoneNumber, requestedAmount);
    }
}