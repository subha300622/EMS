package com.example.ems.support.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.support.dto.*;
import com.example.ems.support.service.MySupportService;
import com.example.ems.security.service.JwtService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmployeeSupportWorkflowSimulationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private MySupportService supportService;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private RoleService roleService;

    @InjectMocks
    private MySupportController controller;

    private final String employeeEmail = "john.doe@company.com";
    private final String token = "employee-mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        User empUser = new User();
        empUser.setId(22L);
        empUser.setWorkEmail(employeeEmail);
        empUser.setFullName("John Doe");

        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(employeeEmail);
        when(userRepository.findByWorkEmail(employeeEmail)).thenReturn(Optional.of(empUser));
        when(roleService.hasPermission(employeeEmail, "employee.support-ticket.read")).thenReturn(true);
        when(roleService.hasPermission(employeeEmail, "employee.support-ticket.create")).thenReturn(true);
        when(roleService.hasPermission(employeeEmail, "employee.support-ticket.update")).thenReturn(true);
    }

    @Test
    public void runEmployeeWorkflowSimulation() throws Exception {
        System.out.println("================================================================================");
        System.out.println("                EMPLOYEE SELF-SERVICE SUPPORT WORKFLOW SIMULATION               ");
        System.out.println("================================================================================");

        // STEP 1: EMPLOYEE CREATES SUPPORT TICKET
        CreateTicketRequest createReq = new CreateTicketRequest();
        createReq.setSubject("Workstation Monitor Flickering");
        createReq.setDescription("My dual setup primary monitor keeps flickering and turning black randomly.");
        createReq.setCategoryId(4L);
        createReq.setPriority("MEDIUM");
        createReq.setPreferredContactMethod("EMAIL");

        CreateTicketResponse mockCreateResp = new CreateTicketResponse(
                102L,
                "SUP-2026-000102",
                "OPEN",
                "MEDIUM",
                LocalDateTime.now().toString(),
                LocalDateTime.now().plusHours(4).toString(),
                LocalDateTime.now().plusDays(2).toString(),
                "Support ticket created successfully"
        );

        when(supportService.createTicket(eq(employeeEmail), any(CreateTicketRequest.class))).thenReturn(mockCreateResp);

        String createJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(createReq);
        System.out.println("\n[HTTP REQUEST] POST /api/v1/my-support/tickets");
        System.out.println("Headers: Authorization = Bearer employee-mock-token");
        System.out.println("Body:\n" + createJson);

        MvcResult createResult = mockMvc.perform(post("/api/v1/my-support/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ticketId").value(102))
                .andExpect(jsonPath("$.data.ticketNumber").value("SUP-2026-000102"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.priority").value("MEDIUM"))
                .andReturn();

        String createResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(createResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 201 Created");
        System.out.println("Body:\n" + createResponseJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 2: EMPLOYEE LISTS THEIR TICKETS
        MyTicketsResponse.TicketListItem mockItem = new MyTicketsResponse.TicketListItem(
                102L, "SUP-2026-000102", "Workstation Monitor Flickering", "IT Hardware", "MEDIUM", "OPEN", "Hardware Team", LocalDateTime.now().toString(), LocalDateTime.now().toString()
        );
        MyTicketsResponse mockListResp = new MyTicketsResponse(
                Collections.singletonList(mockItem),
                new MyTicketsResponse.PaginationDto(0, 10, 1L, 1, false)
        );

        when(supportService.getMyTickets(eq(employeeEmail), any(), any(), any(), any(), any())).thenReturn(mockListResp);

        System.out.println("\n[HTTP REQUEST] GET /api/v1/my-support/tickets?status=OPEN");
        System.out.println("Headers: Authorization = Bearer employee-mock-token");

        MvcResult listResult = mockMvc.perform(get("/api/v1/my-support/tickets")
                .header("Authorization", "Bearer " + token)
                .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].ticketId").value(102))
                .andExpect(jsonPath("$.data.content[0].status").value("OPEN"))
                .andReturn();

        String listResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(listResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 200 OK");
        System.out.println("Body:\n" + listResponseJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 3: EMPLOYEE GETS TICKET DETAILS
        TicketDetailsResponse.CreatedByDto creator = new TicketDetailsResponse.CreatedByDto(22L, "John Doe");
        TicketDetailsResponse.AssignedToDto assignee = new TicketDetailsResponse.AssignedToDto("Hardware Team", "Agent Smith");
        TicketDetailsResponse.SlaDetailDto sla = new TicketDetailsResponse.SlaDetailDto(LocalDateTime.now().plusHours(4).toString(), LocalDateTime.now().plusDays(2).toString(), "WITHIN_SLA");
        
        TicketDetailsResponse mockDetailResp = new TicketDetailsResponse(
                102L,
                "SUP-2026-000102",
                "Workstation Monitor Flickering",
                "My dual setup primary monitor keeps flickering and turning black randomly.",
                "IT Hardware",
                "MEDIUM",
                "OPEN",
                creator,
                assignee,
                Collections.emptyList(),
                sla
        );

        when(supportService.getTicketDetails(eq(employeeEmail), eq(102L))).thenReturn(mockDetailResp);

        System.out.println("\n[HTTP REQUEST] GET /api/v1/my-support/tickets/102");
        System.out.println("Headers: Authorization = Bearer employee-mock-token");

        MvcResult detailResult = mockMvc.perform(get("/api/v1/my-support/tickets/102")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ticketId").value(102))
                .andExpect(jsonPath("$.data.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn();

        String detailResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(detailResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 200 OK");
        System.out.println("Body:\n" + detailResponseJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 4: EMPLOYEE UPDATES TICKET DETAILS (ONLY IF STATUS IS OPEN)
        UpdateTicketRequest updateReq = new UpdateTicketRequest();
        updateReq.setSubject("Workstation Monitor Flickering (Dell 24-inch)");
        updateReq.setDescription("Primary Dell 24-inch monitor flickers and turns black. Connection cables checked and swapped.");

        UpdateTicketResponse mockUpdateResp = new UpdateTicketResponse(
                102L,
                "SUP-2026-000102",
                updateReq.getSubject(),
                updateReq.getDescription(),
                "MEDIUM",
                "OPEN",
                LocalDateTime.now().toString(),
                "Ticket updated successfully"
        );

        when(supportService.updateTicket(eq(employeeEmail), eq(102L), any(UpdateTicketRequest.class))).thenReturn(mockUpdateResp);

        String updateJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(updateReq);
        System.out.println("\n[HTTP REQUEST] PUT /api/v1/my-support/tickets/102");
        System.out.println("Headers: Authorization = Bearer employee-mock-token");
        System.out.println("Body:\n" + updateJson);

        MvcResult updateResult = mockMvc.perform(put("/api/v1/my-support/tickets/102")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(102))
                .andExpect(jsonPath("$.data.ticketNumber").value("SUP-2026-000102"))
                .andExpect(jsonPath("$.data.subject").value("Workstation Monitor Flickering (Dell 24-inch)"))
                .andExpect(jsonPath("$.data.description").value("Primary Dell 24-inch monitor flickers and turns black. Connection cables checked and swapped."))
                .andExpect(jsonPath("$.data.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.message").value("Ticket updated successfully"))
                .andReturn();

        String updateResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(updateResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 200 OK");
        System.out.println("Body:\n" + updateResponseJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 5: EMPLOYEE CLOSES TICKET
        CloseTicketRequest closeReq = new CloseTicketRequest();
        closeReq.setRating(5);
        closeReq.setFeedback("Superb support, agent replaced the HDMI cable and resolved the issue.");

        CloseTicketResponse mockCloseResp = new CloseTicketResponse(
                102L,
                "SUP-2026-000102",
                "CLOSED",
                LocalDateTime.now().toString(),
                5,
                closeReq.getFeedback(),
                "Ticket closed successfully"
        );

        when(supportService.closeTicket(eq(employeeEmail), eq(102L), any(CloseTicketRequest.class))).thenReturn(mockCloseResp);

        String closeJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(closeReq);
        System.out.println("\n[HTTP REQUEST] PATCH /api/v1/my-support/tickets/102/close");
        System.out.println("Headers: Authorization = Bearer employee-mock-token");
        System.out.println("Body:\n" + closeJson);

        MvcResult closeResult = mockMvc.perform(patch("/api/v1/my-support/tickets/102/close")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(closeJson))
                .andReturn();

        String closeResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(closeResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 200 OK");
        System.out.println("Body:\n" + closeResponseJson);
        System.out.println("================================================================================");
    }
}
