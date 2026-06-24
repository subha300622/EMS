package com.example.ems.training.dto;

import java.time.LocalDate;
import java.util.List;

public class EmployeeTrainingDetailResponse {

    private Long employeeId;
    private String employeeName;
    private List<CourseProgressDto> courses;
    private List<CertificationDto> certifications;
    private String overallStatus;

    public EmployeeTrainingDetailResponse() {}

    public EmployeeTrainingDetailResponse(Long employeeId, String employeeName, List<CourseProgressDto> courses, List<CertificationDto> certifications, String overallStatus) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.courses = courses;
        this.certifications = certifications;
        this.overallStatus = overallStatus;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public List<CourseProgressDto> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseProgressDto> courses) {
        this.courses = courses;
    }

    public List<CertificationDto> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<CertificationDto> certifications) {
        this.certifications = certifications;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public static class CourseProgressDto {
        private String courseName;
        private String status;
        private Integer progress;
        private LocalDate dueDate;

        public CourseProgressDto() {}

        public CourseProgressDto(String courseName, String status, Integer progress, LocalDate dueDate) {
            this.courseName = courseName;
            this.status = status;
            this.progress = progress;
            this.dueDate = dueDate;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getProgress() {
            return progress;
        }

        public void setProgress(Integer progress) {
            this.progress = progress;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }
    }

    public static class CertificationDto {
        private String name;
        private LocalDate issuedDate;

        public CertificationDto() {}

        public CertificationDto(String name, LocalDate issuedDate) {
            this.name = name;
            this.issuedDate = issuedDate;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDate getIssuedDate() {
            return issuedDate;
        }

        public void setIssuedDate(LocalDate issuedDate) {
            this.issuedDate = issuedDate;
        }
    }
}
