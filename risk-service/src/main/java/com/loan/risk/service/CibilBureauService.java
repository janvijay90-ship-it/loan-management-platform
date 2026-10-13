package com.loan.risk.service;

import com.loan.risk.dto.CibilScoreResult;

public interface CibilBureauService {
    CibilScoreResult fetchCibilScore(String panNumber, String applicantName, String phoneNumber);
}
