package com.loan.risk.service;

import com.loan.risk.dto.CibilScoreResult;
import com.loan.risk.entity.RiskAssessment;
import com.loan.risk.kafka.RiskEventProducer;
import com.loan.risk.repository.RiskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class RiskService {

    private static final Logger log = LoggerFactory.getLogger(RiskService.class);

    private final RiskRepository riskRepository;
    private final RiskEventProducer riskEventProducer;
    private final CibilBureauService cibilBureauService;

    public RiskService(RiskRepository riskRepository, 
                       RiskEventProducer riskEventProducer, 
                       CibilBureauService cibilBureauService) {
        this.riskRepository = riskRepository;
        this.riskEventProducer = riskEventProducer;
        this.cibilBureauService = cibilBureauService;
    }

    @Transactional
    public RiskAssessment evaluateRisk(String loanReferenceId, String customerId, String applicantName, 
                                       String panNumber, String phoneNumber, BigDecimal requestedAmount) {
        log.info("Evaluating risk for Loan Reference: {}, PAN: {}", loanReferenceId, panNumber);

        // Fetch CIBIL Score from Bureau (or Redis cache)
        CibilScoreResult cibilResult = cibilBureauService.fetchCibilScore(panNumber, applicantName, phoneNumber);
        int cibilScore = cibilResult.getCibilScore();

        String status;
        String remarks;

        // Enterprise Underwriting Rules:
        // Score >= 650 -> APPROVED
        // Score 600-649 -> MANUAL_REVIEW
        // Score < 600 -> REJECTED
        if (cibilScore >= 650) {
            status = "APPROVED";
            remarks = "CIBIL score (" + cibilScore + ") meets prime lending criteria (>= 650).";
        } else if (cibilScore >= 600) {
            status = "MANUAL_REVIEW";
            remarks = "CIBIL score (" + cibilScore + ") requires underwriter manual review (600 - 649).";
        } else {
            status = "REJECTED";
            remarks = "CIBIL score (" + cibilScore + ") is below minimum cutoff threshold of 600.";
        }

        RiskAssessment assessment = new RiskAssessment(
                loanReferenceId,
                customerId,
                applicantName,
                panNumber,
                phoneNumber,
                requestedAmount,
                cibilScore,
                cibilResult.getBureauReferenceNumber(),
                status,
                remarks
        );

        RiskAssessment savedAssessment = riskRepository.save(assessment);
        log.info("Risk assessment saved successfully with status: {}, CIBIL Score: {}", status, cibilScore);

        // Publish event to loan-evaluated-topic
        riskEventProducer.publishLoanEvaluatedEvent(savedAssessment);

        return savedAssessment;
    }
}