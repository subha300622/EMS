package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class SupportSlaConfigDto {

    @Schema(description = "Whether SLA evaluation is enabled", example = "true")
    private boolean enabled = true;

    @Schema(description = "List of SLA rules by priority")
    private List<SlaRuleItem> rules;

    public SupportSlaConfigDto() {}

    public SupportSlaConfigDto(boolean enabled, List<SlaRuleItem> rules) {
        this.enabled = enabled;
        this.rules = rules;
    }

    public static class SlaRuleItem {
        @Schema(description = "Ticket priority", example = "CRITICAL")
        private String priority;

        @Schema(description = "SLA resolution deadline in hours", example = "2")
        private Integer slaHours;

        public SlaRuleItem() {}
        public SlaRuleItem(String priority, Integer slaHours) {
            this.priority = priority;
            this.slaHours = slaHours;
        }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public Integer getSlaHours() { return slaHours; }
        public void setSlaHours(Integer slaHours) { this.slaHours = slaHours; }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public List<SlaRuleItem> getRules() { return rules; }
    public void setRules(List<SlaRuleItem> rules) { this.rules = rules; }
}
