package com.loan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.loan.enums.LoanStatus;

public class LoanResponseDTO {
    private Long id; 
    private String loanReferenceId;
    private String customerId;
    private String applicantName;
    private String panNumber;
    private String phoneNumber;
    private BigDecimal requestedAmount;
    private Integer creditScore;
    private LoanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LoanResponseDTO() {
    }

    public LoanResponseDTO(Long id, String loanReferenceId, String customerId, String applicantName, 
                           String panNumber, String phoneNumber, BigDecimal requestedAmount, 
                           Integer creditScore, LoanStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.loanReferenceId = loanReferenceId;
        this.customerId = customerId;
        this.applicantName = applicantName;
        this.panNumber = panNumber;
        this.phoneNumber = phoneNumber;
        this.requestedAmount = requestedAmount;
        this.creditScore = creditScore;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
