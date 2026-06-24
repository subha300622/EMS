package com.example.ems.finance.dto;

import java.util.Map;

public class FinanceCommandEnvelope {
    private String commandType;
    private String action;
    private Integer version;
    private Map<String, Object> payload;
    private Metadata metadata;

    public FinanceCommandEnvelope() {}

    public String getCommandType() { return commandType; }
    public void setCommandType(String commandType) { this.commandType = commandType; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }

    public static class Metadata {
        private String idempotencyKey;
        private String source;

        public Metadata() {}

        public String getIdempotencyKey() { return idempotencyKey; }
        public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
}
