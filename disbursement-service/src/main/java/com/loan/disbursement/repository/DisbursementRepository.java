package com.loan.disbursement.repository;

import com.loan.disbursement.entity.DisbursementRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisbursementRepository extends JpaRepository<DisbursementRecord, Long> {
    Optional<DisbursementRecord> findByLoanReferenceId(String loanReferenceId);
}
