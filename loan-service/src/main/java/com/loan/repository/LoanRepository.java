package com.loan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loan.entity.LoanApplication;

public interface LoanRepository extends JpaRepository<LoanApplication, Long> {
    Optional<LoanApplication> findByLoanReferenceId(String loanReferenceId);
}
