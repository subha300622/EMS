package com.example.ems.employee.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

public class OrgChartNodeDto {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "string")
    private String employeeId;
    @Schema(example = "John Doe")
    private String fullName;
    @Schema(example = "Software Engineer")
    private String designation;
    @Schema(example = "john.doe@example.com")
    private String email;
    @Schema(example = "string")
    private String profileImage;
    @Schema(example = "Engineering")
    private String department;
    private List<OrgChartNodeDto> children = new ArrayList<>();

    public OrgChartNodeDto() {}

    public OrgChartNodeDto(Long id, String employeeId, String fullName, String designation, String email, String profileImage, String department) {
        this.id = id;
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.designation = designation;
        this.email = email;
        this.profileImage = profileImage;
        this.department = department;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public List<OrgChartNodeDto> getChildren() { return children; }
    public void setChildren(List<OrgChartNodeDto> children) { this.children = children; }

    public void addChild(OrgChartNodeDto child) {
        this.children.add(child);
    }
}
