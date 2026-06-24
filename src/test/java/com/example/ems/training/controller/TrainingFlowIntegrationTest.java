package com.example.ems.training.controller;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.training.dto.*;
import com.example.ems.training.entity.Certification;
import com.example.ems.training.entity.TrainingCourse;
import com.example.ems.training.entity.TrainingProgress;
import com.example.ems.training.entity.TrainingStatus;
import com.example.ems.training.repository.CertificationRepository;
import com.example.ems.training.repository.TrainingProgressRepository;
import com.example.ems.training.service.TrainingAssignmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TrainingFlowIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TrainingAssignmentService assignmentService;

    @Autowired
    private TrainingProgressRepository progressRepository;

    @Autowired
    private CertificationRepository certificationRepository;

    @Test
    public void testFullLmsProductionFlow() {
        // Setup manager and employee relationship
        Employee manager = new Employee();
        manager.setFullName("Ravi Manager");
        manager.setEmail("ravi.manager@company.com");
        manager.setEmployeeId("MGR901");
        manager = employeeRepository.save(manager);

        Employee employee = new Employee();
        employee.setFullName("Arjun Mehta");
        employee.setEmail("arjun.mehta@company.com");
        employee.setEmployeeId("EMP901");
        employee.setManager(manager);
        employee.setDesignation("Backend Dev");
        employee = employeeRepository.save(employee);

        // 1. HR/Manager creates Course in Training Catalog
        TrainingCatalogRequest catalogReq = new TrainingCatalogRequest();
        catalogReq.setName("AWS Cloud Architect");
        catalogReq.setDescription("AWS fundamentals to advanced architecture");
        catalogReq.setDurationHours(12);
        catalogReq.setCategory("CLOUD");
        catalogReq.setDifficulty("INTERMEDIATE");
        catalogReq.setIsMandatory(true);
        catalogReq.setCreatedBy(manager.getId());

        TrainingCourse course = assignmentService.createCourse(catalogReq);

        // 2. Verify Course is stored in training_courses table with ACTIVE status
        assertNotNull(course.getId());
        assertEquals("AWS Cloud Architect", course.getTitle());
        assertEquals("ACTIVE", course.getStatus());

        // 3. Manager assigns course to the Employee
        TrainingAssignRequest assignReq = new TrainingAssignRequest();
        assignReq.setCourseId(course.getId());
        assignReq.setAssignedToEmployeeIds(List.of(employee.getId()));
        assignReq.setDueDate(LocalDate.now().plusDays(10));
        assignReq.setPriority("HIGH");
        assignReq.setAssignedBy(manager.getId());

        Map<String, Object> assignResp = assignmentService.assignTraining(assignReq);

        // 4. Verify training_assignment is created & Employee is linked via TrainingProgress
        assertNotNull(assignResp.get("assignmentId"));
        assertEquals("Training assigned successfully", assignResp.get("message"));
        assertEquals(1, assignResp.get("assignedCount"));

        Long assignmentId = (Long) assignResp.get("assignmentId");
        List<TrainingProgress> progressList = progressRepository.findByAssignmentId(assignmentId);
        assertEquals(1, progressList.size());
        TrainingProgress progress = progressList.get(0);
        assertEquals(employee.getId(), progress.getEmployee().getId());
        assertEquals(TrainingStatus.ASSIGNED, progress.getStatus());
        assertEquals(0, progress.getProgressPercent());

        // 5. Employee starts learning and progress is updated (IN_PROGRESS)
        TrainingProgressUpdateRequest progressReq = new TrainingProgressUpdateRequest();
        progressReq.setEmployeeId(employee.getId());
        progressReq.setProgress(40);
        progressReq.setStatus("IN_PROGRESS");

        Map<String, Object> progressResp = assignmentService.updateProgress(assignmentId, progressReq);
        assertEquals("Progress updated successfully", progressResp.get("message"));
        assertEquals(40, progressResp.get("currentProgress"));
        assertEquals("IN_PROGRESS", progressResp.get("status"));

        // 6. System evaluates completion & marks COMPLETED & issues Certification
        Map<String, Object> completeResp = assignmentService.completeTraining(assignmentId, employee.getId());
        assertEquals("Training completed successfully", completeResp.get("message"));
        assertEquals("COMPLETED", completeResp.get("status"));

        // Verify state is updated to COMPLETED / 100% progress
        TrainingProgress completedProgress = progressRepository.findById(progress.getId()).orElseThrow();
        assertEquals(TrainingStatus.COMPLETED, completedProgress.getStatus());
        assertEquals(100, completedProgress.getProgressPercent());
        assertNotNull(completedProgress.getCompletionDate());

        // Verify Certification is issued and saved in the database
        List<Certification> certs = certificationRepository.findByEmployeeId(employee.getId());
        assertEquals(1, certs.size());
        Certification cert = certs.get(0);
        assertEquals(course.getId(), cert.getCourse().getId());
        assertNotNull(cert.getCertificateNumber());
        assertTrue(cert.getCertificateNumber().startsWith("CERT-"));

        // 7. Dashboard aggregates team stats for the Manager
        TeamSummaryResponse summary = assignmentService.getTeamSummary(manager.getId());
        assertEquals(1, summary.getTotalEmployees());
        assertEquals(1, summary.getTotalAssigned());
        assertEquals(1, summary.getCompleted());
        assertEquals(0, summary.getInProgress());
        assertEquals(0, summary.getOverdue());
        assertEquals(100.0, summary.getComplianceRate());

        Map<String, Object> teamProgress = assignmentService.getTeamProgressList(manager.getId());
        List<Map<String, Object>> content = (List<Map<String, Object>>) teamProgress.get("content");
        assertEquals(1, content.size());
        assertEquals("Arjun Mehta", content.get(0).get("employeeName"));
        assertEquals(1, content.get(0).get("completed"));
        assertEquals(false, content.get(0).get("risk"));

        List<TeamRiskResponse> teamRisk = assignmentService.getTeamRiskList(manager.getId());
        assertTrue(teamRisk.isEmpty()); // Dev is completed on time, so no risk
    }
}
