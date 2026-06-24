package com.example.ems.training.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.training.dto.*;
import com.example.ems.training.entity.*;
import com.example.ems.training.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrainingAssignmentService {

    @Autowired
    private TrainingCourseRepository courseRepository;

    @Autowired
    private TrainingAssignmentRepository assignmentRepository;

    @Autowired
    private TrainingProgressRepository progressRepository;

    @Autowired
    private CertificationRepository certificationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    // ── A. Catalog Service ───────────────────────────────────────────────────
    @Transactional
    public TrainingCourse createCourse(TrainingCatalogRequest request) {
        TrainingCourse course = new TrainingCourse();
        course.setTitle(request.getName());
        course.setDescription(request.getDescription());
        course.setDurationHours(request.getDurationHours());
        course.setCategory(request.getCategory() != null ? request.getCategory().toUpperCase() : null);
        course.setDifficulty(request.getDifficulty() != null ? request.getDifficulty().toUpperCase() : null);
        course.setIsMandatory(request.getIsMandatory() != null ? request.getIsMandatory() : false);
        course.setCreatedBy(request.getCreatedBy());
        course.setStatus("ACTIVE");
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        return courseRepository.save(course);
    }

    public List<TrainingCourse> getCourses() {
        return courseRepository.findAll();
    }

    public Optional<TrainingCourse> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    // ── B. Assignment Service ────────────────────────────────────────────────
    @Transactional
    public Map<String, Object> assignTraining(TrainingAssignRequest request) {
        TrainingCourse course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + request.getCourseId()));

        Set<Employee> employeesToAssign = new LinkedHashSet<>();

        // Resolve by department
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId()).orElse(null);
            if (dept != null) {
                List<Employee> deptEmps = employeeRepository.findByDepartment(dept.getName());
                employeesToAssign.addAll(deptEmps);
            }
        }

        // Resolve by employee list
        if (request.getAssignedToEmployeeIds() != null) {
            for (Long empId : request.getAssignedToEmployeeIds()) {
                // Try find by database ID (primary key)
                Employee emp = employeeRepository.findById(empId).orElse(null);
                if (emp == null) {
                    // Fallback: try by business employeeId
                    emp = employeeRepository.findByEmployeeId(String.valueOf(empId)).orElse(null);
                }
                if (emp != null) {
                    employeesToAssign.add(emp);
                }
            }
        }

        if (employeesToAssign.isEmpty()) {
            throw new IllegalArgumentException("No valid employees resolved for assignment");
        }

        TrainingAssignment assignment = new TrainingAssignment();
        assignment.setCourse(course);
        assignment.setDepartmentId(request.getDepartmentId());
        assignment.setAssignedBy(request.getAssignedBy());
        assignment.setDueDate(request.getDueDate());
        assignment.setPriority(request.getPriority() != null ? request.getPriority().toUpperCase() : "MEDIUM");
        assignment.setNote(request.getNote());
        assignment.setCreatedAt(LocalDateTime.now());

        TrainingAssignment savedAssignment = assignmentRepository.save(assignment);

        List<TrainingProgress> progressList = new ArrayList<>();
        for (Employee emp : employeesToAssign) {
            TrainingProgress progress = new TrainingProgress();
            progress.setEmployee(emp);
            progress.setAssignment(savedAssignment);
            progress.setProgressPercent(0);
            progress.setStatus(TrainingStatus.ASSIGNED);
            progress.setUpdatedAt(LocalDateTime.now());
            progressList.add(progressRepository.save(progress));
        }

        savedAssignment.setProgressList(progressList);
        assignmentRepository.save(savedAssignment);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("assignmentId", savedAssignment.getId());
        response.put("message", "Training assigned successfully");
        response.put("assignedCount", employeesToAssign.size());
        response.put("status", "ASSIGNED");
        response.put("assignedOn", LocalDate.now().toString());

        return response;
    }

    public List<TrainingAssignment> getAssignments() {
        return assignmentRepository.findAll();
    }

    public Optional<Map<String, Object>> getAssignmentDetails(Long id) {
        return assignmentRepository.findById(id).map(assignment -> {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("assignmentId", assignment.getId());
            
            Map<String, Object> courseMap = new LinkedHashMap<>();
            courseMap.put("id", assignment.getCourse().getId());
            courseMap.put("name", assignment.getCourse().getTitle());
            courseMap.put("durationHours", assignment.getCourse().getDurationHours());
            resp.put("course", courseMap);

            List<Map<String, Object>> assignedToList = assignment.getProgressList().stream().map(tp -> {
                Map<String, Object> empMap = new LinkedHashMap<>();
                empMap.put("employeeId", tp.getEmployee().getId());
                empMap.put("name", tp.getEmployee().getFullName());
                return empMap;
            }).collect(Collectors.toList());
            resp.put("assignedTo", assignedToList);

            resp.put("dueDate", assignment.getDueDate() != null ? assignment.getDueDate().toString() : null);
            resp.put("priority", assignment.getPriority());

            // Compute overall status and progress
            List<TrainingProgress> list = assignment.getProgressList();
            int avgProgress = 0;
            String computedStatus = "ASSIGNED";
            if (!list.isEmpty()) {
                avgProgress = (int) list.stream().mapToInt(TrainingProgress::getProgressPercent).average().orElse(0.0);
                boolean anyInProgress = list.stream().anyMatch(t -> t.getStatus() == TrainingStatus.IN_PROGRESS);
                boolean allCompleted = list.stream().allMatch(t -> t.getStatus() == TrainingStatus.COMPLETED || t.getStatus() == TrainingStatus.CERTIFIED);
                if (allCompleted) {
                    computedStatus = "COMPLETED";
                } else if (anyInProgress || avgProgress > 0) {
                    computedStatus = "IN_PROGRESS";
                }
            }
            resp.put("status", computedStatus);
            resp.put("progress", avgProgress);
            resp.put("note", assignment.getNote());

            return resp;
        });
    }

    // ── C. Employee Self-Service ─────────────────────────────────────────────
    public List<Map<String, Object>> getMyTrainings(String employeeEmail) {
        List<TrainingProgress> progressList = progressRepository.findByEmployeeEmail(employeeEmail);
        return progressList.stream().map(tp -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("assignmentId", tp.getAssignment().getId());
            m.put("courseName", tp.getAssignment().getCourse().getTitle());
            m.put("priority", tp.getAssignment().getPriority());
            m.put("dueDate", tp.getAssignment().getDueDate() != null ? tp.getAssignment().getDueDate().toString() : null);
            
            // Dynamic evaluation of OVERDUE status
            LocalDate due = tp.getAssignment().getDueDate();
            boolean overdue = due != null && due.isBefore(LocalDate.now()) 
                    && tp.getStatus() != TrainingStatus.COMPLETED 
                    && tp.getStatus() != TrainingStatus.CERTIFIED;
            m.put("status", overdue ? "OVERDUE" : tp.getStatus().name());
            
            m.put("progress", tp.getProgressPercent());

            // Resolve assigned by name
            String managerName = "HR / Manager";
            if (tp.getAssignment().getAssignedBy() != null) {
                managerName = employeeRepository.findById(tp.getAssignment().getAssignedBy())
                        .map(Employee::getFullName)
                        .orElse("HR / Manager");
            }
            m.put("assignedBy", managerName);

            return m;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> updateProgress(Long assignmentId, TrainingProgressUpdateRequest request) {
        TrainingProgress tp = progressRepository.findByAssignmentIdAndEmployeeId(assignmentId, request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Training progress record not found for assignment: " 
                        + assignmentId + " and employee: " + request.getEmployeeId()));

        tp.setProgressPercent(request.getProgress());
        
        if (request.getProgress() >= 100) {
            tp.setStatus(TrainingStatus.COMPLETED);
            tp.setCompletionDate(LocalDate.now());
            // Auto generate certificate
            generateCertification(tp.getEmployee(), tp.getAssignment().getCourse());
        } else if (request.getProgress() > 0 && tp.getStatus() == TrainingStatus.ASSIGNED) {
            tp.setStatus(TrainingStatus.IN_PROGRESS);
        } else if (request.getStatus() != null) {
            try {
                tp.setStatus(TrainingStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (Exception ignored) {}
        }
        
        tp.setUpdatedAt(LocalDateTime.now());
        TrainingProgress saved = progressRepository.save(tp);

        return Map.of(
                "message", "Progress updated successfully",
                "currentProgress", saved.getProgressPercent(),
                "status", saved.getStatus().name()
        );
    }

    @Transactional
    public Map<String, Object> completeTraining(Long assignmentId, Long employeeId) {
        TrainingProgress tp = progressRepository.findByAssignmentIdAndEmployeeId(assignmentId, employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Training progress record not found for assignment: " 
                        + assignmentId + " and employee: " + employeeId));

        tp.setProgressPercent(100);
        tp.setStatus(TrainingStatus.COMPLETED);
        tp.setCompletionDate(LocalDate.now());
        tp.setUpdatedAt(LocalDateTime.now());
        progressRepository.save(tp);

        generateCertification(tp.getEmployee(), tp.getAssignment().getCourse());

        return Map.of(
                "message", "Training completed successfully",
                "status", "COMPLETED",
                "completedDate", LocalDate.now().toString()
        );
    }

    @Transactional
    public void generateCertification(Employee employee, TrainingCourse course) {
        Optional<Certification> existing = certificationRepository.findByEmployeeIdAndCourseId(employee.getId(), course.getId());
        if (existing.isEmpty()) {
            Certification cert = new Certification();
            cert.setEmployee(employee);
            cert.setCourse(course);
            cert.setCertificateNumber("CERT-" + System.currentTimeMillis() + "-" + employee.getId());
            cert.setIssueDate(LocalDate.now());
            cert.setFileUrl("/api/v1/training/certificates/" + employee.getId() + "/download");
            certificationRepository.save(cert);
        }
    }

    // ── D. Team Service & Aggregations ───────────────────────────────────────
    public TeamSummaryResponse getTeamSummary(Long managerId) {
        List<Employee> directReports = employeeRepository.findByManagerId(managerId);
        if (directReports.isEmpty()) {
            return new TeamSummaryResponse(0, 0, 0, 0, 0, 100.0);
        }

        List<TrainingProgress> progressList = progressRepository.findByEmployeeManagerId(managerId);

        int totalAssigned = progressList.size();
        int completed = 0;
        int inProgress = 0;
        int overdue = 0;

        for (TrainingProgress tp : progressList) {
            LocalDate due = tp.getAssignment().getDueDate();
            boolean isOverdue = due != null && due.isBefore(LocalDate.now()) 
                    && tp.getStatus() != TrainingStatus.COMPLETED 
                    && tp.getStatus() != TrainingStatus.CERTIFIED;
            
            if (isOverdue) {
                overdue++;
            }

            if (tp.getStatus() == TrainingStatus.COMPLETED || tp.getStatus() == TrainingStatus.CERTIFIED) {
                completed++;
            } else if (tp.getStatus() == TrainingStatus.IN_PROGRESS) {
                inProgress++;
            }
        }

        double complianceRate = totalAssigned > 0 ? ((double) completed / totalAssigned) * 100.0 : 100.0;
        // round to two decimal places
        complianceRate = Math.round(complianceRate * 100.0) / 100.0;

        return new TeamSummaryResponse(
                directReports.size(),
                totalAssigned,
                completed,
                inProgress,
                overdue,
                complianceRate
        );
    }

    public Map<String, Object> getTeamProgressList(Long managerId) {
        List<Employee> directReports = employeeRepository.findByManagerId(managerId);
        List<Map<String, Object>> content = new ArrayList<>();

        for (Employee emp : directReports) {
            List<TrainingProgress> empProgress = progressRepository.findByEmployeeId(emp.getId());
            int totalAssigned = empProgress.size();
            int completed = 0;
            int overdue = 0;

            for (TrainingProgress tp : empProgress) {
                if (tp.getStatus() == TrainingStatus.COMPLETED || tp.getStatus() == TrainingStatus.CERTIFIED) {
                    completed++;
                }
                LocalDate due = tp.getAssignment().getDueDate();
                if (due != null && due.isBefore(LocalDate.now()) 
                        && tp.getStatus() != TrainingStatus.COMPLETED 
                        && tp.getStatus() != TrainingStatus.CERTIFIED) {
                    overdue++;
                }
            }

            // Flag employee as at risk if overdueCount > 0
            boolean risk = overdue > 0;

            Map<String, Object> empMap = new LinkedHashMap<>();
            empMap.put("employeeId", emp.getId());
            empMap.put("employeeName", emp.getFullName());
            empMap.put("designation", emp.getDesignation());
            empMap.put("totalAssigned", totalAssigned);
            empMap.put("completed", completed);
            empMap.put("overdue", overdue);
            empMap.put("risk", risk);

            content.add(empMap);
        }

        return Map.of("content", content);
    }

    public List<TeamRiskResponse> getTeamRiskList(Long managerId) {
        List<Employee> directReports = employeeRepository.findByManagerId(managerId);
        List<TeamRiskResponse> riskList = new ArrayList<>();

        for (Employee emp : directReports) {
            List<TrainingProgress> empProgress = progressRepository.findByEmployeeId(emp.getId());
            int totalAssigned = empProgress.size();
            if (totalAssigned == 0) continue;

            int completed = 0;
            int overdueCount = 0;
            boolean zeroProgressAndOverdue = false;

            for (TrainingProgress tp : empProgress) {
                if (tp.getStatus() == TrainingStatus.COMPLETED || tp.getStatus() == TrainingStatus.CERTIFIED) {
                    completed++;
                }
                LocalDate due = tp.getAssignment().getDueDate();
                boolean isOverdue = due != null && due.isBefore(LocalDate.now()) 
                        && tp.getStatus() != TrainingStatus.COMPLETED 
                        && tp.getStatus() != TrainingStatus.CERTIFIED;
                
                if (isOverdue) {
                    overdueCount++;
                    if (tp.getProgressPercent() == 0) {
                        zeroProgressAndOverdue = true;
                    }
                }
            }

            double completionRate = ((double) completed / totalAssigned) * 100.0;
            
            String riskLevel = null;
            if (zeroProgressAndOverdue) {
                riskLevel = "HIGH";
            } else if (overdueCount > 0) {
                riskLevel = "HIGH"; // matched to JSON example showing HIGH for Dev Patel with overdue
            } else if (completionRate < 50.0) {
                riskLevel = "MEDIUM";
            }

            if (riskLevel != null) {
                riskList.add(new TeamRiskResponse(
                        emp.getId(),
                        emp.getFullName(),
                        overdueCount,
                        riskLevel
                ));
            }
        }

        return riskList;
    }

    public EmployeeTrainingDetailResponse getEmployeeDetail(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        List<TrainingProgress> progressList = progressRepository.findByEmployeeId(employeeId);
        
        List<EmployeeTrainingDetailResponse.CourseProgressDto> courseList = progressList.stream().map(tp -> {
            LocalDate due = tp.getAssignment().getDueDate();
            boolean isOverdue = due != null && due.isBefore(LocalDate.now()) 
                    && tp.getStatus() != TrainingStatus.COMPLETED 
                    && tp.getStatus() != TrainingStatus.CERTIFIED;

            String statusStr = isOverdue ? "OVERDUE" : tp.getStatus().name();
            
            return new EmployeeTrainingDetailResponse.CourseProgressDto(
                    tp.getAssignment().getCourse().getTitle(),
                    statusStr,
                    tp.getProgressPercent(),
                    tp.getAssignment().getDueDate()
            );
        }).collect(Collectors.toList());

        List<Certification> certifications = certificationRepository.findByEmployeeId(employeeId);
        List<EmployeeTrainingDetailResponse.CertificationDto> certList = certifications.stream().map(c -> 
            new EmployeeTrainingDetailResponse.CertificationDto(
                    c.getCourse().getTitle(),
                    c.getIssueDate()
            )
        ).collect(Collectors.toList());

        // Overall risk determination
        boolean hasOverdue = courseList.stream().anyMatch(c -> "OVERDUE".equals(c.getStatus()));
        String overallStatus = hasOverdue ? "AT_RISK" : "ON_TRACK";

        return new EmployeeTrainingDetailResponse(
                emp.getId(),
                emp.getFullName(),
                courseList,
                certList,
                overallStatus
        );
    }
}
