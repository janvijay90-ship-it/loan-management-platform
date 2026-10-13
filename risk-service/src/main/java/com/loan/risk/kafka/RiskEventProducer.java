package com.loan.risk.kafka;

import com.loan.risk.entity.RiskAssessment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RiskEventProducer {

    private static final Logger log = LoggerFactory.getLogger(RiskEventProducer.class);
    private static final String TOPIC = "loan-evaluated-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RiskEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishLoanEvaluatedEvent(RiskAssessment assessment) {
        log.info("Publishing loan-evaluated event for Loan Reference: {} with status: {} (CIBIL: {})", 
                assessment.getLoanReferenceId(), assessment.getRiskStatus(), assessment.getCibilScore());
        kafkaTemplate.send(TOPIC, assessment.getLoanReferenceId(), assessment);
    }
}