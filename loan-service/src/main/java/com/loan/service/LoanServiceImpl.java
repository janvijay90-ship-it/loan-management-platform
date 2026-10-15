package com.loan.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan.entity.OutboxEvent;
import com.loan.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loan.dto.LoanRequestDTO;
import com.loan.dto.LoanResponseDTO;
import com.loan.entity.LoanApplication;
import com.loan.enums.LoanStatus;
import com.loan.kafka.LoanEventProducer;
import com.loan.repository.LoanRepository;

@Service
public class LoanServiceImpl implements LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanServiceImpl.class);

    private final LoanRepository loanRepository;
    private final OutboxRepository outboxRepository;
    private final LoanEventProducer loanEventProducer;
    private final ObjectMapper objectMapper;

    public LoanServiceImpl(LoanRepository loanRepository, 
                           OutboxRepository outboxRepository,
                           LoanEventProducer loanEventProducer, 
                           ObjectMapper objectMapper) {
        this.loanRepository = loanRepository;
        this.outboxRepository = outboxRepository;
        this.loanEventProducer = loanEventProducer;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public LoanResponseDTO applyForLoan(LoanRequestDTO requestDTO) {
        log.info("Processing loan application for customer: {}, PAN: {}", 
                requestDTO.getCustomerId(), requestDTO.getPanNumber());

        String referenceId = "LOAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        LoanApplication loan = new LoanApplication(
                referenceId,
                requestDTO.getCustomerId(),
                requestDTO.getApplicantName(),
                requestDTO.getPanNumber().toUpperCase(),
                requestDTO.getPhoneNumber(),
                requestDTO.getRequestedAmount(),
                null, // creditScore is null until bureau evaluation
                LoanStatus.SUBMITTED
        );

        // 1. Save to PostgreSQL (loan_db)
        LoanApplication savedLoan = loanRepository.save(loan);
        log.info("Loan saved successfully with Reference ID: {}", savedLoan.getLoanReferenceId());

        LoanResponseDTO responseDTO = mapToDTO(savedLoan);

        // 2. Transactional Outbox Pattern: Insert event into 'outbox_events' table in the same transaction
        try {
            String jsonPayload = objectMapper.writeValueAsString(responseDTO);
            OutboxEvent outboxEvent = new OutboxEvent(
                    "LOAN",
                    referenceId,
                    "LOAN_SUBMITTED",
                    "loan-submitted",
                    jsonPayload
            );
            outboxRepository.save(outboxEvent);
            log.info("Transactional Outbox: Event inserted for loan reference: {}", referenceId);
        } catch (Exception e) {
            log.error("Failed to serialize outbox event: {}", e.getMessage(), e);
        }

        // 3. Publish directly as well for zero-delay (OutboxRelay acts as guaranteed delivery safety net)
        loanEventProducer.publishLoanSubmittedEvent(responseDTO);

        return responseDTO;
    }

    @Override
    public LoanResponseDTO getLoanById(Long id) {
        LoanApplication loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + id));
        return mapToDTO(loan);
    }

    @Override
    public LoanResponseDTO getLoanByReferenceId(String referenceId) {
        LoanApplication loan = loanRepository.findByLoanReferenceId(referenceId)
                .orElseThrow(() -> new RuntimeException("Loan not found with reference ID: " + referenceId));
        return mapToDTO(loan);
    }

    @Override
    public List<LoanResponseDTO> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void updateLoanStatus(String loanReferenceId, String status) {
        log.info("Updating loan status for Reference ID: {} to {}", loanReferenceId, status);
        LoanApplication loan = loanRepository.findByLoanReferenceId(loanReferenceId)
                .orElseThrow(() -> new RuntimeException("Loan not found with reference ID: " + loanReferenceId));
        
        LoanStatus newStatus = LoanStatus.valueOf(status);
        loan.setStatus(newStatus);
        LoanApplication updatedLoan = loanRepository.save(loan);
        log.info("Loan status updated successfully in loan_db to {}.", newStatus);

        // If the loan is APPROVED, publish loan-approved event so disbursement-service can process payout
        if (newStatus == LoanStatus.APPROVED) {
            try {
                LoanResponseDTO responseDTO = mapToDTO(updatedLoan);
                String jsonPayload = objectMapper.writeValueAsString(responseDTO);
                
                OutboxEvent outboxEvent = new OutboxEvent(
                        "LOAN",
                        loanReferenceId,
                        "LOAN_APPROVED",
                        "loan-approved",
                        jsonPayload
                );
                outboxRepository.save(outboxEvent);
                log.info("Transactional Outbox: loan-approved event queued for reference: {}", loanReferenceId);
                
                // Also publish directly for immediate processing
                loanEventProducer.publishLoanApprovedEvent(responseDTO);
            } catch (Exception e) {
                log.error("Failed to queue loan-approved event: {}", e.getMessage(), e);
            }
        }
    }

    private LoanResponseDTO mapToDTO(LoanApplication loan) {
        return new LoanResponseDTO(
                loan.getId(),
                loan.getLoanReferenceId(),
                loan.getCustomerId(),
                loan.getApplicantName(),
                loan.getPanNumber(),
                loan.getPhoneNumber(),
                loan.getRequestedAmount(),
                loan.getCreditScore(),
                loan.getStatus(),
                loan.getCreatedAt(),
                loan.getUpdatedAt()
        );
    }
}