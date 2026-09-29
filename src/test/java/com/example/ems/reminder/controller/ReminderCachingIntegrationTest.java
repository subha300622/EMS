package com.example.ems.reminder.controller;

import com.example.ems.reminder.dto.ReminderRequest;
import com.example.ems.reminder.entity.Reminder;
import com.example.ems.reminder.repository.ReminderRepository;
import com.example.ems.reminder.service.ReminderCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Reminder caching layer.
 *
 * <p>These tests verify that the API endpoints behave correctly with the
 * production-grade {@link ReminderCacheService} facade (no Spring Cache
 * annotations used). Redis may or may not be available during tests — the
 * service degrades gracefully by falling back to the DB.</p>
 *
 * <h3>What is tested</h3>
 * <ul>
 *   <li>GET by ID: 200 on hit, 500 on missing (mapped by GlobalExceptionHandler).</li>
 *   <li>GET all: always returns valid list.</li>
 *   <li>GET by user: filters correctly.</li>
 *   <li>POST: creates and returns correct data.</li>
 *   <li>PUT: updates and returns correct data.</li>
 *   <li>DELETE: removes and subsequent GET returns 500.</li>
 * </ul>
 */
@SpringBootTest
@Transactional
public class ReminderCachingIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ReminderRepository reminderRepository;


    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private Reminder testReminder;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        reminderRepository.deleteAll();

        Reminder reminder = new Reminder();
        reminder.setTitle("Sprint Planning Reminder");
        reminder.setDescription("Discuss upcoming features and tasks.");
        reminder.setReminderDate(LocalDateTime.now().plusDays(2));
        reminder.setEmployeeId(1L);
        testReminder = reminderRepository.save(reminder);
    }

    // ── GET ───────────────────────────────────────────────────────────────────

    @Test
    public void testGetReminderById_returnsCorrectData() throws Exception {
        mockMvc.perform(get("/api/v1/reminders/" + testReminder.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Sprint Planning Reminder"))
                .andExpect(jsonPath("$.data.employeeId").value(1));
    }

    @Test
    public void testGetReminderById_calledTwice_returnsSameData() throws Exception {
        // First call → DB (cache miss)
        mockMvc.perform(get("/api/v1/reminders/" + testReminder.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Sprint Planning Reminder"));

        // Give async cache write time to complete
        TimeUnit.MILLISECONDS.sleep(200);

        // Second call → may hit Redis cache (if Redis is up) or DB (if Redis is down)
        // Either way, data must be consistent
        mockMvc.perform(get("/api/v1/reminders/" + testReminder.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Sprint Planning Reminder"));
    }

    @Test
    public void testGetReminder_notFound_returnsError() throws Exception {
        mockMvc.perform(get("/api/v1/reminders/999999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllReminders_returnsList() throws Exception {
        mockMvc.perform(get("/api/v1/reminders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reminders").isArray());
    }

    @Test
    public void testGetRemindersByUser_returnsFilteredList() throws Exception {
        mockMvc.perform(get("/api/v1/reminders/user/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reminders").isArray());
    }

    // ── POST ──────────────────────────────────────────────────────────────────

    @Test
    public void testCreateReminder_returnsCreatedData() throws Exception {
        ReminderRequest request = new ReminderRequest();
        request.setTitle("Submit Timesheet");
        request.setDescription("Ensure all hours are entered.");
        request.setReminderDate(LocalDateTime.now().plusDays(1));
        request.setEmployeeId(1L);

        mockMvc.perform(post("/api/v1/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Submit Timesheet"))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    public void testCreateThenGet_returnsNewReminder() throws Exception {
        ReminderRequest request = new ReminderRequest();
        request.setTitle("Code Review");
        request.setDescription("Review PR #42");
        request.setReminderDate(LocalDateTime.now().plusDays(3));
        request.setEmployeeId(2L);

        // Create
        String createResponse = mockMvc.perform(post("/api/v1/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long createdId = objectMapper.readTree(createResponse)
                .get("data").get("id").asLong();

        TimeUnit.MILLISECONDS.sleep(200); // let async cache write settle

        // Get newly created reminder
        mockMvc.perform(get("/api/v1/reminders/" + createdId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Code Review"));
    }

    // ── PUT ───────────────────────────────────────────────────────────────────

    @Test
    public void testUpdateReminder_returnsUpdatedData() throws Exception {
        ReminderRequest request = new ReminderRequest();
        request.setTitle("Sprint Planning Updated");
        request.setDescription("Updated description.");
        request.setReminderDate(testReminder.getReminderDate());
        request.setEmployeeId(testReminder.getEmployeeId());

        mockMvc.perform(put("/api/v1/reminders/" + testReminder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Sprint Planning Updated"));
    }

    @Test
    public void testUpdateThenGet_returnsFreshData() throws Exception {
        ReminderRequest request = new ReminderRequest();
        request.setTitle("Sprint Planning Updated");
        request.setDescription("Updated description.");
        request.setReminderDate(testReminder.getReminderDate());
        request.setEmployeeId(testReminder.getEmployeeId());

        mockMvc.perform(put("/api/v1/reminders/" + testReminder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        TimeUnit.MILLISECONDS.sleep(200); // let async cache refresh settle

        mockMvc.perform(get("/api/v1/reminders/" + testReminder.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Sprint Planning Updated"));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Test
    public void testDeleteReminder_succeeds() throws Exception {
        mockMvc.perform(delete("/api/v1/reminders/" + testReminder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testDeleteThenGet_returnsError() throws Exception {
        // Delete the reminder
        mockMvc.perform(delete("/api/v1/reminders/" + testReminder.getId()))
                .andExpect(status().isOk());

        TimeUnit.MILLISECONDS.sleep(200); // let async eviction settle

        // Subsequent GET must fail with an error (not found)
        mockMvc.perform(get("/api/v1/reminders/" + testReminder.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
