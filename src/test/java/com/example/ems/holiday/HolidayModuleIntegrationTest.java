package com.example.ems.holiday;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.holiday.entity.Holiday;
import com.example.ems.holiday.entity.HolidayStatus;
import com.example.ems.holiday.repository.HolidayRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class HolidayModuleIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private com.example.ems.holiday.service.HolidayAttendanceWorker holidayAttendanceWorker;

    @Autowired
    private com.example.ems.attendance.repository.AttendanceRepository attendanceRepository;

    @Autowired
    private JwtService jwtService;

    private Organization testOrg;
    private Employee emp1;
    private User user1;
    private String token1;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        testOrg = new Organization();
        testOrg.setName("Holiday Test Org");
        testOrg.setOrganizationCode("ORG-HOL-" + UUID.randomUUID().toString().substring(0, 8));
        testOrg = organizationRepository.save(testOrg);

        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("SUPER_ADMIN");
                    r.setDescription("Super Admin Role");
                    return roleRepository.save(r);
                });

        emp1 = new Employee();
        emp1.setFirstName("Admin");
        emp1.setLastName("User");
        emp1.setFullName("Admin User");
        emp1.setEmployeeId("EMP-HOL-001");
        emp1.setEmail("admin@holidaytest.com");
        emp1.setOrganization(testOrg);
        emp1.setStatus("ACTIVE");
        emp1 = employeeRepository.save(emp1);

        user1 = new User();
        user1.setUserId(emp1.getEmployeeId());
        user1.setWorkEmail(emp1.getEmail());
        user1.setPassword("$2a$10$7Q9b9K1l1l1l1l1l1l1l1u");
        user1.setRole(superAdminRole);
        user1.setOrganization(testOrg);
        user1 = userRepository.save(user1);

        token1 = jwtService.generateAccessToken(user1.getUserId(), user1.getWorkEmail(), user1.getRole().getName());
    }

    @Test
    public void testCreateHoliday_Success() throws Exception {
        String payload = """
            {
              "name": "Independence Day",
              "holidayDate": "2026-08-15",
              "description": "National Independence Day"
            }
            """;

        mockMvc.perform(post("/api/v1/holidays")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.holidayId", startsWith("HOL-")))
                .andExpect(jsonPath("$.data.name", is("Independence Day")))
                .andExpect(jsonPath("$.data.holidayDate", is("2026-08-15")))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));
    }

    @Test
    public void testCreateHoliday_DuplicateDate_Fails() throws Exception {
        String payload = """
            {
              "name": "Republic Day",
              "holidayDate": "2026-01-26",
              "description": "National Republic Day"
            }
            """;

        mockMvc.perform(post("/api/v1/holidays")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated());

        // Attempt duplicate holiday on same date
        mockMvc.perform(post("/api/v1/holidays")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("HOLIDAY_001")));
    }

    @Test
    public void testGetHolidayById_Success() throws Exception {
        Holiday h = new Holiday();
        h.setHolidayId("HOL-10001");
        h.setOrganizationId(testOrg.getId());
        h.setName("New Year");
        h.setHolidayDate(LocalDate.of(2026, 1, 1));
        h.setDescription("New Year Holiday");
        h.setStatus(HolidayStatus.ACTIVE);
        holidayRepository.save(h);

        mockMvc.perform(get("/api/v1/holidays/HOL-10001")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.holidayId", is("HOL-10001")))
                .andExpect(jsonPath("$.data.name", is("New Year")));
    }

    @Test
    public void testGetHolidayById_NotFound_Fails() throws Exception {
        mockMvc.perform(get("/api/v1/holidays/HOL-99999")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is("HOLIDAY_404")));
    }

    @Test
    public void testListHolidays_WithFilter() throws Exception {
        Holiday h1 = new Holiday();
        h1.setHolidayId("HOL-20001");
        h1.setOrganizationId(testOrg.getId());
        h1.setName("Republic Day");
        h1.setHolidayDate(LocalDate.of(2026, 1, 26));
        holidayRepository.save(h1);

        Holiday h2 = new Holiday();
        h2.setHolidayId("HOL-20002");
        h2.setOrganizationId(testOrg.getId());
        h2.setName("Independence Day");
        h2.setHolidayDate(LocalDate.of(2026, 8, 15));
        holidayRepository.save(h2);

        mockMvc.perform(get("/api/v1/holidays?year=2026&page=0&size=20")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements", is(2)));
    }

    @Test
    public void testUpdateHoliday_Success() throws Exception {
        Holiday h = new Holiday();
        h.setHolidayId("HOL-30001");
        h.setOrganizationId(testOrg.getId());
        h.setName("Labor Day");
        h.setHolidayDate(LocalDate.of(2026, 5, 1));
        h.setStatus(HolidayStatus.ACTIVE);
        holidayRepository.save(h);

        String updatePayload = """
            {
              "name": "International Workers Day",
              "holidayDate": "2026-05-01",
              "description": "Labor & Workers Day"
            }
            """;

        mockMvc.perform(put("/api/v1/holidays/HOL-30001")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("International Workers Day")))
                .andExpect(jsonPath("$.data.description", is("Labor & Workers Day")));
    }

    @Test
    public void testDeleteHoliday_SoftDelete_Success() throws Exception {
        Holiday h = new Holiday();
        h.setHolidayId("HOL-40001");
        h.setOrganizationId(testOrg.getId());
        h.setName("Festival");
        h.setHolidayDate(LocalDate.of(2026, 11, 1));
        h.setStatus(HolidayStatus.ACTIVE);
        holidayRepository.save(h);

        mockMvc.perform(delete("/api/v1/holidays/HOL-40001")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("INACTIVE")));

        Holiday updated = holidayRepository.findByHolidayIdAndOrganizationId("HOL-40001", testOrg.getId()).orElseThrow();
        assertEquals(HolidayStatus.INACTIVE, updated.getStatus());
    }

    @Test
    public void testCheckHoliday() throws Exception {
        Holiday h = new Holiday();
        h.setHolidayId("HOL-50001");
        h.setOrganizationId(testOrg.getId());
        h.setName("Independence Day");
        h.setHolidayDate(LocalDate.of(2026, 8, 15));
        h.setStatus(HolidayStatus.ACTIVE);
        holidayRepository.save(h);

        // Check date that IS a holiday
        mockMvc.perform(get("/api/v1/holidays/check?date=2026-08-15")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isHoliday", is(true)))
                .andExpect(jsonPath("$.data.holidayId", is("HOL-50001")))
                .andExpect(jsonPath("$.data.holidayName", is("Independence Day")));

        // Check date that is NOT a holiday
        mockMvc.perform(get("/api/v1/holidays/check?date=2026-08-17")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isHoliday", is(false)))
                .andExpect(jsonPath("$.data.holidayId", nullValue()))
                .andExpect(jsonPath("$.data.holidayName", nullValue()));
    }

    @Test
    public void testGetHolidayCalendar() throws Exception {
        Holiday h = new Holiday();
        h.setHolidayId("HOL-60001");
        h.setOrganizationId(testOrg.getId());
        h.setName("Republic Day");
        h.setHolidayDate(LocalDate.of(2026, 1, 26));
        h.setStatus(HolidayStatus.ACTIVE);
        holidayRepository.save(h);

        mockMvc.perform(get("/api/v1/holidays/calendar?year=2026")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.year", is(2026)))
                .andExpect(jsonPath("$.data.holidays", hasSize(1)))
                .andExpect(jsonPath("$.data.holidays[0].holidayId", is("HOL-60001")))
                .andExpect(jsonPath("$.data.holidays[0].name", is("Republic Day")));
    }

    @Test
    public void testHolidayAttendanceWorker_ActiveEmployeesOnlyAndPrecedence() {
        LocalDate holidayDate = LocalDate.of(2026, 8, 15);

        Holiday h = new Holiday();
        h.setHolidayId("HOL-70001");
        h.setOrganizationId(testOrg.getId());
        h.setName("Independence Day");
        h.setHolidayDate(holidayDate);
        h.setStatus(HolidayStatus.ACTIVE);
        holidayRepository.save(h);

        // Employee 2 (INACTIVE)
        Employee empInactive = new Employee();
        empInactive.setFirstName("Inactive");
        empInactive.setLastName("User");
        empInactive.setFullName("Inactive User");
        empInactive.setEmployeeId("EMP-HOL-002");
        empInactive.setEmail("inactive@holidaytest.com");
        empInactive.setOrganization(testOrg);
        empInactive.setStatus("INACTIVE");
        employeeRepository.save(empInactive);

        // Pre-existing finalized attendance for emp1 (PRESENT)
        com.example.ems.attendance.entity.Attendance existingAtt = new com.example.ems.attendance.entity.Attendance();
        existingAtt.setEmployee(emp1);
        existingAtt.setDate(holidayDate);
        existingAtt.setStatus("PRESENT");
        existingAtt.setAttendanceType("REGULAR");
        attendanceRepository.save(existingAtt);

        // Run worker for today/holiday date
        holidayAttendanceWorker.processDailyHolidays(holidayDate);

        // Emp1 attendance must remain PRESENT (precedence rule)
        com.example.ems.attendance.entity.Attendance emp1Att = attendanceRepository.findByEmployeeIdAndDate(emp1.getId(), holidayDate).orElseThrow();
        assertEquals("PRESENT", emp1Att.getStatus());

        // EmpInactive must NOT have attendance created (active employee filter)
        assertFalse(attendanceRepository.existsByEmployeeIdAndDate(empInactive.getId(), holidayDate));
    }

    @Test
    public void testDeactivatedHoliday_HistoricalAttendanceUnchanged() throws Exception {
        LocalDate holidayDate = LocalDate.of(2026, 12, 25);

        Holiday h = new Holiday();
        h.setHolidayId("HOL-80001");
        h.setOrganizationId(testOrg.getId());
        h.setName("Christmas");
        h.setHolidayDate(holidayDate);
        h.setStatus(HolidayStatus.ACTIVE);
        holidayRepository.save(h);

        // Worker runs and marks HOLIDAY
        holidayAttendanceWorker.processDailyHolidays(holidayDate);

        com.example.ems.attendance.entity.Attendance emp1AttBefore = attendanceRepository.findByEmployeeIdAndDate(emp1.getId(), holidayDate).orElseThrow();
        assertEquals("HOLIDAY", emp1AttBefore.getStatus());

        // Admin deactivates holiday (soft delete)
        mockMvc.perform(delete("/api/v1/holidays/HOL-80001")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("INACTIVE")));

        // Historical attendance record remains HOLIDAY and unchanged
        com.example.ems.attendance.entity.Attendance emp1AttAfter = attendanceRepository.findByEmployeeIdAndDate(emp1.getId(), holidayDate).orElseThrow();
        assertEquals("HOLIDAY", emp1AttAfter.getStatus());
    }
}
