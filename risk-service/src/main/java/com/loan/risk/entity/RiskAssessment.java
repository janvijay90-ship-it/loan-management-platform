package com.loan.risk.entity;

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
@Table(name = "risk_assessments")
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loanReferenceId;

    @Column(nullable = false)
    private String customerId;

    @Column
    private String applicantName;

    @Column(nullable = false)
    private String panNumber;

    @Column
    private String phoneNumber;

    @Column(nullable = false)
    private BigDecimal requestedAmount;

    @Column(nullable = false)
    private Integer cibilScore;

    @Column
    private String bureauReferenceNumber;

    @Column(nullable = false)
    private String riskStatus; // APPROVED, MANUAL_REVIEW, REJECTED

    private String remarks;

    private LocalDateTime evaluatedAt;

    public RiskAssessment() {
    }

    public RiskAssessment(String loanReferenceId, String customerId, String applicantName, 
                          String panNumber, String phoneNumber, BigDecimal requestedAmount, 
                          Integer cibilScore, String bureauReferenceNumber, String riskStatus, String remarks) {
        this.loanReferenceId = loanReferenceId;
        this.customerId = customerId;
        this.applicantName = applicantName;
        this.panNumber = panNumber;
        this.phoneNumber = phoneNumber;
        this.requestedAmount = requestedAmount;
        this.cibilScore = cibilScore;
        this.bureauReferenceNumber = bureauReferenceNumber;
        this.riskStatus = riskStatus;
        this.remarks = remarks;
    }

    @PrePersist
    public void prePersist() {
        if (this.evaluatedAt == null) {
            this.evaluatedAt = LocalDateTime.now();
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

    public Integer getCibilScore() {
        return cibilScore;
    }

    public void setCibilScore(Integer cibilScore) {
        this.cibilScore = cibilScore;
    }

    public String getBureauReferenceNumber() {
        return bureauReferenceNumber;
    }

    public void setBureauReferenceNumber(String bureauReferenceNumber) {
        this.bureauReferenceNumber = bureauReferenceNumber;
    }

    public String getRiskStatus() {
        return riskStatus;
    }

    public void setRiskStatus(String riskStatus) {
        this.riskStatus = riskStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}