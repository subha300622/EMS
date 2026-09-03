package com.example.ems.payroll.validation;

import java.util.LinkedHashSet;
import java.util.Set;

public class SalaryDependencyNode {

    private Long componentId;
    private String componentCode;
    private String componentName;

    // Component IDs that THIS component depends on (incoming dependencies)
    private Set<Long> dependencies = new LinkedHashSet<>();

    // Component IDs that depend on THIS component (outgoing dependents)
    private Set<Long> dependents = new LinkedHashSet<>();

    public SalaryDependencyNode() {}

    public SalaryDependencyNode(Long componentId, String componentCode, String componentName) {
        this.componentId = componentId;
        this.componentCode = componentCode;
        this.componentName = componentName;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public String getComponentCode() {
        return componentCode;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Set<Long> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<Long> dependencies) {
        this.dependencies = dependencies;
    }

    public Set<Long> getDependents() {
        return dependents;
    }

    public void setDependents(Set<Long> dependents) {
        this.dependents = dependents;
    }
}
