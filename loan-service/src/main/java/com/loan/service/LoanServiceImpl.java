package com.loan.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loan.dto.LoanRequestDTO;
import com.loan.dto.LoanResponseDTO;
import com.loan.entity.LoanApplication;
import com.loan.enums.LoanStatus;
import com.loan.kafka.LoanEventProducer;
import com.loan.repository.LoanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanEventProducer loanEventProducer; // Inject Kafka Producer

    @Override
    @Transactional
    public LoanResponseDTO applyForLoan(LoanRequestDTO requestDTO) {
        log.info("Processing loan application for customer: {}", requestDTO.getCustomerId());

        String referenceId = "LOAN-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        LoanApplication loan = LoanApplication.builder()
                .loanReferenceId(referenceId)
                .customerId(requestDTO.getCustomerId())
                .requestedAmount(requestDTO.getRequestedAmount())
                .creditScore(requestDTO.getCreditScore())
                .status(LoanStatus.SUBMITTED)
                .build();

        // Save to PostgreSQL (loan_db)
        LoanApplication savedLoan = loanRepository.save(loan);
        log.info("Loan saved successfully with Reference ID: {}", savedLoan.getLoanReferenceId());

        // Map Entity to Response DTO
        LoanResponseDTO responseDTO = mapToDTO(savedLoan);

        // Publish event to Kafka so risk-service can consume it
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
        return LoanResponseDTO.builder()
                .id(loan.getId())
                .loanReferenceId(loan.getLoanReferenceId())
                .customerId(loan.getCustomerId())
                .requestedAmount(loan.getRequestedAmount())
                .creditScore(loan.getCreditScore())
                .status(loan.getStatus())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }
}