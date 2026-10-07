package com.loan.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanRequestDTO {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.0", message = "Minimum loan amount is 1,000")
    private BigDecimal requestedAmount;

    @NotNull(message = "Credit score is required")
    @Min(value = 300, message = "Invalid credit score")
    @Max(value = 850, message = "Invalid credit score")
    private Integer creditScore;
}