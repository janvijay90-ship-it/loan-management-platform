package com.loan.risk.kafka;

import com.loan.risk.entity.RiskAssessment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RiskEventProducer {

    private static final Logger log = LoggerFactory.getLogger(RiskEventProducer.class);
    public static final String TOPIC_CREDIT_EVALUATED = "credit-evaluated";
    public static final String TOPIC_LEGACY = "loan-evaluated-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RiskEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishLoanEvaluatedEvent(RiskAssessment assessment) {
        log.info("Publishing credit-evaluated event for Loan Reference: {} with status: {} (CIBIL: {})", 
                assessment.getLoanReferenceId(), assessment.getRiskStatus(), assessment.getCibilScore());
        
        // Publish to primary topic 'credit-evaluated' as per architecture diagram
        kafkaTemplate.send(TOPIC_CREDIT_EVALUATED, assessment.getLoanReferenceId(), assessment);
        // Also send to legacy topic for compatibility
        kafkaTemplate.send(TOPIC_LEGACY, assessment.getLoanReferenceId(), assessment);
    }
}