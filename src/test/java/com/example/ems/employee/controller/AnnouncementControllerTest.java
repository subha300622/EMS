package com.example.ems.employee.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.manager.AnnouncementDto;
import com.example.ems.common.dto.manager.AnnouncementCommentDto;
import com.example.ems.common.service.ManagerNotificationService;
import com.example.ems.employee.entity.Announcement;
import com.example.ems.employee.repository.AnnouncementRepository;
import com.example.ems.employee.repository.AnnouncementCommentRepository;
import com.example.ems.security.service.JwtService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AnnouncementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private AnnouncementCommentRepository announcementCommentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @Mock
    private ManagerNotificationService managerNotificationService;

    @InjectMocks
    private AnnouncementController announcementController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "hr@example.com";
    private User currentUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(announcementController).build();

        currentUser = new User();
        currentUser.setWorkEmail(EMAIL);
        currentUser.setFullName("HR Admin");

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(currentUser));
    }

    private void mockPermission(String permission, boolean allowed) {
        when(roleService.hasPermission(EMAIL, permission)).thenReturn(allowed);
    }

    @Test
    public void testGetAnnouncementsSuccess() throws Exception {
        mockPermission("announcement.manage", true);

        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setTitle("Company Policy Change");
        announcement.setPublishedDate(LocalDateTime.now());
        announcement.setLikes(0);
        announcement.setViews(0);

        Page<Announcement> page = new PageImpl<>(List.of(announcement), org.springframework.data.domain.PageRequest.of(0, 10), 1);
        when(announcementRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(announcementCommentRepository.countByAnnouncementId(1L)).thenReturn(2);

        mockMvc.perform(get("/api/v1/announcements")
                        .header("Authorization", AUTH_HEADER)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Company Policy Change"))
                .andExpect(jsonPath("$.data.content[0].comments").value(2));
    }

    @Test
    public void testGetAnnouncementDetailsSuccess() throws Exception {
        mockPermission("employee.announcement.read", true);
        AnnouncementDto responseDto = new AnnouncementDto(1L, "Picnic", "Annual picnic", "GENERAL", "HR Admin", "2026-06-22", 0, 0, 0);
        when(managerNotificationService.getAnnouncementDetails(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/announcements/1")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testCreateAnnouncementSuccess() throws Exception {
        mockPermission("announcement.manage", true);
        AnnouncementDto responseDto = new AnnouncementDto(1L, "Picnic", "Annual picnic", "GENERAL", "HR Admin", "2026-06-22", 0, 0, 0);
        when(managerNotificationService.createAnnouncement(eq(currentUser), any(AnnouncementDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/announcements")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Picnic\",\"description\":\"Annual picnic\",\"category\":\"GENERAL\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Picnic"));
    }

    @Test
    public void testUpdateAnnouncementSuccess() throws Exception {
        mockPermission("announcement.manage", true);
        AnnouncementDto responseDto = new AnnouncementDto(1L, "Picnic V2", "Annual picnic update", "GENERAL", "HR Admin", "2026-06-22", 0, 0, 0);
        when(managerNotificationService.updateAnnouncement(eq(1L), any(AnnouncementDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/announcements/1")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Picnic V2\",\"description\":\"Annual picnic update\",\"category\":\"GENERAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Picnic V2"));
    }

    @Test
    public void testDeleteAnnouncementSuccess() throws Exception {
        mockPermission("announcement.manage", true);
        doNothing().when(managerNotificationService).deleteAnnouncement(1L);

        mockMvc.perform(delete("/api/v1/announcements/1")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Announcement deleted successfully"));
    }

    @Test
    public void testLikeAnnouncementSuccess() throws Exception {
        mockPermission("employee.announcement.read", true);
        when(managerNotificationService.likeAnnouncement(1L)).thenReturn(Map.of("likes", 12));

        mockMvc.perform(post("/api/v1/announcements/1/like")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.likes").value(12));
    }

    @Test
    public void testGetCommentsSuccess() throws Exception {
        mockPermission("employee.announcement.read", true);
        AnnouncementCommentDto comment = new AnnouncementCommentDto(1L, 101L, "Great news!", "Jane Manager", "2026-06-22T10:00:00Z");
        when(managerNotificationService.getComments(101L)).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/v1/announcements/101/comments")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].content").value("Great news!"));
    }

    @Test
    public void testAddCommentSuccess() throws Exception {
        mockPermission("employee.announcement.read", true);
        AnnouncementCommentDto comment = new AnnouncementCommentDto(1L, 101L, "Great news!", "Jane Manager", "2026-06-22T10:00:00Z");
        when(managerNotificationService.addComment(currentUser, 101L, "Great news!")).thenReturn(comment);

        mockMvc.perform(post("/api/v1/announcements/101/comments")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Great news!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Great news!"));
    }
}
