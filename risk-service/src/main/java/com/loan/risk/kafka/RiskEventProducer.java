package com.loan.risk.kafka;

import com.loan.risk.entity.RiskAssessment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiskEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "loan-evaluated-topic";

    public void publishLoanEvaluatedEvent(RiskAssessment assessment) {
        log.info("Publishing loan-evaluated event for Loan Reference: {} with status: {}", 
                assessment.getLoanReferenceId(), assessment.getRiskStatus());
        kafkaTemplate.send(TOPIC, assessment.getLoanReferenceId(), assessment);
    }
}