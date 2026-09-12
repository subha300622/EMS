package com.example.ems.onboarding.controller;

import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.onboarding.dto.audit.OnboardingAuditLogResponse;
import com.example.ems.onboarding.dto.comment.OnboardingCommentCreateRequest;
import com.example.ems.onboarding.dto.comment.OnboardingCommentResponse;
import com.example.ems.onboarding.dto.phase.OnboardingPhaseListResponse;
import com.example.ems.onboarding.dto.selfservice.OnboardingSelfServiceResponse;
import com.example.ems.onboarding.dto.task.TaskAssignRequest;
import com.example.ems.onboarding.dto.task.TaskCompleteRequest;
import com.example.ems.onboarding.dto.task.OnboardingTaskListResponse;
import com.example.ems.onboarding.exception.InvalidOnboardingTransitionException;
import com.example.ems.onboarding.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OnboardingExpansionApiTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OnboardingTaskService taskService;

    @Mock
    private OnboardingPhaseService phaseService;

    @Mock
    private OnboardingSelfService selfService;

    @Mock
    private OnboardingCommentService commentService;

    @Mock
    private OnboardingAuditLogService auditLogService;

    @Mock
    private OnboardingLifecycleService lifecycleService;

    @InjectMocks
    private TaskController taskController;

    @InjectMocks
    private OnboardingPhaseController phaseController;

    @InjectMocks
    private OnboardingSelfServiceController selfServiceController;

    @InjectMocks
    private OnboardingCommentController commentController;

    @InjectMocks
    private OnboardingAuditLogController auditLogController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(
                taskController,
                phaseController,
                selfServiceController,
                commentController,
                auditLogController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testGetTasksSuccess() throws Exception {
        OnboardingTaskListResponse response = new OnboardingTaskListResponse();
        response.setOnboardingId(1L);
        response.setTotalTasks(5);
        response.setCompletedTasks(2);
        response.setPendingTasks(3);

        when(taskService.getTasks(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/onboarding/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalTasks").value(5))
                .andExpect(jsonPath("$.data.completedTasks").value(2));
    }

    @Test
    public void testAssignTaskSuccess() throws Exception {
        TaskAssignRequest request = new TaskAssignRequest();
        request.setEmployeeId("EMP-1002");

        OnboardingTaskListResponse.TaskItem item = new OnboardingTaskListResponse.TaskItem();
        item.setTaskId(10L);
        item.setTitle("Setup Workstation");

        when(taskService.assignTask(eq(1L), eq(10L), any(TaskAssignRequest.class))).thenReturn(item);

        mockMvc.perform(patch("/api/v1/onboarding/1/tasks/10/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taskId").value(10))
                .andExpect(jsonPath("$.data.title").value("Setup Workstation"));
    }

    @Test
    public void testCompleteTaskSuccess() throws Exception {
        TaskCompleteRequest request = new TaskCompleteRequest();
        request.setRemarks("Document uploaded successfully");

        OnboardingTaskListResponse.TaskItem item = new OnboardingTaskListResponse.TaskItem();
        item.setTaskId(10L);
        item.setStatus("COMPLETED");

        when(taskService.completeTask(eq(1L), eq(10L), any(TaskCompleteRequest.class))).thenReturn(item);

        mockMvc.perform(post("/api/v1/onboarding/1/tasks/10/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    public void testGetPhasesSuccess() throws Exception {
        OnboardingPhaseListResponse response = new OnboardingPhaseListResponse();
        response.setOnboardingId(1L);

        when(phaseService.getPhases(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/onboarding/1/phases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.onboardingId").value(1));
    }

    @Test
    public void testSelfServiceProfileSuccess() throws Exception {
        OnboardingSelfServiceResponse response = new OnboardingSelfServiceResponse();
        response.setOnboardingId(1L);
        response.setEmployeeId("EMP-77");
        response.setStatus("IN_PROGRESS");

        when(selfService.getMyProfile()).thenReturn(response);

        mockMvc.perform(get("/api/v1/onboarding/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value("EMP-77"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    public void testGetCommentsSuccess() throws Exception {
        OnboardingCommentResponse response = new OnboardingCommentResponse();
        response.setCommentId(101L);
        response.setComment("Welcome to the team!");

        when(commentService.getComments(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/onboarding/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].commentId").value(101))
                .andExpect(jsonPath("$.data[0].comment").value("Welcome to the team!"));
    }

    @Test
    public void testCreateCommentSuccess() throws Exception {
        OnboardingCommentCreateRequest request = new OnboardingCommentCreateRequest();
        request.setComment("Great progress!");

        OnboardingCommentResponse response = new OnboardingCommentResponse();
        response.setCommentId(102L);
        response.setComment("Great progress!");

        when(commentService.createComment(eq(1L), any(OnboardingCommentCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/onboarding/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentId").value(102));
    }

    @Test
    public void testGetAuditLogsSuccess() throws Exception {
        OnboardingAuditLogResponse item = new OnboardingAuditLogResponse();
        item.setAuditId(1L);
        item.setAction("TASK_COMPLETED");
        item.setTimestamp(LocalDateTime.now());

        PageImpl<OnboardingAuditLogResponse> page = new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1);
        when(auditLogService.getAuditLogs(1L, 0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/v1/onboarding/1/audit-logs?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].action").value("TASK_COMPLETED"));
    }

    @Test
    public void testInvalidLifecycleTransitionReturns400() throws Exception {
        when(taskService.getTasks(99L)).thenThrow(new InvalidOnboardingTransitionException("COMPLETED", "IN_PROGRESS"));

        mockMvc.perform(get("/api/v1/onboarding/99/tasks"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ONB_400"));
    }
}
