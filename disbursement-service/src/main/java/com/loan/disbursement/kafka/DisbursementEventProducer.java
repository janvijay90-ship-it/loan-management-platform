package com.loan.disbursement.kafka;

import com.loan.disbursement.dto.DisbursementCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DisbursementEventProducer {

    private static final Logger log = LoggerFactory.getLogger(DisbursementEventProducer.class);
    private static final String TOPIC = "disbursement-completed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DisbursementEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDisbursementCompletedEvent(DisbursementCompletedEvent event) {
        log.info("Publishing disbursement-completed event for Loan Reference: {}", event.getLoanReferenceId());
        kafkaTemplate.send(TOPIC, event.getLoanReferenceId(), event);
    }
}
