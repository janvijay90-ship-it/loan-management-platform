package com.loan.kafka;

import com.loan.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanEventListener {

    private final LoanService loanService;

    @KafkaListener(topics = "loan-evaluated-topic", groupId = "loan-service-group")
    public void handleLoanEvaluatedEvent(Map<String, Object> eventPayload) {
        log.info("Received loan-evaluated event: {}", eventPayload);

        String loanReferenceId = (String) eventPayload.get("loanReferenceId");
        String riskStatus = (String) eventPayload.get("riskStatus");

        if (loanReferenceId != null && riskStatus != null) {
            loanService.updateLoanStatus(loanReferenceId, riskStatus);
        } else {
            log.error("Invalid event payload: missing loanReferenceId or riskStatus");
        }
    }
}