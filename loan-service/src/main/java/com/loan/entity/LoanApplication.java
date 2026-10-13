package com.loan.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.loan.enums.LoanStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loanReferenceId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String applicantName;

    @Column(nullable = false)
    private String panNumber;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private BigDecimal requestedAmount;

    @Column
    private Integer creditScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LoanApplication() {
    }

    public LoanApplication(String loanReferenceId, String customerId, String applicantName, String panNumber, 
                           String phoneNumber, BigDecimal requestedAmount, Integer creditScore, LoanStatus status) {
        this.loanReferenceId = loanReferenceId;
        this.customerId = customerId;
        this.applicantName = applicantName;
        this.panNumber = panNumber;
        this.phoneNumber = phoneNumber;
        this.requestedAmount = requestedAmount;
        this.creditScore = creditScore;
        this.status = status;
    }

    @PrePersist
    public void prePersist() {
        if (this.loanReferenceId == null) {
            this.loanReferenceId = "LOAN-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}