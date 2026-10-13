package com.loan.risk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loanReferenceId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private Integer creditScore;

    @Column(nullable = false)
    private BigDecimal requestedAmount;

    @Column(nullable = false)
    private String riskStatus; // APPROVED, REJECTED, MANUAL_REVIEW

    private String remarks;

    private LocalDateTime evaluatedAt;

    @PrePersist
    public void prePersist() {
        this.evaluatedAt = LocalDateTime.now();
    }
}