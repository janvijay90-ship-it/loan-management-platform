package com.loan.kafka;

import com.loan.dto.LoanResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class LoanEventProducer {

    private static final Logger log = LoggerFactory.getLogger(LoanEventProducer.class);
    
    public static final String TOPIC_LOAN_SUBMITTED = "loan-submitted";
    public static final String TOPIC_LOAN_APPROVED = "loan-approved";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public LoanEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishLoanSubmittedEvent(LoanResponseDTO loanResponse) {
        log.info("Publishing loan-submitted event for Loan Reference: {}, PAN: {}", 
                loanResponse.getLoanReferenceId(), loanResponse.getPanNumber());
        kafkaTemplate.send(TOPIC_LOAN_SUBMITTED, loanResponse.getLoanReferenceId(), loanResponse);
    }

    public void publishLoanApprovedEvent(LoanResponseDTO loanResponse) {
        log.info("Publishing loan-approved event for Loan Reference: {}, Amount: {}", 
                loanResponse.getLoanReferenceId(), loanResponse.getRequestedAmount());
        kafkaTemplate.send(TOPIC_LOAN_APPROVED, loanResponse.getLoanReferenceId(), loanResponse);
    }
}