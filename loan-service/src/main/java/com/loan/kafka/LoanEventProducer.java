package com.loan.kafka;

import com.loan.dto.LoanResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "loan-submitted-topic";

    public void publishLoanSubmittedEvent(LoanResponseDTO loanResponse) {
        log.info("Publishing loan-submitted event for Loan Reference: {}", loanResponse.getLoanReferenceId());
        kafkaTemplate.send(TOPIC, loanResponse.getLoanReferenceId(), loanResponse);
    }
}