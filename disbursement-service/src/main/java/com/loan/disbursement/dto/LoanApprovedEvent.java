package com.loan.disbursement.dto;

import java.math.BigDecimal;

public class LoanApprovedEvent {

    private String loanReferenceId;
    private String customerId;
    private BigDecimal amount;
    private String status;

    public LoanApprovedEvent() {
    }

    public LoanApprovedEvent(String loanReferenceId, String customerId, BigDecimal amount, String status) {
        this.loanReferenceId = loanReferenceId;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
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
}
