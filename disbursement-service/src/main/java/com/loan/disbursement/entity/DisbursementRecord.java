package com.loan.disbursement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "disbursement_records")
public class DisbursementRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loanReferenceId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, unique = true)
    private String payoutTransactionId;

    private LocalDateTime disbursedAt;

    public DisbursementRecord() {
    }

    public DisbursementRecord(String loanReferenceId, String customerId, BigDecimal amount, String status, String payoutTransactionId) {
        this.loanReferenceId = loanReferenceId;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
        this.payoutTransactionId = payoutTransactionId;
    }

    @PrePersist
    public void prePersist() {
        if (this.disbursedAt == null) {
            this.disbursedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
