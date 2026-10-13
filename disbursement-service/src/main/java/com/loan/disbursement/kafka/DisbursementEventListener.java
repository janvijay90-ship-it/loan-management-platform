package com.loan.disbursement.kafka;

import com.loan.disbursement.dto.LoanApprovedEvent;
import com.loan.disbursement.service.DisbursementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class DisbursementEventListener {

    private static final Logger log = LoggerFactory.getLogger(DisbursementEventListener.class);

    private final DisbursementService disbursementService;

    public DisbursementEventListener(DisbursementService disbursementService) {
        this.disbursementService = disbursementService;
    }

    @KafkaListener(topics = "loan-approved", groupId = "disbursement-service-group")
    public void handleLoanApprovedEvent(Map<String, Object> eventPayload) {
        log.info("Received loan-approved event payload: {}", eventPayload);

        String loanReferenceId = (String) eventPayload.get("loanReferenceId");
        String customerId = (String) eventPayload.get("customerId");
        
        Object amountObj = eventPayload.get("requestedAmount");
        if (amountObj == null) {
            amountObj = eventPayload.get("amount");
        }
        BigDecimal amount = amountObj != null ? new BigDecimal(amountObj.toString()) : BigDecimal.ZERO;

        String status = (String) eventPayload.getOrDefault("status", "APPROVED");

        LoanApprovedEvent event = new LoanApprovedEvent(loanReferenceId, customerId, amount, status);
        disbursementService.processDisbursement(event);
    }
}
