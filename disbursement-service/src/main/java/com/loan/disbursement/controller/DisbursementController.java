package com.loan.disbursement.controller;

import com.loan.disbursement.entity.DisbursementRecord;
import com.loan.disbursement.repository.DisbursementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/disbursements")
public class DisbursementController {

    private static final Logger log = LoggerFactory.getLogger(DisbursementController.class);

    private final DisbursementRepository disbursementRepository;

    public DisbursementController(DisbursementRepository disbursementRepository) {
        this.disbursementRepository = disbursementRepository;
    }

    @GetMapping("/loan/{loanReferenceId}")
    public ResponseEntity<DisbursementRecord> getDisbursementByLoanReferenceId(@PathVariable String loanReferenceId) {
        log.info("REST: Querying disbursement for Loan Reference: {}", loanReferenceId);
        return disbursementRepository.findByLoanReferenceId(loanReferenceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisbursementRecord> getDisbursementById(@PathVariable Long id) {
        log.info("REST: Querying disbursement with ID: {}", id);
        return disbursementRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<DisbursementRecord>> getAllDisbursements() {
        log.info("REST: Querying all disbursement records");
        return ResponseEntity.ok(disbursementRepository.findAll());
    }
}
