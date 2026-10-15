package com.loan.notification.kafka;

import com.loan.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final EmailService emailService;

    public NotificationEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = {"loan-submitted", "loan-submitted-topic"}, groupId = "notification-service-group")
    public void handleLoanSubmittedEvent(Map<String, Object> eventPayload) {
        log.info("Notification Service received loan-submitted event: {}", eventPayload);

        String loanReferenceId = (String) eventPayload.get("loanReferenceId");
        String customerId = (String) eventPayload.get("customerId");
        String applicantName = (String) eventPayload.get("applicantName");

        Object amountObj = eventPayload.get("requestedAmount");
        BigDecimal amount = amountObj != null ? new BigDecimal(amountObj.toString()) : BigDecimal.ZERO;

        emailService.sendApplicationReceivedEmail(loanReferenceId, applicantName, customerId, amount);
    }

    @KafkaListener(topics = {"credit-evaluated", "loan-evaluated-topic"}, groupId = "notification-service-eval-group")
    public void handleCreditEvaluatedEvent(Map<String, Object> eventPayload) {
        log.info("Notification Service received credit-evaluated event: {}", eventPayload);

        String loanReferenceId = (String) eventPayload.get("loanReferenceId");
        String status = (String) eventPayload.get("riskStatus");
        String remarks = (String) eventPayload.get("remarks");

        emailService.sendDecisionEmail(loanReferenceId, status, remarks);
    }

    @KafkaListener(topics = "disbursement-completed", groupId = "notification-service-disb-group")
    public void handleDisbursementCompletedEvent(Map<String, Object> eventPayload) {
        log.info("Notification Service received disbursement-completed event: {}", eventPayload);

        String loanReferenceId = (String) eventPayload.get("loanReferenceId");
        String transactionId = (String) eventPayload.get("payoutTransactionId");

        Object amountObj = eventPayload.get("amount");
        BigDecimal amount = amountObj != null ? new BigDecimal(amountObj.toString()) : BigDecimal.ZERO;

        emailService.sendDisbursementCompletedEmail(loanReferenceId, amount, transactionId);
    }
}
