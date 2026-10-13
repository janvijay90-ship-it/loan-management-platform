package com.loan.disbursement.service;

import com.loan.disbursement.dto.DisbursementCompletedEvent;
import com.loan.disbursement.dto.LoanApprovedEvent;
import com.loan.disbursement.entity.DisbursementRecord;
import com.loan.disbursement.kafka.DisbursementEventProducer;
import com.loan.disbursement.repository.DisbursementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
public class DisbursementServiceImpl implements DisbursementService {

    private static final Logger log = LoggerFactory.getLogger(DisbursementServiceImpl.class);

    private final DisbursementRepository disbursementRepository;
    private final DisbursementEventProducer eventProducer;
    private final StringRedisTemplate redisTemplate;

    public DisbursementServiceImpl(DisbursementRepository disbursementRepository,
                                  DisbursementEventProducer eventProducer,
                                  StringRedisTemplate redisTemplate) {
        this.disbursementRepository = disbursementRepository;
        this.eventProducer = eventProducer;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public DisbursementRecord processDisbursement(LoanApprovedEvent event) {
        String loanRef = event.getLoanReferenceId();
        String idempotencyKey = "idempotency:" + loanRef;

        log.info("Processing payout for Loan Reference: {}", loanRef);

        // Redis Idempotency Check: Prevent duplicate payouts
        Boolean isNewRequest = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "PROCESSING", Duration.ofHours(24));

        if (Boolean.FALSE.equals(isNewRequest)) {
            log.warn("Idempotency check failed: Loan Reference {} was already processed or is currently processing. Skipping payout.", loanRef);
            return disbursementRepository.findByLoanReferenceId(loanRef).orElse(null);
        }

        // Simulate Payout Gateway Processing
        String payoutTransactionId = "PAYOUT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Generated Payout Transaction ID: {} for Loan Reference: {}", payoutTransactionId, loanRef);

        DisbursementRecord record = new DisbursementRecord(
                loanRef,
                event.getCustomerId(),
                event.getAmount(),
                "SUCCESS",
                payoutTransactionId
        );

        DisbursementRecord savedRecord = disbursementRepository.save(record);
        redisTemplate.opsForValue().set(idempotencyKey, "COMPLETED", Duration.ofHours(24));

        log.info("Payout successfully recorded in disbursement_db for Loan Reference: {}", loanRef);

        // Publish Kafka Event: disbursement-completed
        DisbursementCompletedEvent completedEvent = new DisbursementCompletedEvent(
                savedRecord.getLoanReferenceId(),
                savedRecord.getCustomerId(),
                savedRecord.getAmount(),
                savedRecord.getStatus(),
                savedRecord.getPayoutTransactionId(),
                savedRecord.getDisbursedAt()
        );

        eventProducer.publishDisbursementCompletedEvent(completedEvent);

        return savedRecord;
    }
}
