package org.lamisplus.modules.biometric.enumeration;

public enum MatchTypes {
    ImperfectMatch("IMPERFECT MATCH"), PerfectMatch("PERFECT MATCH"), NoMatch("NO MATCH");
    private String matchType;

    MatchTypes(String matchType)
    {
        this.matchType=matchType;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }
}





