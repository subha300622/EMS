package com.example.ems.security.dto;

public class AuthDecisionTrace {
    private final String userId;
    private final String sessionId;
    private final long epoch;
    private final int version;
    private final AuthenticationOutcome decision;
    private final String reason;
    private final long latencyMs;

    public AuthDecisionTrace(String userId, String sessionId, long epoch, int version,
                             AuthenticationOutcome decision, String reason, long latencyMs) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.epoch = epoch;
        this.version = version;
        this.decision = decision;
        this.reason = reason;
        this.latencyMs = latencyMs;
    }

    public String getUserId() { return userId; }
    public String getSessionId() { return sessionId; }
    public long getEpoch() { return epoch; }
    public int getVersion() { return version; }
    public AuthenticationOutcome getDecision() { return decision; }
    public String getReason() { return reason; }
    public long getLatencyMs() { return latencyMs; }

    @Override
    public String toString() {
        return "AuthDecisionTrace{" +
                "userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", epoch=" + epoch +
                ", version=" + version +
                ", decision=" + decision +
                ", reason='" + reason + '\'' +
                ", latencyMs=" + latencyMs +
                '}';
    }
}
