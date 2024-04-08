package org.lamisplus.modules.biometric.enumeration;

public enum MatchTypes {
    ImperfectMatch("Imperfect match"), PerfectMatch("Perfect match"), NoMatch("No match");
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





