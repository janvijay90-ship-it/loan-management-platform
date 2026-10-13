package com.loan.risk.repository;

import com.loan.risk.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RiskRepository extends JpaRepository<RiskAssessment, Long> {
    Optional<RiskAssessment> findByLoanReferenceId(String loanReferenceId);
}