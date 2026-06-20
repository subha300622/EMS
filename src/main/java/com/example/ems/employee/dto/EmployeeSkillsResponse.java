package com.example.ems.employee.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class EmployeeSkillsResponse {
    @Schema(example = "1")
    private Long employeeId;
    private List<SkillDto> skills;

    public EmployeeSkillsResponse() {}

    public EmployeeSkillsResponse(Long employeeId, List<SkillDto> skills) {
        this.employeeId = employeeId;
        this.skills = skills;
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public List<SkillDto> getSkills() { return skills; }
    public void setSkills(List<SkillDto> skills) { this.skills = skills; }

    public static class SkillDto {
        @Schema(example = "string")
        private String name;
        @Schema(example = "string")
        private String level;
        @Schema(example = "2026")
        private Integer experienceYears;

        public SkillDto() {}

        public SkillDto(String name, String level, Integer experienceYears) {
            this.name = name;
            this.level = level;
            this.experienceYears = experienceYears;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }

        public Integer getExperienceYears() { return experienceYears; }
        public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    }
}
