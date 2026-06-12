package com.example.ems.common.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.common.dto.DmsDashboardResponse;
import com.example.ems.common.dto.DmsDocumentAuditLogResponse;
import com.example.ems.common.dto.DmsDocumentRequest;
import com.example.ems.common.dto.DmsDocumentResponse;
import com.example.ems.common.dto.DmsDocumentShareRequest;
import com.example.ems.common.dto.DmsDocumentShareResponse;
import com.example.ems.common.dto.DmsDocumentVersionRequest;
import com.example.ems.common.dto.DmsDocumentVersionResponse;
import com.example.ems.common.dto.DmsSignatureCompleteRequest;
import com.example.ems.common.dto.DmsSignatureRequest;
import com.example.ems.common.dto.DmsSignatureResponse;
import com.example.ems.employee.entity.Employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DmsControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private com.example.ems.common.service.DmsService dmsService;
    @Mock
    private com.example.ems.auth.repository.UserRepository userRepository;
    @Mock
    private com.example.ems.employee.repository.EmployeeRepository employeeRepository;
    @Mock
    private com.example.ems.security.service.JwtService jwtService;
    @Mock
    private com.example.ems.auth.service.RoleService roleService;

    @InjectMocks
    private com.example.ems.common.controller.DmsController dmsController;

    private com.example.ems.auth.entity.User hrUser;
    private com.example.ems.auth.entity.User empUser;
    private com.example.ems.employee.entity.Employee empRecord;
    private final String hrEmail = "hr@example.com";
    private final String empEmail = "emp@example.com";
    private final String token = "mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(dmsController).build();

        hrUser = new User();
        hrUser.setWorkEmail(hrEmail);
        hrUser.setEmployeeId("1");

        empUser = new User();
        empUser.setWorkEmail(empEmail);
        empUser.setEmployeeId("2");

        empRecord = new Employee();
        empRecord.setId(2L);
        empRecord.setFullName("John Doe");
        empRecord.setEmail(empEmail);
    }

    private void setupManager() {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(hrEmail);
        when(userRepository.findByWorkEmail(hrEmail)).thenReturn(Optional.of(hrUser));
        when(roleService.hasPermission(hrEmail, "employee.update")).thenReturn(true);
    }

    private void setupEmployee() {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
        when(roleService.hasPermission(empEmail, "employee.update")).thenReturn(false);
        when(roleService.hasPermission(empEmail, "employee.delete")).thenReturn(false);
        when(roleService.hasPermission(empEmail, "recruitment.manage")).thenReturn(false);
        when(employeeRepository.findByEmail(empEmail)).thenReturn(Optional.of(empRecord));
    }

    // ── 1. DASHBOARD ──────────────────────────────────────────────────────────
    @Test
    public void testGetDashboardSuccess() throws Exception {
        setupManager();

        DmsDashboardResponse stats = new DmsDashboardResponse();
        stats.setTotalDocuments(15);
        stats.setPendingApprovals(5);
        stats.setApprovedDocuments(8);
        stats.setRejectedDocuments(2);
        stats.setExpiringSoon(3);
        stats.setTotalShares(4);
        stats.setTotalSignatureRequests(5);
        stats.setPendingSignatureRequests(2);

        when(dmsService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/documents/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDocuments").value(15))
                .andExpect(jsonPath("$.data.pendingApprovals").value(5))
                .andExpect(jsonPath("$.data.approvedDocuments").value(8))
                .andExpect(jsonPath("$.data.rejectedDocuments").value(2))
                .andExpect(jsonPath("$.data.expiringSoon").value(3))
                .andExpect(jsonPath("$.data.totalShares").value(4))
                .andExpect(jsonPath("$.data.totalSignatureRequests").value(5))
                .andExpect(jsonPath("$.data.pendingSignatureRequests").value(2));
    }

    @Test
    public void testGetDashboardForbidden() throws Exception {
        setupEmployee();

        mockMvc.perform(get("/api/v1/documents/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
    }

    @Test
    public void testGetDashboardUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/documents/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_014"));
    }

    // ── 2. CREATE DOCUMENT ───────────────────────────────────────────────────
    @Test
    public void testCreateDocumentSuccessByManager() throws Exception {
        setupManager();

        DmsDocumentRequest req = new DmsDocumentRequest();
        req.setTitle("Contract Agreement");
        req.setCategory("CONTRACT");
        req.setEmployeeId(2L);
        req.setFileName("contract.pdf");
        req.setFileType("application/pdf");
        req.setFileSize(5000L);
        req.setDownloadUrl("/api/v1/documents/download/1");

        DmsDocumentResponse resp = new DmsDocumentResponse();
        resp.setId(10L);
        resp.setTitle("Contract Agreement");
        resp.setCategory("CONTRACT");
        resp.setEmployeeId(2L);

        when(dmsService.createDocument(any(DmsDocumentRequest.class), eq(hrEmail))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/documents")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Contract Agreement"));
    }

    @Test
    public void testCreateDocumentSuccessBySelf() throws Exception {
        setupEmployee();

        DmsDocumentRequest req = new DmsDocumentRequest();
        req.setTitle("My Visa");
        req.setCategory("VISA");
        req.setEmployeeId(2L); // matches empUser employeeId "2"
        req.setFileName("visa.pdf");
        req.setFileType("application/pdf");
        req.setFileSize(3000L);
        req.setDownloadUrl("/api/v1/documents/download/2");

        DmsDocumentResponse resp = new DmsDocumentResponse();
        resp.setId(11L);
        resp.setTitle("My Visa");
        resp.setEmployeeId(2L);

        when(dmsService.createDocument(any(DmsDocumentRequest.class), eq(empEmail))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/documents")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testCreateDocumentForbiddenForOther() throws Exception {
        setupEmployee();

        DmsDocumentRequest req = new DmsDocumentRequest();
        req.setTitle("Other Visa");
        req.setCategory("VISA");
        req.setEmployeeId(3L); // trying to upload for employee 3
        req.setFileName("visa.pdf");
        req.setFileType("application/pdf");
        req.setFileSize(3000L);
        req.setDownloadUrl("/api/v1/documents/download/3");

        mockMvc.perform(post("/api/v1/documents")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ── 3. LIST DOCUMENTS ────────────────────────────────────────────────────
    @Test
    public void testGetDocumentsByManager() throws Exception {
        setupManager();

        DmsDocumentResponse doc = new DmsDocumentResponse();
        doc.setId(10L);
        doc.setTitle("Contract Agreement");

        when(dmsService.getDocuments()).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/v1/documents")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Contract Agreement"));
    }

    @Test
    public void testGetDocumentsByEmployee() throws Exception {
        setupEmployee();

        DmsDocumentResponse doc = new DmsDocumentResponse();
        doc.setId(11L);
        doc.setTitle("My Visa");

        when(dmsService.getMyDocuments(empEmail)).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/v1/documents")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("My Visa"));
    }

    // ── 4. DETAILS ───────────────────────────────────────────────────────────
    @Test
    public void testGetDocumentByIdSuccessByOwner() throws Exception {
        setupEmployee();

        DmsDocumentResponse doc = new DmsDocumentResponse();
        doc.setId(11L);
        doc.setTitle("My Visa");
        doc.setEmployeeId(2L); // owns the document

        when(dmsService.getDocumentById(11L)).thenReturn(Optional.of(doc));

        mockMvc.perform(get("/api/v1/documents/11")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetDocumentByIdSuccessBySharedRecipient() throws Exception {
        setupEmployee();

        DmsDocumentResponse doc = new DmsDocumentResponse();
        doc.setId(10L);
        doc.setTitle("Shared Policy");
        doc.setEmployeeId(1L); // owned by superadmin 1

        when(dmsService.getDocumentById(10L)).thenReturn(Optional.of(doc));

        // Mock document shares list to simulate shared recipient
        DmsDocumentShareResponse share = new DmsDocumentShareResponse();
        share.setDocumentId(10L);
        share.setSharedWithEmployeeId(2L); // shared with employee John Doe (ID 2)
        when(dmsService.getSharesByDocument(10L)).thenReturn(List.of(share));

        mockMvc.perform(get("/api/v1/documents/10")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetDocumentByIdForbidden() throws Exception {
        setupEmployee();

        DmsDocumentResponse doc = new DmsDocumentResponse();
        doc.setId(12L);
        doc.setTitle("Confidential contract");
        doc.setEmployeeId(3L); // owned by employee 3

        when(dmsService.getDocumentById(12L)).thenReturn(Optional.of(doc));
        when(dmsService.getSharesByDocument(12L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/documents/12")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ── 5. DOWNLOAD ──────────────────────────────────────────────────────────
    @Test
    public void testDownloadDocumentSuccess() throws Exception {
        setupEmployee();

        DmsDocumentResponse doc = new DmsDocumentResponse();
        doc.setId(11L);
        doc.setEmployeeId(2L);
        doc.setDownloadUrl("/api/v1/documents/download/11");

        when(dmsService.getDocumentById(11L)).thenReturn(Optional.of(doc));
        when(dmsService.downloadDocument(11L, empEmail)).thenReturn(Optional.of(doc));

        mockMvc.perform(get("/api/v1/documents/11/download")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.downloadUrl").value("/api/v1/documents/download/11"));
    }

    // ── 6. APPROVAL / REJECTION ──────────────────────────────────────────────
    @Test
    public void testApproveDocumentSuccess() throws Exception {
        setupManager();

        DmsDocumentResponse resp = new DmsDocumentResponse();
        resp.setId(10L);
        resp.setStatus("APPROVED");

        when(dmsService.approveDocument(10L, hrEmail)).thenReturn(Optional.of(resp));

        mockMvc.perform(patch("/api/v1/documents/10/approve")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    public void testRejectDocumentSuccess() throws Exception {
        setupManager();

        DmsDocumentResponse resp = new DmsDocumentResponse();
        resp.setId(10L);
        resp.setStatus("REJECTED");

        when(dmsService.rejectDocument(10L, hrEmail)).thenReturn(Optional.of(resp));

        mockMvc.perform(patch("/api/v1/documents/10/reject")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    // ── 7. VERSIONS ──────────────────────────────────────────────────────────
    @Test
    public void testAddVersionSuccess() throws Exception {
        setupEmployee();

        DmsDocumentResponse doc = new DmsDocumentResponse();
        doc.setId(11L);
        doc.setEmployeeId(2L); // owner

        when(dmsService.getDocumentById(11L)).thenReturn(Optional.of(doc));

        DmsDocumentVersionRequest req = new DmsDocumentVersionRequest();
        req.setFileName("visa_v2.pdf");
        req.setFileSize(3500L);
        req.setDownloadUrl("/api/v1/documents/download/11_2");
        req.setChangeNotes("Updated with page 2 signature");

        DmsDocumentVersionResponse resp = new DmsDocumentVersionResponse();
        resp.setId(102L);
        resp.setVersionNumber(2);
        resp.setFileName("visa_v2.pdf");

        when(dmsService.addVersion(eq(11L), any(DmsDocumentVersionRequest.class), eq(empEmail))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/documents/11/versions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("visa_v2.pdf"));
    }

    // ── 8. SHARES ────────────────────────────────────────────────────────────
    @Test
    public void testShareDocumentSuccess() throws Exception {
        setupEmployee();

        DmsDocumentResponse doc = new DmsDocumentResponse();
        doc.setId(11L);
        doc.setEmployeeId(2L); // owner

        when(dmsService.getDocumentById(11L)).thenReturn(Optional.of(doc));

        DmsDocumentShareRequest req = new DmsDocumentShareRequest();
        req.setEmployeeId(3L); // share with employee 3
        req.setAccessLevel("READ");

        DmsDocumentShareResponse resp = new DmsDocumentShareResponse();
        resp.setId(50L);
        resp.setDocumentId(11L);
        resp.setSharedWithEmployeeId(3L);

        when(dmsService.shareDocument(eq(11L), any(DmsDocumentShareRequest.class), eq(empEmail))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/documents/11/shares")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sharedWithEmployeeId").value(3));
    }

    // ── 9. AUDIT LOGS ────────────────────────────────────────────────────────
    @Test
    public void testGetAuditLogsSuccess() throws Exception {
        setupEmployee();

        DmsDocumentResponse doc = new DmsDocumentResponse();
        doc.setId(11L);
        doc.setEmployeeId(2L); // owner

        when(dmsService.getDocumentById(11L)).thenReturn(Optional.of(doc));

        DmsDocumentAuditLogResponse log = new DmsDocumentAuditLogResponse();
        log.setId(201L);
        log.setAction("UPLOADED");
        log.setPerformedByEmail(empEmail);

        when(dmsService.getAuditLogs(11L)).thenReturn(List.of(log));

        mockMvc.perform(get("/api/v1/documents/11/audit-logs")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].action").value("UPLOADED"));
    }

    // ── 10. EXPIRING ─────────────────────────────────────────────────────────
    @Test
    public void testGetExpiringDocumentsByManager() throws Exception {
        setupManager();

        DmsDocumentResponse doc = new DmsDocumentResponse();
        doc.setId(10L);
        doc.setTitle("Contract Agreement");

        when(dmsService.getExpiringDocuments()).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/v1/documents/expiring")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── 11. SIGNATURE WORKFLOWS ──────────────────────────────────────────────
    @Test
    public void testSignatureRequestByManager() throws Exception {
        setupManager();

        Map<String, Object> body = Map.of(
                "employeeId", 2L,
                "comments", "Please sign the contract");

        DmsSignatureResponse resp = new DmsSignatureResponse();
        resp.setId(80L);
        resp.setStatus("PENDING");

        when(dmsService.submitSignatureRequest(eq(10L), any(DmsSignatureRequest.class), eq(hrEmail))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/documents/10/signature-request")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    public void testSignatureLogByEmployee() throws Exception {
        setupEmployee();

        Map<String, Object> body = Map.of(
                "status", "SIGNED",
                "comments", "Signed off");

        DmsSignatureResponse resp = new DmsSignatureResponse();
        resp.setId(80L);
        resp.setStatus("SIGNED");

        when(dmsService.completeSignature(eq(10L), any(DmsSignatureCompleteRequest.class), eq(empEmail)))
                .thenReturn(resp);

        mockMvc.perform(post("/api/v1/documents/10/signature-request")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SIGNED"));
    }

    // ── 12. REPORTS ──────────────────────────────────────────────────────────
    @Test
    public void testGetReportsSuccess() throws Exception {
        setupManager();

        Map<String, Object> reports = Map.of(
                "reportType", "general",
                "totalDocumentsCount", 10L);

        when(dmsService.getReports("general")).thenReturn(reports);

        mockMvc.perform(get("/api/v1/documents/reports/general")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportType").value("general"));
    }

    @Test
    public void testGetReportsForbidden() throws Exception {
        setupEmployee();

        mockMvc.perform(get("/api/v1/documents/reports/general")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateDocumentSuccess() throws Exception {
        setupManager();

        DmsDocumentResponse doc = new DmsDocumentResponse();
        doc.setId(10L);
        doc.setTitle("Contract Agreement");
        doc.setEmployeeId(2L);

        when(dmsService.getDocumentById(10L)).thenReturn(Optional.of(doc));

        DmsDocumentRequest req = new DmsDocumentRequest();
        req.setTitle("Contract Agreement Updated");
        req.setCategory("CONTRACT");
        req.setEmployeeId(2L);
        req.setFileName("contract_v2.pdf");
        req.setFileType("application/pdf");
        req.setFileSize(6000L);
        req.setDownloadUrl("/api/v1/documents/download/10");

        DmsDocumentResponse updatedResp = new DmsDocumentResponse();
        updatedResp.setId(10L);
        updatedResp.setTitle("Contract Agreement Updated");
        updatedResp.setEmployeeId(2L);

        when(dmsService.updateDocument(eq(10L), any(DmsDocumentRequest.class), eq(hrEmail))).thenReturn(updatedResp);

        mockMvc.perform(put("/api/v1/documents/10")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Contract Agreement Updated"));
    }

    @Test
    public void testUpdateDocumentNotFound() throws Exception {
        setupManager();

        DmsDocumentRequest req = new DmsDocumentRequest();
        req.setTitle("Contract Agreement Updated");
        req.setCategory("CONTRACT");
        req.setEmployeeId(2L);
        req.setFileName("contract_v2.pdf");
        req.setFileType("application/pdf");
        req.setFileSize(6000L);
        req.setDownloadUrl("/api/v1/documents/download/10");

        when(dmsService.getDocumentById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/documents/10")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testDeleteDocumentSuccess() throws Exception {
        setupManager();

        DmsDocumentResponse doc = new DmsDocumentResponse();
        doc.setId(10L);
        doc.setTitle("Contract Agreement");
        doc.setEmployeeId(2L);

        when(dmsService.getDocumentById(10L)).thenReturn(Optional.of(doc));
        when(dmsService.deleteDocument(10L, hrEmail)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/documents/10")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testDeleteDocumentNotFound() throws Exception {
        setupManager();

        when(dmsService.getDocumentById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/documents/10")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
