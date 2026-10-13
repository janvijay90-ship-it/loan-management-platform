package com.loan.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class LoanRequestDTO {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Applicant name is required")
    private String applicantName;

    @NotBlank(message = "PAN card number is required")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN card format (e.g. ABCDE1234F)")
    private String panNumber;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid 10-digit Indian mobile number")
    private String phoneNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.0", message = "Minimum loan amount is 1,000")
    private BigDecimal requestedAmount;

    public LoanRequestDTO() {
    }

    public LoanRequestDTO(String customerId, String applicantName, String panNumber, String phoneNumber, BigDecimal requestedAmount) {
        this.customerId = customerId;
        this.applicantName = applicantName;
        this.panNumber = panNumber;
        this.phoneNumber = phoneNumber;
        this.requestedAmount = requestedAmount;
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
}