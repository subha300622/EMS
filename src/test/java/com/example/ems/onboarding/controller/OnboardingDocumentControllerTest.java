package com.example.ems.onboarding.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.onboarding.dto.OnboardingDocumentVerifyRequest;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.entity.OnboardingDocument;
import com.example.ems.onboarding.repository.OnboardingDocumentRepository;
import com.example.ems.onboarding.repository.OnboardingRepository;
import com.example.ems.onboarding.service.OnboardingDocumentService;
import com.example.ems.security.context.SecurityContextFacade;
import com.example.ems.storage.service.FirebaseStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OnboardingDocumentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OnboardingRepository onboardingRepository;

    @Mock
    private OnboardingDocumentRepository onboardingDocumentRepository;

    @Mock
    private FirebaseStorageService storageService;

    @Mock
    private SecurityContextFacade securityContextFacade;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OnboardingDocumentService service;

    @InjectMocks
    private OnboardingDocumentController controller;

    private User testUser;
    private Onboarding onboarding;
    private OnboardingDocument document;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        // Manual injection of service and mocks into controllers/services
        org.springframework.test.util.ReflectionTestUtils.setField(service, "onboardingRepository", onboardingRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "onboardingDocumentRepository", onboardingDocumentRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "storageService", storageService);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "documentService", service);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "securityContextFacade", securityContextFacade);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "userRepository", userRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new com.example.ems.config.GlobalExceptionHandler())
                .build();

        // Standard setup entities
        Employee employee = new Employee();
        employee.setId(10L);
        employee.setEmployeeId("emp-eng-101");
        employee.setEmail("vikram.seth@company.com");
        employee.setDepartment("Engineering");

        onboarding = new Onboarding();
        onboarding.setId(241L);
        onboarding.setEmployee(employee);
        onboarding.setStatus("PRE_JOINING");
        onboarding.setJoiningDate(LocalDate.of(2026, 8, 1));

        document = new OnboardingDocument();
        document.setId(1L);
        document.setOnboarding(onboarding);
        document.setDocumentType("Aadhaar Card");
        document.setFileName("[Pending Upload] - Aadhaar Card");
        document.setVerificationStatus("PENDING");

        Role role = new Role();
        role.setName("EMPLOYEE");

        testUser = new User();
        testUser.setUserId("emp-eng-101");
        testUser.setWorkEmail("vikram.seth@company.com");
        testUser.setRole(role);
        testUser.setDepartment("Engineering");
    }

    @Test
    public void testGetDocumentsList() throws Exception {
        when(onboardingRepository.findById(241L)).thenReturn(Optional.of(onboarding));
        when(onboardingDocumentRepository.findByOnboardingId(241L)).thenReturn(Collections.singletonList(document));

        mockMvc.perform(get("/api/v1/onboarding/onb-241/documents")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("doc-1"))
                .andExpect(jsonPath("$.data[0].documentName").value("Aadhaar Card"));
    }

    @Test
    public void testUploadDocumentSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "aadhaar.pdf", MediaType.APPLICATION_PDF_VALUE, "mock pdf content".getBytes()
        );

        when(securityContextFacade.getEmail()).thenReturn("vikram.seth@company.com");
        when(userRepository.findByWorkEmail("vikram.seth@company.com")).thenReturn(Optional.of(testUser));
        when(onboardingRepository.findById(241L)).thenReturn(Optional.of(onboarding));
        when(onboardingDocumentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(storageService.uploadFile(eq(file), eq("onboarding/onb-241"), any(String.class))).thenReturn("onboarding/onb-241/mock-path.pdf");
        when(onboardingDocumentRepository.save(any(OnboardingDocument.class))).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(multipart("/api/v1/onboarding/onb-241/documents/doc-1/upload")
                .file(file)
                .header("Authorization", "Bearer mockToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("aadhaar.pdf"))
                .andExpect(jsonPath("$.data.status").value("UPLOADED"));
    }

    @Test
    public void testDownloadDocumentSuccess() throws Exception {
        document.setFilePath("onboarding/onb-241/mock-path.pdf");
        document.setFileName("aadhaar.pdf");

        when(securityContextFacade.getEmail()).thenReturn("vikram.seth@company.com");
        when(userRepository.findByWorkEmail("vikram.seth@company.com")).thenReturn(Optional.of(testUser));
        when(onboardingRepository.findById(241L)).thenReturn(Optional.of(onboarding));
        when(onboardingDocumentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(onboardingDocumentRepository.findByOnboardingId(241L)).thenReturn(Collections.singletonList(document));
        when(storageService.downloadFileAsStream("onboarding/onb-241/mock-path.pdf")).thenReturn(new ByteArrayInputStream("mock pdf content".getBytes()));

        mockMvc.perform(get("/api/v1/onboarding/onb-241/documents/doc-1/download")
                .header("Authorization", "Bearer mockToken"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"aadhaar.pdf\""))
                .andExpect(content().string("mock pdf content"));
    }

    @Test
    public void testVerifyDocumentSuccess() throws Exception {
        document.setFilePath("onboarding/onb-241/mock-path.pdf");

        Role hrRole = new Role();
        hrRole.setName("HR");
        testUser.setRole(hrRole);

        when(securityContextFacade.getEmail()).thenReturn("hr@company.com");
        when(userRepository.findByWorkEmail("hr@company.com")).thenReturn(Optional.of(testUser));
        when(onboardingRepository.findById(241L)).thenReturn(Optional.of(onboarding));
        when(onboardingDocumentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(onboardingDocumentRepository.save(any(OnboardingDocument.class))).thenAnswer(i -> i.getArguments()[0]);

        OnboardingDocumentVerifyRequest request = new OnboardingDocumentVerifyRequest();
        request.setStatus("VERIFIED");
        request.setRemarks("Document verified successfully");

        mockMvc.perform(patch("/api/v1/onboarding/onb-241/documents/doc-1/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer mockToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"));
    }

    @Test
    public void testDeleteDocumentSuccess() throws Exception {
        document.setFilePath("onboarding/onb-241/mock-path.pdf");

        when(securityContextFacade.getEmail()).thenReturn("vikram.seth@company.com");
        when(userRepository.findByWorkEmail("vikram.seth@company.com")).thenReturn(Optional.of(testUser));
        when(onboardingRepository.findById(241L)).thenReturn(Optional.of(onboarding));
        when(onboardingDocumentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(onboardingDocumentRepository.save(any(OnboardingDocument.class))).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(delete("/api/v1/onboarding/onb-241/documents/doc-1")
                .header("Authorization", "Bearer mockToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(storageService).deleteFile("onboarding/onb-241/mock-path.pdf");
    }
}
