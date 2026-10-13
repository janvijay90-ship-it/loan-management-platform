package com.loan.risk.dto;

public class CibilScoreResult {

    private String panNumber;
    private Integer cibilScore;
    private String bureauReferenceNumber;
    private boolean cached;

    public CibilScoreResult() {
    }

    public CibilScoreResult(String panNumber, Integer cibilScore, String bureauReferenceNumber, boolean cached) {
        this.panNumber = panNumber;
        this.cibilScore = cibilScore;
        this.bureauReferenceNumber = bureauReferenceNumber;
        this.cached = cached;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
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

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }
}
