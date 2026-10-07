package com.loan.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.loan.dto.LoanRequestDTO;
import com.loan.dto.LoanResponseDTO;
import com.loan.entity.LoanApplication;
import com.loan.enums.LoanStatus;
import com.loan.repository.LoanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    @Override
    public LoanResponseDTO applyForLoan(LoanRequestDTO requestDTO) {
        LoanApplication loan = LoanApplication.builder()
                .loanReferenceId("LN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customerId(requestDTO.getCustomerId())
                .requestedAmount(requestDTO.getRequestedAmount())
                .creditScore(requestDTO.getCreditScore())
                .status(LoanStatus.SUBMITTED)
                .build();

        LoanApplication savedLoan = loanRepository.save(loan);
        return mapToResponseDTO(savedLoan);
    }

    @Override
    public LoanResponseDTO getLoanById(Long id) {
        LoanApplication loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + id));
        return mapToResponseDTO(loan);
    }

    @Override
    public LoanResponseDTO getLoanByReferenceId(String loanReferenceId) {
        LoanApplication loan = loanRepository.findByLoanReferenceId(loanReferenceId)
                .orElseThrow(() -> new RuntimeException("Loan application not found with reference: " + loanReferenceId));
        return mapToResponseDTO(loan);
    }

    @Override
    public List<LoanResponseDTO> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private LoanResponseDTO mapToResponseDTO(LoanApplication loan) {
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
