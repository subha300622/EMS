package com.example.ems.onboarding.controller;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.onboarding.dto.*;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.entity.OnboardingTask;
import com.example.ems.onboarding.entity.OnboardingTemplate;
import com.example.ems.onboarding.repository.OnboardingDocumentRepository;
import com.example.ems.onboarding.repository.OnboardingRepository;
import com.example.ems.onboarding.repository.OnboardingTaskRepository;
import com.example.ems.onboarding.repository.OnboardingTemplateRepository;
import com.example.ems.onboarding.service.OnboardingWorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OnboardingWorkflowControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OnboardingRepository onboardingRepository;

    @Mock
    private OnboardingTaskRepository onboardingTaskRepository;

    @Mock
    private OnboardingDocumentRepository onboardingDocumentRepository;

    @Mock
    private OnboardingTemplateRepository templateRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private OnboardingWorkflowService service;

    @InjectMocks
    private OnboardingWorkflowController controller;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        org.springframework.test.util.ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "workflowService", service);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new com.example.ems.config.GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testGetStats() throws Exception {
        when(onboardingRepository.findAll()).thenReturn(Collections.emptyList());
        when(onboardingTaskRepository.findAll()).thenReturn(Collections.emptyList());
        when(onboardingDocumentRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/onboarding/stats")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeCount").value(0));
    }

    @Test
    public void testLaunchOnboardingDuplicateConflict() throws Exception {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeId("emp-001");
        employee.setEmail("priya.sharma@example.com");

        Onboarding activeOnboarding = new Onboarding();
        activeOnboarding.setId(2L);
        activeOnboarding.setStatus("PRE_JOINING");

        when(employeeRepository.findByEmployeeId("emp-001")).thenReturn(Optional.of(employee));
        when(onboardingRepository.findByEmployeeId(1L)).thenReturn(Optional.of(activeOnboarding));

        OnboardingLaunchRequest request = new OnboardingLaunchRequest();
        request.setEmployeeId("emp-001");
        request.setEmail("priya.sharma@example.com");
        request.setTemplateId("tpl-eng-001");

        mockMvc.perform(post("/api/v1/onboarding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Active onboarding already exists for this employee"));
    }

    @Test
    public void testLaunchOnboardingSuccess() throws Exception {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeId("emp-001");
        employee.setEmail("priya.sharma@example.com");

        OnboardingTemplate template = new OnboardingTemplate();
        template.setId(1L);
        template.setTemplateCode("tpl-eng-001");
        template.setSectionsJson(
                "[{\"name\":\"Company Process\",\"tasks\":[{\"name\":\"Issue Laptop\",\"dueDays\":2}]}]");
        template.setDocumentsJson("[{\"name\":\"PAN Card\",\"maxSize\":5}]");

        Onboarding savedOnb = new Onboarding();
        savedOnb.setId(2L);
        savedOnb.setEmployee(employee);
        savedOnb.setJoiningDate(LocalDate.of(2026, 8, 1));
        savedOnb.setStatus("PRE_JOINING");
        savedOnb.setAssignedTemplateId("tpl-eng-001");

        when(employeeRepository.findByEmployeeId("emp-001")).thenReturn(Optional.of(employee));
        when(onboardingRepository.findByEmployeeId(1L)).thenReturn(Optional.empty());
        when(templateRepository.findByTemplateCode("tpl-eng-001")).thenReturn(Optional.of(template));
        when(onboardingRepository.save(any(Onboarding.class))).thenReturn(savedOnb);

        OnboardingLaunchRequest request = new OnboardingLaunchRequest();
        request.setEmployeeId("emp-001");
        request.setEmployeeName("Priya Sharma");
        request.setEmail("priya.sharma@example.com");
        request.setTemplateId("tpl-eng-001");
        request.setJoiningDate(LocalDate.of(2026, 8, 1));

        mockMvc.perform(post("/api/v1/onboarding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.onboarding.id").value("onb-2"))
                .andExpect(jsonPath("$.data.tasksCreated").value(1))
                .andExpect(jsonPath("$.data.documentsCreated").value(1));
    }

    @Test
    public void testAssignOrReplaceTemplatePreservation() throws Exception {
        Onboarding onboarding = new Onboarding();
        onboarding.setId(1L);
        onboarding.setStatus("PRE_JOINING");
        onboarding.setJoiningDate(LocalDate.of(2026, 8, 1));

        OnboardingTemplate newTemplate = new OnboardingTemplate();
        newTemplate.setId(2L);
        newTemplate.setTemplateCode("tpl-eng-001");
        newTemplate.setSectionsJson(
                "[{\"name\":\"Company Process\",\"tasks\":[{\"name\":\"Task A\",\"dueDays\":1},{\"name\":\"Task B\",\"dueDays\":2}]}]");
        newTemplate.setDocumentsJson("[]");

        OnboardingTask completedTask = new OnboardingTask();
        completedTask.setId(10L);
        completedTask.setTitle("Task A");
        completedTask.setStatus("COMPLETED");

        OnboardingTask incompleteTask = new OnboardingTask();
        incompleteTask.setId(11L);
        incompleteTask.setTitle("Old Task");
        incompleteTask.setStatus("PENDING");

        when(onboardingRepository.findById(1L)).thenReturn(Optional.of(onboarding));
        when(templateRepository.findByTemplateCode("tpl-eng-001")).thenReturn(Optional.of(newTemplate));
        when(onboardingTaskRepository.findByOnboardingId(1L)).thenReturn(List.of(completedTask, incompleteTask));

        OnboardingAssignTemplateRequest request = new OnboardingAssignTemplateRequest();
        request.setTemplateId("tpl-eng-001");
        request.setRegenerateWorkflow(true);

        mockMvc.perform(patch("/api/v1/onboarding/1/template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tasksCreated").value(1)); // Task B created, Task A skipped (already
                                                                      // completed)

        verify(onboardingTaskRepository).delete(incompleteTask);
        verify(onboardingTaskRepository, never()).delete(completedTask);
    }
}
