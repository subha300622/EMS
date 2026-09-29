package com.example.ems.appraisal.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IncrementPolicyDto {

    private Long id;
    private String name = "Standard Increment Policy";
    private Boolean active = true;
    private LocalDate effectiveFrom;
    private List<IncrementRuleDto> rules = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public List<IncrementRuleDto> getRules() { return rules; }
    public void setRules(List<IncrementRuleDto> rules) { this.rules = rules; }
}
