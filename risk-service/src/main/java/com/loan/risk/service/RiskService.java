package com.loan.risk.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loan.risk.entity.RiskAssessment;
import com.loan.risk.kafka.RiskEventProducer;
import com.loan.risk.repository.RiskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskService {

    private final RiskRepository riskRepository;
    
    private final RiskEventProducer riskEventProducer;

    @Transactional
    public RiskAssessment evaluateRisk(String loanReferenceId, String customerId, Integer creditScore, BigDecimal requestedAmount) {
        log.info("Evaluating risk for Loan Reference: {} with Credit Score: {}", loanReferenceId, creditScore);

        String status;
        String remarks;

        // Simple enterprise credit rule engine logic
        if (creditScore >= 700) {
            status = "APPROVED";
            remarks = "Credit score meets prime lending criteria.";
        } else if (creditScore >= 600) {
            status = "MANUAL_REVIEW";
            remarks = "Credit score requires manual loan officer underwriting.";
        } else {
            status = "REJECTED";
            remarks = "Credit score is below the minimum threshold of 600.";
        }

        RiskAssessment assessment = RiskAssessment.builder()
                .loanReferenceId(loanReferenceId)
                .customerId(customerId)
                .creditScore(creditScore)
                .requestedAmount(requestedAmount)
                .riskStatus(status)
                .remarks(remarks)
                .build();

        RiskAssessment savedAssessment = riskRepository.save(assessment);
        riskEventProducer.publishLoanEvaluatedEvent(savedAssessment);
        log.info("Risk assessment saved and event published successfully.");
        return savedAssessment;
    }
}