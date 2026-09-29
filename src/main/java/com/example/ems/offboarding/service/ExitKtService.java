package com.example.ems.offboarding.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.ExitKtPlanResponse;
import com.example.ems.offboarding.entity.*;
import com.example.ems.offboarding.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExitKtService {

    @Autowired
    private ExitKtPlanRepository ktPlanRepository;

    @Autowired
    private ExitKtProjectRepository projectRepository;

    @Autowired
    private ExitKtContactRepository contactRepository;

    @Autowired
    private ExitKtSystemAccessRepository systemAccessRepository;

    @Autowired
    private ExitKtTaskRepository taskRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public ExitKtPlan getOrCreateKTPlan(Long employeeId) {
        Optional<ExitKtPlan> planOpt = ktPlanRepository.findByEmployeeId(employeeId);
        if (planOpt.isPresent()) {
            return planOpt.get();
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        ExitKtPlan plan = new ExitKtPlan();
        plan.setEmployee(employee);
        plan.setStatus("IN_PROGRESS");
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        plan = ktPlanRepository.save(plan);

        seedDefaultKTData(plan);

        return plan;
    }

    private void seedDefaultKTData(ExitKtPlan plan) {
        // Seed default Project
        ExitKtProject project = new ExitKtProject();
        project.setKtPlan(plan);
        project.setProjectName("Financial Forecasting Revamp");
        project.setStatus("IN_PROGRESS");
        project.setRiskLevel("MEDIUM");
        project.setHandoverNotes("Model documentation in progress");
        projectRepository.save(project);
        plan.getProjects().add(project);

        // Seed default Contact
        ExitKtContact contact = new ExitKtContact();
        contact.setKtPlan(plan);
        contact.setName("Anita Sharma");
        contact.setRole("Finance Manager");
        contact.setEmail("anita@company.com");
        contact.setResponsibility("Budget approvals");
        contactRepository.save(contact);
        plan.getContacts().add(contact);

        // Seed default System Access
        ExitKtSystemAccess sys = new ExitKtSystemAccess();
        sys.setKtPlan(plan);
        sys.setSystemName("SAP Finance");
        sys.setAccessType("READ_ONLY");
        sys.setStatus("ACTIVE");
        sys.setHandoverStatus("PENDING");
        systemAccessRepository.save(sys);
        plan.getSystemAccesses().add(sys);

        // Seed default Task
        ExitKtTask task = new ExitKtTask();
        task.setKtPlan(plan);
        task.setTaskName("Complete KT documentation");
        task.setStatus("PENDING");
        task.setDueDate(LocalDate.now().plusDays(10));
        taskRepository.save(task);
        plan.getTasks().add(task);
    }

    @Transactional
    public ExitKtProject updateProject(Long employeeId, Long projectId, String notes, String status, String riskLevel) {
        ExitKtPlan plan = getOrCreateKTPlan(employeeId);
        ExitKtProject proj = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        if (!proj.getKtPlan().getId().equals(plan.getId())) {
            throw new IllegalArgumentException("Project does not belong to this employee's KT plan.");
        }

        if (notes != null) {
            proj.setHandoverNotes(notes);
        }
        if (status != null) {
            proj.setStatus(status);
        }
        if (riskLevel != null) {
            proj.setRiskLevel(riskLevel);
        }

        return projectRepository.save(proj);
    }

    @Transactional
    public ExitKtContact updateContact(Long employeeId, Long contactId, String responsibility) {
        ExitKtPlan plan = getOrCreateKTPlan(employeeId);
        ExitKtContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found with ID: " + contactId));

        if (!contact.getKtPlan().getId().equals(plan.getId())) {
            throw new IllegalArgumentException("Contact does not belong to this employee's KT plan.");
        }

        if (responsibility != null) {
            contact.setResponsibility(responsibility);
        }

        return contactRepository.save(contact);
    }

    @Transactional
    public ExitKtSystemAccess updateSystemAccess(Long employeeId, Long systemId, String status, String handoverStatus) {
        ExitKtPlan plan = getOrCreateKTPlan(employeeId);
        ExitKtSystemAccess sys = systemAccessRepository.findById(systemId)
                .orElseThrow(() -> new IllegalArgumentException("System access record not found with ID: " + systemId));

        if (!sys.getKtPlan().getId().equals(plan.getId())) {
            throw new IllegalArgumentException("System access record does not belong to this employee's KT plan.");
        }

        if (status != null) {
            sys.setStatus(status);
        }
        if (handoverStatus != null) {
            sys.setHandoverStatus(handoverStatus);
        }

        return systemAccessRepository.save(sys);
    }

    @Transactional
    public ExitKtPlan assignHandover(Long employeeId, Long handoverPersonId) {
        ExitKtPlan plan = getOrCreateKTPlan(employeeId);
        Employee handoverPerson = employeeRepository.findById(handoverPersonId)
                .orElseThrow(() -> new IllegalArgumentException("Handover person not found with ID: " + handoverPersonId));

        plan.setHandoverPerson(handoverPerson);
        plan.setUpdatedAt(LocalDateTime.now());
        return ktPlanRepository.save(plan);
    }

    @Transactional
    public ExitKtPlan completeSection(Long employeeId, String sectionName) {
        ExitKtPlan plan = getOrCreateKTPlan(employeeId);

        if ("PROJECTS".equalsIgnoreCase(sectionName)) {
            for (ExitKtProject p : plan.getProjects()) {
                if (p.getHandoverNotes() == null || p.getHandoverNotes().trim().isEmpty()) {
                    throw new IllegalArgumentException("Cannot complete Projects section: Handover notes are required for all projects.");
                }
            }
            plan.setProjectsCompleted(true);
        } else if ("SYSTEM_CREDENTIALS".equalsIgnoreCase(sectionName)) {
            for (ExitKtSystemAccess s : plan.getSystemAccesses()) {
                if ("PENDING".equalsIgnoreCase(s.getHandoverStatus())) {
                    throw new IllegalArgumentException("Cannot complete System Credentials section: Handover status is pending for " + s.getSystemName());
                }
            }
            plan.setSystemCredentialsCompleted(true);
        } else if ("TASKS".equalsIgnoreCase(sectionName) || "PENDING_TASKS".equalsIgnoreCase(sectionName)) {
            plan.setPendingTasksCompleted(true);
        } else if ("CONTACTS".equalsIgnoreCase(sectionName) || "KEY_CONTACTS".equalsIgnoreCase(sectionName)) {
            plan.setContactsCompleted(true);
        } else {
            throw new IllegalArgumentException("Invalid section: " + sectionName);
        }

        plan.setUpdatedAt(LocalDateTime.now());
        return ktPlanRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public ExitKtPlanResponse mapToResponse(ExitKtPlan plan) {
        ExitKtPlanResponse resp = new ExitKtPlanResponse();
        resp.setEmployeeId(plan.getEmployee().getId());
        resp.setEmployeeName(plan.getEmployee().getFullName());
        resp.setRole(plan.getEmployee().getDesignation());

        resp.setProjects(plan.getProjects().stream().map(p -> {
            ExitKtPlanResponse.ProjectDto dto = new ExitKtPlanResponse.ProjectDto();
            dto.setProjectId(p.getId());
            dto.setProjectName(p.getProjectName());
            dto.setStatus(p.getStatus());
            dto.setHandoverNotes(p.getHandoverNotes());
            dto.setRiskLevel(p.getRiskLevel());
            return dto;
        }).collect(Collectors.toList()));

        resp.setKeyContacts(plan.getContacts().stream().map(c -> {
            ExitKtPlanResponse.ContactDto dto = new ExitKtPlanResponse.ContactDto();
            dto.setContactId(c.getId());
            dto.setName(c.getName());
            dto.setRole(c.getRole());
            dto.setEmail(c.getEmail());
            dto.setResponsibility(c.getResponsibility());
            return dto;
        }).collect(Collectors.toList()));

        resp.setSystemCredentials(plan.getSystemAccesses().stream().map(s -> {
            ExitKtPlanResponse.SystemCredentialDto dto = new ExitKtPlanResponse.SystemCredentialDto();
            dto.setSystemId(s.getId());
            dto.setSystemName(s.getSystemName());
            dto.setAccessType(s.getAccessType());
            dto.setStatus(s.getStatus());
            dto.setHandoverStatus(s.getHandoverStatus());
            return dto;
        }).collect(Collectors.toList()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        resp.setPendingTasks(plan.getTasks().stream().map(t -> {
            ExitKtPlanResponse.TaskDto dto = new ExitKtPlanResponse.TaskDto();
            dto.setTaskId(t.getId());
            dto.setTaskName(t.getTaskName());
            dto.setStatus(t.getStatus());
            dto.setDueDate(t.getDueDate() != null ? t.getDueDate().format(formatter) : null);
            return dto;
        }).collect(Collectors.toList()));

        if (plan.getHandoverPerson() != null) {
            ExitKtPlanResponse.HandoverPersonDto dto = new ExitKtPlanResponse.HandoverPersonDto();
            dto.setEmployeeId(plan.getHandoverPerson().getId());
            dto.setName(plan.getHandoverPerson().getFullName());
            dto.setRole(plan.getHandoverPerson().getDesignation());
            resp.setHandoverPerson(dto);
        }

        return resp;
    }
}
