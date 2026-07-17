package com.example.ems.employee.dto;

import java.util.List;

public class DepartmentCreateRequest {
    private String name;
    private String code;
    private String head;
    private String parentDepartment;
    private String description;
    private List<TeamInput> teams;

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

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getParentDepartment() {
        return parentDepartment;
    }

    public void setParentDepartment(String parentDepartment) {
        this.parentDepartment = parentDepartment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TeamInput> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamInput> teams) {
        this.teams = teams;
    }

    public static class TeamInput {
        private String name;
        private String lead;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLead() {
            return lead;
        }

        public void setLead(String lead) {
            this.lead = lead;
        }
    }
}
