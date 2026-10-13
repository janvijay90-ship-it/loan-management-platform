package com.loan.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final LoanEventProducer loanEventProducer;

    public LoanServiceImpl(LoanRepository loanRepository, LoanEventProducer loanEventProducer) {
        this.loanRepository = loanRepository;
        this.loanEventProducer = loanEventProducer;
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

        // Save to PostgreSQL (loan_db)
        LoanApplication savedLoan = loanRepository.save(loan);
        log.info("Loan saved successfully with Reference ID: {}", savedLoan.getLoanReferenceId());

        // Map Entity to Response DTO
        LoanResponseDTO responseDTO = mapToDTO(savedLoan);

        // Publish event to Kafka so risk-service can perform CIBIL inquiry
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
        
        loan.setStatus(LoanStatus.valueOf(status));
        loanRepository.save(loan);
        log.info("Loan status updated successfully in loan_db.");
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