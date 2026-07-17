package com.example.ems.employee.dto;

import java.util.List;

public class DepartmentUpdateRequest {
    private String name;
    private String code;
    private String headId;
    private String description;
    private String parentDepartmentId;
    private List<TeamUpdateInput> teams;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getHeadId() {
        return headId;
    }

    public void setHeadId(String headId) {
        this.headId = headId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentDepartmentId() {
        return parentDepartmentId;
    }

    public void setParentDepartmentId(String parentDepartmentId) {
        this.parentDepartmentId = parentDepartmentId;
    }

    public List<TeamUpdateInput> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamUpdateInput> teams) {
        this.teams = teams;
    }

    public static class TeamUpdateInput {
        private String id;
        private String name;
        private String leadId;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLeadId() {
            return leadId;
        }

        public void setLeadId(String leadId) {
            this.leadId = leadId;
        }
    }
}
