package com.example.ems.onboarding.controller;

import com.example.ems.employee.entity.Department;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.onboarding.dto.OnboardingTemplateCreateRequest;
import com.example.ems.onboarding.dto.OnboardingTemplateResponse;
import com.example.ems.onboarding.entity.OnboardingTemplate;
import com.example.ems.onboarding.repository.OnboardingTemplateRepository;
import com.example.ems.onboarding.service.OnboardingTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OnboardingTemplateControllerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OnboardingTemplateRepository templateRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ObjectMapper mockObjectMapper; // will spy or use real in service test

    @InjectMocks
    private OnboardingTemplateService service;

    @InjectMocks
    private OnboardingTemplateController controller;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        org.springframework.test.util.ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "templateService", service);
    }

    @Test
    public void testCreateTemplateServiceSuccess() {
        // Mock department
        Department dept = new Department();
        dept.setId(1L);
        dept.setCode("ENG-ORG0001");
        when(departmentRepository.findByCode("dept-eng-001")).thenReturn(Optional.of(dept));

        // Mock repository save
        OnboardingTemplate savedEntity = new OnboardingTemplate();
        savedEntity.setId(10L);
        savedEntity.setName("Engineering Onboarding");
        savedEntity.setDepartmentId("dept-eng-001");
        savedEntity.setDesignation("Software Engineer");
        savedEntity.setEmploymentType("Full-time");
        savedEntity.setEffectiveFrom(LocalDate.of(2026, 7, 1));
        savedEntity.setIsDefault(true);
        savedEntity.setTemplateCode("TPL-ENG-001");
        savedEntity.setSectionsJson(
                "[{\"name\":\"Company Process\",\"tasks\":[{\"name\":\"Issue Laptop\",\"dueDays\":2}]}]");
        savedEntity.setDocumentsJson("[]");

        when(templateRepository.save(any(OnboardingTemplate.class))).thenReturn(savedEntity);
        when(templateRepository.countByDepartmentId("dept-eng-001")).thenReturn(0L);

        // Setup request
        OnboardingTemplateCreateRequest request = new OnboardingTemplateCreateRequest();
        request.setName("Engineering Onboarding");
        request.setDepartmentId("dept-eng-001");
        request.setDesignation("Software Engineer");
        request.setEmploymentType("Full-time");
        request.setEffectiveFrom(LocalDate.of(2026, 7, 1));
        request.setIsDefault(true);

        OnboardingTemplateCreateRequest.SectionRequest sec = new OnboardingTemplateCreateRequest.SectionRequest();
        sec.setName("Company Process");
        OnboardingTemplateCreateRequest.TaskRequest task = new OnboardingTemplateCreateRequest.TaskRequest();
        task.setName("Issue Laptop");
        task.setDueDays(2);
        sec.setTasks(List.of(task));
        request.setSections(List.of(sec));
        request.setDocuments(Collections.emptyList());

        // Invoke service
        OnboardingTemplateResponse response = service.createTemplate(request);

        // Assertions
        assertNotNull(response);
        assertEquals("TPL-ENG-001", response.getId());
        assertEquals("TPL-ENG-001", response.getTemplateCode());
        assertEquals(1, response.getPhases());
        assertEquals(1, response.getTasks());
        assertEquals("2 days", response.getAvgDays());
        assertTrue(response.getIsDefault());

        // Verify default reset query was called
        verify(templateRepository).resetDefaultTemplate("dept-eng-001", "Software Engineer", "Full-time");
    }

    @Test
    public void testCreateTemplateServiceValidationDatesFailure() {
        OnboardingTemplateCreateRequest request = new OnboardingTemplateCreateRequest();
        request.setName("Test");
        request.setEffectiveFrom(LocalDate.of(2026, 7, 1));
        request.setEffectiveTo(LocalDate.of(2026, 6, 30)); // before start date

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.createTemplate(request);
        });

        assertEquals("effectiveTo date must be after or equal to effectiveFrom date", ex.getMessage());
    }

    @Test
    public void testCreateTemplateServiceValidationEmptySectionsFailure() {
        OnboardingTemplateCreateRequest request = new OnboardingTemplateCreateRequest();
        request.setName("Test");
        request.setEffectiveFrom(LocalDate.of(2026, 7, 1));
        request.setSections(Collections.emptyList());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.createTemplate(request);
        });

        assertEquals("sections must contain at least one section", ex.getMessage());
    }
}
