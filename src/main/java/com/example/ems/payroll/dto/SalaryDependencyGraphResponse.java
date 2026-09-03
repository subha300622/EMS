package com.example.ems.payroll.dto;

import java.util.ArrayList;
import java.util.List;

public class SalaryDependencyGraphResponse {

    private Long structureId;
    private String structureCode;
    private List<ComponentDependencyItem> components = new ArrayList<>();
    private List<String> calculationOrder = new ArrayList<>();
    private boolean hasCycle;
    private String cyclePath;

    public SalaryDependencyGraphResponse() {}

    public SalaryDependencyGraphResponse(Long structureId, String structureCode) {
        this.structureId = structureId;
        this.structureCode = structureCode;
    }

    public static class ComponentDependencyItem {
        private Long componentId;
        private String componentCode;
        private String componentName;
        private String calculationType;
        private List<String> dependsOn = new ArrayList<>();

        public ComponentDependencyItem() {}

        public ComponentDependencyItem(Long componentId, String componentCode, String componentName, String calculationType, List<String> dependsOn) {
            this.componentId = componentId;
            this.componentCode = componentCode;
            this.componentName = componentName;
            this.calculationType = calculationType;
            this.dependsOn = dependsOn != null ? dependsOn : new ArrayList<>();
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

        public String getCalculationType() {
            return calculationType;
        }

        public void setCalculationType(String calculationType) {
            this.calculationType = calculationType;
        }

        public List<String> getDependsOn() {
            return dependsOn;
        }

        public void setDependsOn(List<String> dependsOn) {
            this.dependsOn = dependsOn;
        }
    }

    public Long getStructureId() {
        return structureId;
    }

    public void setStructureId(Long structureId) {
        this.structureId = structureId;
    }

    public String getStructureCode() {
        return structureCode;
    }

    public void setStructureCode(String structureCode) {
        this.structureCode = structureCode;
    }

    public List<ComponentDependencyItem> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentDependencyItem> components) {
        this.components = components;
    }

    public List<String> getCalculationOrder() {
        return calculationOrder;
    }

    public void setCalculationOrder(List<String> calculationOrder) {
        this.calculationOrder = calculationOrder;
    }

    public boolean isHasCycle() {
        return hasCycle;
    }

    public void setHasCycle(boolean hasCycle) {
        this.hasCycle = hasCycle;
    }

    public String getCyclePath() {
        return cyclePath;
    }

    public void setCyclePath(String cyclePath) {
        this.cyclePath = cyclePath;
    }
}
