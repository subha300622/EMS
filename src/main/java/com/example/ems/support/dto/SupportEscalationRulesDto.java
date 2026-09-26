package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class SupportEscalationRulesDto {

    @Schema(description = "List of tiered escalation rules")
    private List<EscalationRuleItem> rules;

    public SupportEscalationRulesDto() {}

    public SupportEscalationRulesDto(List<EscalationRuleItem> rules) {
        this.rules = rules;
    }

    public static class EscalationRuleItem {
        @Schema(description = "Escalation tier level", example = "1")
        private Integer level;

        @Schema(description = "Trigger threshold in minutes after overdue", example = "30")
        private Integer triggerAfterMinutes;

        @Schema(description = "Escalation action taken", example = "NOTIFY_MANAGER")
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
