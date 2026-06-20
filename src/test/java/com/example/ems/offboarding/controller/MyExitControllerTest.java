package com.example.ems.offboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.service.MyExitService;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class MyExitControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MyExitService myExitService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private MyExitController myExitController;

    private User empUser;
    private String empEmail = "john@example.com";
    private String mockToken = "mock-bearer-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(myExitController).build();

        empUser = new User();
        empUser.setWorkEmail(empEmail);

        when(jwtService.validateAccessToken(mockToken)).thenReturn(true);
        when(jwtService.getEmailFromToken(mockToken)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
    }



    @Test
    public void testSubmitResignationSuccess() throws Exception {
        SubmitResignationRequest req = new SubmitResignationRequest();
        req.setReason("Higher education opportunity");
        req.setReasonCategory("CAREER_GROWTH");
        req.setResignationDate(LocalDate.of(2026, 4, 1));
        req.setRequestedLastWorkingDay(LocalDate.of(2026, 4, 30));
        req.setComments("Thank you for all the learning opportunities.");

        SubmitResignationResponse resp = new SubmitResignationResponse("Resignation request submitted successfully", 501L, "PENDING_MANAGER_APPROVAL", LocalDateTime.of(2026, 4, 1, 10, 30, 0));

        when(myExitService.submitResignation(eq(empEmail), any(SubmitResignationRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/my-exit/resignation")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resignation request submitted successfully"))
                .andExpect(jsonPath("$.exitRequestId").value(501L))
                .andExpect(jsonPath("$.status").value("PENDING_MANAGER_APPROVAL"));
    }

    @Test
    public void testGetExitChecklistSuccess() throws Exception {
        ExitChecklistResponse.ChecklistItem item1 = new ExitChecklistResponse.ChecklistItem(1L, "Submit Resignation Letter", "EMPLOYEE", "COMPLETED", LocalDateTime.of(2026, 3, 28, 15, 20, 0), false, List.of(), null);
        ExitChecklistResponse.ChecklistItem item2 = new ExitChecklistResponse.ChecklistItem(2L, "Upload Clearance Documents", "EMPLOYEE", "IN_PROGRESS", null, true, List.of("UPLOAD_DOCUMENT"), null);
        ExitChecklistResponse.ChecklistItem item3 = new ExitChecklistResponse.ChecklistItem(3L, "Return Company Laptop", "IT", "PENDING", null, false, List.of(), 55L);
        ExitChecklistResponse actualResp = new ExitChecklistResponse(List.of(item1, item2, item3), new ExitChecklistResponse.ChecklistSummary(2, 8));
        when(myExitService.getExitChecklist(empEmail)).thenReturn(actualResp);

        mockMvc.perform(get("/api/v1/my-exit/checklist")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checklist[0].taskId").value(1L))
                .andExpect(jsonPath("$.summary.completed").value(2));
    }

    @Test
    public void testUploadDocumentSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "noc_document.pdf", MediaType.APPLICATION_PDF_VALUE, "dummy content".getBytes());
        UploadDocumentResponse resp = new UploadDocumentResponse(901L, "noc_document.pdf", "NOC", "John Doe", LocalDateTime.of(2026, 4, 2, 11, 30, 0), "UNDER_VERIFICATION");

        when(myExitService.uploadDocument(eq(empEmail), eq("NOC"), eq("noc_document.pdf"), eq("All documents submitted"))).thenReturn(resp);

        mockMvc.perform(multipart("/api/v1/my-exit/documents")
                .file(file)
                .param("documentType", "NOC")
                .param("comments", "All documents submitted")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(901L))
                .andExpect(jsonPath("$.documentName").value("noc_document.pdf"))
                .andExpect(jsonPath("$.status").value("UNDER_VERIFICATION"));
    }

    @Test
    public void testGetUploadedDocumentsSuccess() throws Exception {
        UploadedDocumentsResponse.DocumentItem doc = new UploadedDocumentsResponse.DocumentItem(901L, "NOC", "noc.pdf", "APPROVED", "HR", LocalDateTime.of(2026, 4, 3, 9, 30, 0));
        UploadedDocumentsResponse resp = new UploadedDocumentsResponse(List.of(doc));

        when(myExitService.getUploadedDocuments(empEmail)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-exit/documents")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documents[0].documentId").value(901L))
                .andExpect(jsonPath("$.documents[0].status").value("APPROVED"));
    }

    @Test
    public void testConfirmAssetReturnSuccess() throws Exception {
        AssetReturnConfirmRequest req = new AssetReturnConfirmRequest();
        req.setReturnDate(LocalDate.of(2026, 4, 5));
        req.setCondition("GOOD");
        req.setRemarks("Laptop returned with charger and bag");

        AssetReturnConfirmResponse resp = new AssetReturnConfirmResponse(55L, "Dell Latitude 5440", "RETURN_PENDING_VERIFICATION", null);

        when(myExitService.confirmAssetReturn(eq(empEmail), eq(55L), any(AssetReturnConfirmRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/my-exit/assets/55/return")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetId").value(55L))
                .andExpect(jsonPath("$.status").value("RETURN_PENDING_VERIFICATION"));
    }

    @Test
    public void testGetAssetsSuccess() throws Exception {
        AssignedAssetsResponse.AssetItem asset1 = new AssignedAssetsResponse.AssetItem(55L, "Dell Latitude 5440", "LAPTOP", "DL12345", "PENDING");
        AssignedAssetsResponse.AssetItem asset2 = new AssignedAssetsResponse.AssetItem(56L, "iPhone 14 Pro", "MOBILE", "DL12345", "PENDING");
        AssignedAssetsResponse resp = new AssignedAssetsResponse(List.of(asset1, asset2));

        when(myExitService.getAssignedAssets(empEmail)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-exit/assets")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assets[0].assetId").value(55L))
                .andExpect(jsonPath("$.assets[1].assetId").value(56L));
    }

    @Test
    public void testScheduleInterviewSuccess() throws Exception {
        ExitInterviewScheduleRequest req = new ExitInterviewScheduleRequest();
        req.setPreferredDate(LocalDate.of(2026, 4, 8));
        req.setPreferredTime("15:00");
        req.setComments("Available after lunch");

        ExitInterviewScheduleResponse resp = new ExitInterviewScheduleResponse(300L, "SCHEDULE_REQUESTED", null);

        when(myExitService.scheduleExitInterview(eq(empEmail), any(ExitInterviewScheduleRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/my-exit/interview")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interviewId").value(300L))
                .andExpect(jsonPath("$.status").value("SCHEDULE_REQUESTED"));
    }

    @Test
    public void testSignAgreementSuccess() throws Exception {
        SignAgreementRequest req = new SignAgreementRequest();
        req.setAgreementType("NDA");
        req.setAccepted(true);

        SignAgreementResponse resp = new SignAgreementResponse(700L, "NDA", LocalDateTime.of(2026, 4, 7, 10, 20, 0), "SIGNED");

        when(myExitService.signAgreement(eq(empEmail), any(SignAgreementRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/my-exit/agreements/sign")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agreementId").value(700L))
                .andExpect(jsonPath("$.status").value("SIGNED"));
    }

    @Test
    public void testGetSettlementSuccess() throws Exception {
        SettlementDetailsResponse resp = new SettlementDetailsResponse(
                BigDecimal.valueOf(87000), BigDecimal.valueOf(54000), BigDecimal.valueOf(28000),
                BigDecimal.valueOf(4200), BigDecimal.valueOf(0), BigDecimal.valueOf(173200),
                "PENDING_FINANCE_APPROVAL", LocalDate.of(2026, 4, 20)
        );

        when(myExitService.getSettlementDetails(empEmail)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-exit/settlement")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salary").value(87000))
                .andExpect(jsonPath("$.netPayableAmount").value(173200));
    }

    @Test
    public void testGetTimelineSuccess() throws Exception {
        ExitTimelineResponse.TimelineEventItem event1 = new ExitTimelineResponse.TimelineEventItem(LocalDateTime.of(2026, 4, 1, 10, 30, 0), "Resignation Submitted", "John Doe");
        ExitTimelineResponse.TimelineEventItem event2 = new ExitTimelineResponse.TimelineEventItem(LocalDateTime.of(2026, 4, 2, 11, 0, 0), "Manager Approved Resignation", "Manager");
        ExitTimelineResponse resp = new ExitTimelineResponse(List.of(event1, event2));

        when(myExitService.getExitTimeline(empEmail)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-exit/timeline")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events[0].performedBy").value("John Doe"))
                .andExpect(jsonPath("$.events[1].performedBy").value("Manager"));
    }

    @Test
    public void testDownloadExperienceLetterSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/my-exit/experience-letter")
                .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"experience_letter.pdf\""));
    }

    @Test
    public void testCancelExitRequestSuccess() throws Exception {
        CancelExitRequest req = new CancelExitRequest();
        req.setReason("Decided to continue with the company");

        CancelExitResponse resp = new CancelExitResponse(501L, "CANCELLED", "Exit request cancelled successfully");

        when(myExitService.cancelExitRequest(eq(empEmail), any(CancelExitRequest.class))).thenReturn(resp);

        mockMvc.perform(put("/api/v1/my-exit/resignation/cancel")
                .header("Authorization", "Bearer " + mockToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitRequestId").value(501L))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
