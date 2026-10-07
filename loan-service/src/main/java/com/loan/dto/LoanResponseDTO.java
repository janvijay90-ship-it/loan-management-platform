package com.loan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.loan.enums.LoanStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanResponseDTO {
	private Long id; 
    private String loanReferenceId;
    private String customerId;
    private BigDecimal requestedAmount;
    private Integer creditScore;
    private LoanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
