package com.loan.kafka;

import com.loan.service.LoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LoanEventListener {

    private static final Logger log = LoggerFactory.getLogger(LoanEventListener.class);

    private final LoanService loanService;

    public LoanEventListener(LoanService loanService) {
        this.loanService = loanService;
    }

    @KafkaListener(topics = {"credit-evaluated", "loan-evaluated-topic"}, groupId = "loan-service-group")
    public void handleCreditEvaluatedEvent(Map<String, Object> eventPayload) {
        log.info("Received credit-evaluated event: {}", eventPayload);

        String loanReferenceId = (String) eventPayload.get("loanReferenceId");
        String riskStatus = (String) eventPayload.get("riskStatus");

        if (loanReferenceId != null && riskStatus != null) {
            loanService.updateLoanStatus(loanReferenceId, riskStatus);
        } else {
            log.error("Invalid event payload: missing loanReferenceId or riskStatus");
        }
    }

    @KafkaListener(topics = "disbursement-completed", groupId = "loan-service-disbursement-group")
    public void handleDisbursementCompletedEvent(Map<String, Object> eventPayload) {
        log.info("Received disbursement-completed event: {}", eventPayload);

        String loanReferenceId = (String) eventPayload.get("loanReferenceId");
        String status = (String) eventPayload.getOrDefault("status", "SUCCESS");

        if (loanReferenceId != null && "SUCCESS".equalsIgnoreCase(status)) {
            log.info("Disbursement succeeded. Transitioning final loan status to ACTIVE for: {}", loanReferenceId);
            loanService.updateLoanStatus(loanReferenceId, "ACTIVE");
        }
    }
}