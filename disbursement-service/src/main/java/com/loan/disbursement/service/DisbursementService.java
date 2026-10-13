package com.loan.disbursement.service;

import com.loan.disbursement.dto.LoanApprovedEvent;
import com.loan.disbursement.entity.DisbursementRecord;

public interface DisbursementService {
    DisbursementRecord processDisbursement(LoanApprovedEvent event);
}
