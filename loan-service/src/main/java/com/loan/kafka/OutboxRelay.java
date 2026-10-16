package com.loan.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan.entity.OutboxEvent;
import com.loan.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxRelay(OutboxRepository outboxRepository, 
                       KafkaTemplate<String, Object> kafkaTemplate,
                       ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void pollAndPublishOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Outbox Relay found {} pending events to publish to Kafka.", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                log.info("Outbox Relay publishing event [id={}, topic={}, aggregateId={}]", 
                        event.getId(), event.getTopic(), event.getAggregateId());

                Object payloadObject = objectMapper.readValue(event.getPayload(), Map.class);
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), payloadObject).get();

                event.setStatus("PUBLISHED");
                event.setPublishedAt(LocalDateTime.now());
                outboxRepository.save(event);
                log.info("Outbox event [id={}] marked as PUBLISHED.", event.getId());
            } catch (Exception ex) {
                log.error("Outbox Relay failed to publish event [id={}, topic={}]: {}", 
                        event.getId(), event.getTopic(), ex.getMessage());
                // Will retry on next scheduled execution
            }
        }
    }
}
