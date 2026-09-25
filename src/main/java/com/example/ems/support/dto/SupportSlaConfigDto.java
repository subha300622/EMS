package com.example.ems.support.dto;

import java.util.List;

public class SupportSlaConfigDto {

    private boolean enabled = true;
    private List<SlaRuleItem> rules;

    public SupportSlaConfigDto() {}

    public SupportSlaConfigDto(boolean enabled, List<SlaRuleItem> rules) {
        this.enabled = enabled;
        this.rules = rules;
    }

    public static class SlaRuleItem {
        private String priority;
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
