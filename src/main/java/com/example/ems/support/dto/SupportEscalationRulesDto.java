package com.example.ems.support.dto;

import java.util.List;

public class SupportEscalationRulesDto {

    private List<EscalationRuleItem> rules;

    public SupportEscalationRulesDto() {}

    public SupportEscalationRulesDto(List<EscalationRuleItem> rules) {
        this.rules = rules;
    }

    public static class EscalationRuleItem {
        private Integer level;
        private Integer triggerAfterMinutes;
        private String action;

        public EscalationRuleItem() {}

        public EscalationRuleItem(Integer level, Integer triggerAfterMinutes, String action) {
            this.level = level;
            this.triggerAfterMinutes = triggerAfterMinutes;
            this.action = action;
        }

        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }

        public Integer getTriggerAfterMinutes() { return triggerAfterMinutes; }
        public void setTriggerAfterMinutes(Integer triggerAfterMinutes) { this.triggerAfterMinutes = triggerAfterMinutes; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }

    public List<EscalationRuleItem> getRules() { return rules; }
    public void setRules(List<EscalationRuleItem> rules) { this.rules = rules; }
}
