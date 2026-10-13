package com.loan.disbursement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DisbursementCompletedEvent {

    private String loanReferenceId;
    private String customerId;
    private BigDecimal amount;
    private String status;
    private String payoutTransactionId;
    private LocalDateTime disbursedAt;

    public DisbursementCompletedEvent() {
    }

    public DisbursementCompletedEvent(String loanReferenceId, String customerId, BigDecimal amount, String status, String payoutTransactionId, LocalDateTime disbursedAt) {
        this.loanReferenceId = loanReferenceId;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
        this.payoutTransactionId = payoutTransactionId;
        this.disbursedAt = disbursedAt;
    }

    public String getLoanReferenceId() {
        return loanReferenceId;
    }

    public void setLoanReferenceId(String loanReferenceId) {
        this.loanReferenceId = loanReferenceId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPayoutTransactionId() {
        return payoutTransactionId;
    }

    public void setPayoutTransactionId(String payoutTransactionId) {
        this.payoutTransactionId = payoutTransactionId;
    }

    public LocalDateTime getDisbursedAt() {
        return disbursedAt;
    }

    public void setDisbursedAt(LocalDateTime disbursedAt) {
        this.disbursedAt = disbursedAt;
    }
}
