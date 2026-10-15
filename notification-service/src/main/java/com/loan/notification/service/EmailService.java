package com.loan.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void sendApplicationReceivedEmail(String loanReferenceId, String applicantName, String customerId, BigDecimal amount) {
        log.info("==================== [NOTIFICATION: EMAIL DISPATCHED] ====================");
        log.info("TO: {} ({})", applicantName != null ? applicantName : customerId, customerId);
        log.info("SUBJECT: Application Received - Reference: {}", loanReferenceId);
        log.info("BODY: Dear {}, we have received your loan application for Rs. {}. Your reference number is {}. Our risk engine is currently evaluating your application.", 
                applicantName != null ? applicantName : "Applicant", amount, loanReferenceId);
        log.info("=========================================================================");
    }

    public void sendDecisionEmail(String loanReferenceId, String status, String remarks) {
        log.info("==================== [NOTIFICATION: EMAIL DISPATCHED] ====================");
        log.info("SUBJECT: Loan Status Update - {}", loanReferenceId);
        log.info("BODY: Your loan application {} status has been updated to: {}. Remarks: {}", 
                loanReferenceId, status, remarks != null ? remarks : "Underwriting completed.");
        log.info("=========================================================================");
    }

    public void sendDisbursementCompletedEmail(String loanReferenceId, BigDecimal amount, String transactionId) {
        log.info("==================== [NOTIFICATION: EMAIL DISPATCHED] ====================");
        log.info("SUBJECT: Loan Disbursement Completed! - {}", loanReferenceId);
        log.info("BODY: Congratulations! Your loan payout of Rs. {} has been successfully disbursed. Payout Transaction ID: {}.", 
                amount, transactionId);
        log.info("=========================================================================");
    }
}
