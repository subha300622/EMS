package com.example.ems.employee.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.dto.*;
import com.example.ems.employee.entity.MyEmployeeDocument;
import com.example.ems.employee.service.MyDocumentService;
import com.example.ems.security.service.JwtService;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MyDocumentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MyDocumentService documentService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private MyDocumentController myDocumentController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "employee@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(myDocumentController).build();

        // Standard auth mock setup
        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        User user = new User();
        user.setWorkEmail(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    private void mockPermission(String permission, boolean allowed) {
        when(roleService.hasPermission(EMAIL, permission)).thenReturn(allowed);
    }

    @Test
    public void testGetDashboardSuccess() throws Exception {
        mockPermission("document.self.read", true);

        MyDocumentsDashboardResponse.EmployeeInfo emp = new MyDocumentsDashboardResponse.EmployeeInfo(1L, "EMP001", "John Doe");
        MyDocumentsDashboardResponse.SummaryInfo summary = new MyDocumentsDashboardResponse.SummaryInfo(21, 18, 2, 1, 86);
        MyDocumentsDashboardResponse.AlertInfo alert = new MyDocumentsDashboardResponse.AlertInfo("PENDING_UPLOAD", "2 documents pending upload", 2);
        MyDocumentsDashboardResponse resp = new MyDocumentsDashboardResponse(emp, summary, List.of(alert));

        when(documentService.getDocumentDashboard(EMAIL)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-documents/dashboard")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employee.name").value("John Doe"))
                .andExpect(jsonPath("$.data.summary.completionPercentage").value(86));
    }

    @Test
    public void testGetCategoriesSuccess() throws Exception {
        mockPermission("document.self.read", true);

        MyDocumentCategoriesResponse.CategoryItem cat = new MyDocumentCategoriesResponse.CategoryItem(1L, "Identity Documents", "IDENTITY", 3, 4, 75);
        MyDocumentCategoriesResponse resp = new MyDocumentCategoriesResponse(List.of(cat));

        when(documentService.getDocumentCategories(EMAIL)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-documents/categories")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categories[0].name").value("Identity Documents"));
    }

    @Test
    public void testGetDocumentsByCategorySuccess() throws Exception {
        mockPermission("document.self.read", true);

        MyCategoryDocumentsResponse.CategoryInfo category = new MyCategoryDocumentsResponse.CategoryInfo(1L, "Identity Documents");
        MyCategoryDocumentsResponse.DocumentItem item = new MyCategoryDocumentsResponse.DocumentItem(
                201L, "Passport", "PASSPORT", "UPLOADED", "APPROVED", LocalDate.of(2030, 6, 1),
                LocalDateTime.now(), new MyCategoryDocumentsResponse.ActionInfo(true, true, true, false)
        );
        MyCategoryDocumentsResponse resp = new MyCategoryDocumentsResponse(category, List.of(item));

        when(documentService.getDocumentsByCategory(EMAIL, 1L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-documents/categories/1/documents")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.category.name").value("Identity Documents"))
                .andExpect(jsonPath("$.data.documents[0].documentName").value("Passport"));
    }

    @Test
    public void testUploadDocumentSuccess() throws Exception {
        mockPermission("document.self.upload", true);

        MockMultipartFile file = new MockMultipartFile("file", "passport.pdf", MediaType.APPLICATION_PDF_VALUE, "passport content".getBytes());
        MyDocumentUploadResponse resp = new MyDocumentUploadResponse(301L, "passport.pdf", "PASSPORT", 1, "UPLOADED", "PENDING_VERIFICATION", LocalDateTime.now());

        when(documentService.uploadDocument(eq(EMAIL), eq(1L), eq("PASSPORT"), any(), any(), any(), any(), any())).thenReturn(resp);

        mockMvc.perform(multipart("/api/v1/my-documents")
                .file(file)
                .param("categoryId", "1")
                .param("documentType", "PASSPORT")
                .param("documentNumber", "P1234567")
                .param("issuedDate", "2020-01-01")
                .param("expiryDate", "2030-01-01")
                .param("remarks", "My passport")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.documentId").value(301L))
                .andExpect(jsonPath("$.data.status").value("UPLOADED"));
    }

    @Test
    public void testReplaceDocumentSuccess() throws Exception {
        mockPermission("document.self.update", true);

        MockMultipartFile file = new MockMultipartFile("file", "passport_renewed.pdf", MediaType.APPLICATION_PDF_VALUE, "passport updated".getBytes());
        MyDocumentReplaceResponse resp = new MyDocumentReplaceResponse(301L, 1, 2, "UPDATED", LocalDateTime.now());

        when(documentService.replaceDocument(eq(EMAIL), eq(301L), any(), any())).thenReturn(resp);

        mockMvc.perform(multipart("/api/v1/my-documents/301")
                .file(file)
                .param("remarks", "Renewed")
                .header("Authorization", AUTH_HEADER)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.newVersion").value(2));
    }

    @Test
    public void testGetDocumentDetailsSuccess() throws Exception {
        mockPermission("document.self.read", true);

        MyDocumentDetailsResponse.VerificationInfo verification = new MyDocumentDetailsResponse.VerificationInfo("APPROVED", "HR Manager", LocalDateTime.now(), "Ok");
        MyDocumentDetailsResponse resp = new MyDocumentDetailsResponse(301L, "Passport", "PASSPORT", "Identity Documents", "passport.pdf", "PDF", "2.5 MB", "P1234567", LocalDate.of(2020, 1, 1), LocalDate.of(2030, 1, 1), 2, verification, LocalDateTime.now());

        when(documentService.getDocumentDetails(EMAIL, 301L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-documents/301")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.documentName").value("Passport"))
                .andExpect(jsonPath("$.data.verification.status").value("APPROVED"));
    }

    @Test
    public void testPreviewDocumentSuccess() throws Exception {
        mockPermission("document.self.preview", true);

        MyDocumentPreviewResponse resp = new MyDocumentPreviewResponse(301L, "Passport", "http://storage.url", 600);
        when(documentService.previewDocument(EMAIL, 301L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-documents/301/preview")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.previewUrl").value("http://storage.url"));
    }

    @Test
    public void testDownloadDocumentSuccess() throws Exception {
        mockPermission("document.self.download", true);

        MyEmployeeDocument doc = new MyEmployeeDocument();
        doc.setFileName("passport.pdf");
        doc.setFileType("application/pdf");
        doc.setFileData("dummy data".getBytes());

        when(documentService.downloadDocument(EMAIL, 301L)).thenReturn(doc);

        mockMvc.perform(get("/api/v1/my-documents/301/download")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(status().is(200));
    }

    @Test
    public void testGetExpiryNotificationsSuccess() throws Exception {
        mockPermission("document.self.read", true);

        MyDocumentNotificationsResponse.NotificationItem note = new MyDocumentNotificationsResponse.NotificationItem(505L, "Passport", "EXPIRING_SOON", 30, LocalDate.of(2026, 6, 30));
        MyDocumentNotificationsResponse resp = new MyDocumentNotificationsResponse(List.of(note));

        when(documentService.getExpiryNotifications(EMAIL)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-documents/notifications")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notifications[0].daysRemaining").value(30));
    }

    @Test
    public void testGetDocumentHistorySuccess() throws Exception {
        mockPermission("document.self.history.read", true);

        MyDocumentHistoryResponse.HistoryItem item = new MyDocumentHistoryResponse.HistoryItem(1001L, "DOCUMENT_UPLOADED", "Passport", "John Doe", LocalDateTime.now());
        MyDocumentHistoryResponse.PaginationInfo pagination = new MyDocumentHistoryResponse.PaginationInfo(0, 10, 1, 1);
        MyDocumentHistoryResponse resp = new MyDocumentHistoryResponse(List.of(item), pagination);

        when(documentService.getDocumentActivityHistory(EMAIL, 0, 10)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-documents/history")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].action").value("DOCUMENT_UPLOADED"));
    }

    @Test
    public void testGetAllowedDocumentTypesSuccess() throws Exception {
        mockPermission("document.self.read", true);

        MyDocumentTypesResponse.DocumentTypeItem item = new MyDocumentTypesResponse.DocumentTypeItem(1L, "PASSPORT", "Passport", "Identity Documents", true, true, List.of("PDF"), 10);
        MyDocumentTypesResponse resp = new MyDocumentTypesResponse(List.of(item));

        when(documentService.getAllowedDocumentTypes()).thenReturn(resp);

        mockMvc.perform(get("/api/v1/my-documents/document-types")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.documentTypes[0].code").value("PASSPORT"));
    }
}
