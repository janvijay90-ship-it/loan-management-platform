package com.loan.service;

import java.util.List;

import com.loan.dto.LoanRequestDTO;
import com.loan.dto.LoanResponseDTO;

public interface LoanService {
    LoanResponseDTO applyForLoan(LoanRequestDTO requestDTO);
    LoanResponseDTO getLoanById(Long id);
    LoanResponseDTO getLoanByReferenceId(String loanReferenceId);
    List<LoanResponseDTO> getAllLoans();
    
    void updateLoanStatus(String loanReferenceId, String status);
    
 }
