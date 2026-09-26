package com.example.ems.schedule.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.schedule.dto.*;
import com.example.ems.schedule.entity.ScheduleStatus;
import com.example.ems.schedule.service.ScheduleManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ScheduleModuleIntegrationTest {

        @Autowired
        private OrganizationRepository organizationRepository;

        @Autowired
        private EmployeeRepository employeeRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private ScheduleManagementService scheduleManagementService;

        private Organization org1;
        private Organization org2;
        private User adminUserOrg1;
        private User adminUserOrg2;
        private Employee empOrg1;
        private Employee empOrg2;

        @BeforeEach
        public void setUp() {
                Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
                        Role r = new Role();
                        r.setName("ADMIN");
                        return roleRepository.save(r);
                });

                String suffix = UUID.randomUUID().toString().substring(0, 8);

                org1 = new Organization();
                org1.setName("Acme Corp " + suffix);
                org1.setOrganizationCode("ORG-ACME-" + suffix);
                org1.setNormalizedName("acme corp " + suffix);
                org1 = organizationRepository.save(org1);

                org2 = new Organization();
                org2.setName("Stark Industries " + suffix);
                org2.setOrganizationCode("ORG-STARK-" + suffix);
                org2.setNormalizedName("stark industries " + suffix);
                org2 = organizationRepository.save(org2);

                adminUserOrg1 = new User();
                adminUserOrg1.setWorkEmail("admin-" + suffix + "@acme.com");
                adminUserOrg1.setFullName("Acme Admin");
                adminUserOrg1.setRole(adminRole);
                adminUserOrg1.setOrganization(org1);
                adminUserOrg1 = userRepository.save(adminUserOrg1);

                adminUserOrg2 = new User();
                adminUserOrg2.setWorkEmail("admin-" + suffix + "@stark.com");
                adminUserOrg2.setFullName("Stark Admin");
                adminUserOrg2.setRole(adminRole);
                adminUserOrg2.setOrganization(org2);
                adminUserOrg2 = userRepository.save(adminUserOrg2);

                empOrg1 = new Employee();
                empOrg1.setFullName("Alice Acme");
                empOrg1.setEmail("alice-" + suffix + "@acme.com");
                empOrg1.setEmployeeId("EMP-ACME-" + suffix);
                empOrg1.setOrganization(org1);
                empOrg1 = employeeRepository.save(empOrg1);

                empOrg2 = new Employee();
                empOrg2.setFullName("Bob Stark");
                empOrg2.setEmail("bob-" + suffix + "@stark.com");
                empOrg2.setEmployeeId("EMP-STARK-" + suffix);
                empOrg2.setOrganization(org2);
                empOrg2 = employeeRepository.save(empOrg2);
        }

        @Test
        public void testValidScheduleCreationAndRetrieval() {
                ScheduleCreateRequest req = new ScheduleCreateRequest(
                                empOrg1.getEmployeeId(),
                                "2026-08-25",
                                "09:00",
                                "17:00",
                                "Building A",
                                "Regular shift");

                ScheduleDto created = scheduleManagementService.createSchedule(adminUserOrg1, req);
                assertNotNull(created.getScheduleId());
                assertTrue(created.getScheduleId().startsWith("SCH-"));
                assertEquals(empOrg1.getEmployeeId(), created.getEmployeeId());
                assertEquals("Alice Acme", created.getEmployeeName());
                assertEquals("2026-08-25", created.getDate());
                assertEquals("09:00", created.getStartTime());
                assertEquals("17:00", created.getEndTime());
                assertEquals(ScheduleStatus.SCHEDULED, created.getStatus());

                // Fetch single schedule
                ScheduleDto fetched = scheduleManagementService.getScheduleById(adminUserOrg1, created.getScheduleId());
                assertEquals(created.getScheduleId(), fetched.getScheduleId());
        }

        @Test
        public void testStartTimeMustBeBeforeEndTimeValidation() {
                ScheduleCreateRequest req = new ScheduleCreateRequest(
                                empOrg1.getEmployeeId(),
                                "2026-08-25",
                                "18:00",
                                "09:00", // Invalid: start > end
                                "Office",
                                "Invalid time");

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                () -> scheduleManagementService.createSchedule(adminUserOrg1, req));
                assertTrue(ex.getMessage().contains("startTime must be before endTime"));
        }

        @Test
        public void testAdjacentShiftsAllowed() {
                // Shift 1: 09:00 - 12:00
                ScheduleCreateRequest req1 = new ScheduleCreateRequest(
                                empOrg1.getEmployeeId(), "2026-08-25", "09:00", "12:00", "Office", "Morning shift");
                scheduleManagementService.createSchedule(adminUserOrg1, req1);

                // Shift 2: 12:00 - 18:00 (Adjacent start time equals previous end time ->
                // Should pass!)
                ScheduleCreateRequest req2 = new ScheduleCreateRequest(
                                empOrg1.getEmployeeId(), "2026-08-25", "12:00", "18:00", "Office", "Afternoon shift");
                ScheduleDto created2 = scheduleManagementService.createSchedule(adminUserOrg1, req2);
                assertNotNull(created2.getScheduleId());
        }

        @Test
        public void testOverlappingShiftsRejected() {
                // Shift 1: 09:00 - 12:00
                ScheduleCreateRequest req1 = new ScheduleCreateRequest(
                                empOrg1.getEmployeeId(), "2026-08-25", "09:00", "12:00", "Office", "Shift 1");
                scheduleManagementService.createSchedule(adminUserOrg1, req1);

                // Shift 2: 11:00 - 14:00 (Overlaps 11:00-12:00 -> Should throw
                // IllegalStateException)
                ScheduleCreateRequest req2 = new ScheduleCreateRequest(
                                empOrg1.getEmployeeId(), "2026-08-25", "11:00", "14:00", "Office", "Shift 2");
                IllegalStateException ex = assertThrows(IllegalStateException.class,
                                () -> scheduleManagementService.createSchedule(adminUserOrg1, req2));
                assertTrue(ex.getMessage().contains("Schedule overlaps"));
        }

        @Test
        public void testUpdateScheduleSelfExclusion() {
                ScheduleCreateRequest req = new ScheduleCreateRequest(
                                empOrg1.getEmployeeId(), "2026-08-25", "09:00", "17:00", "Office", "Initial shift");
                ScheduleDto created = scheduleManagementService.createSchedule(adminUserOrg1, req);

                // Update without changing time (location update) -> should succeed without
                // false overlap error
                ScheduleUpdateRequest updateReq = new ScheduleUpdateRequest(
                                "2026-08-25", "09:00", "17:00", ScheduleStatus.IN_PROGRESS, "Remote / Home",
                                "Updated notes");
                ScheduleDto updated = scheduleManagementService.updateSchedule(adminUserOrg1, created.getScheduleId(),
                                updateReq);
                assertEquals(ScheduleStatus.IN_PROGRESS, updated.getStatus());
                assertEquals("Remote / Home", updated.getLocation());
        }

        @Test
        public void testMultiTenantOrganizationIsolation() {
                ScheduleCreateRequest req = new ScheduleCreateRequest(
                                empOrg1.getEmployeeId(), "2026-08-25", "09:00", "17:00", "Acme HQ", "Acme schedule");
                ScheduleDto created = scheduleManagementService.createSchedule(adminUserOrg1, req);

                // Admin from Org 2 attempts to fetch Org 1 schedule -> Should throw
                // IllegalArgumentException (Not Found in Org 2)
                assertThrows(IllegalArgumentException.class, () -> scheduleManagementService
                                .getScheduleById(adminUserOrg2, created.getScheduleId()));

                // Admin from Org 2 attempts to update Org 1 schedule -> Should throw
                // IllegalArgumentException
                ScheduleUpdateRequest updateReq = new ScheduleUpdateRequest(
                                "2026-08-25", "09:00", "17:00", ScheduleStatus.CANCELLED, "Hacked Location",
                                "Malicious notes");
                assertThrows(IllegalArgumentException.class, () -> scheduleManagementService
                                .updateSchedule(adminUserOrg2, created.getScheduleId(), updateReq));

                // Admin from Org 2 attempts to delete Org 1 schedule -> Should throw
                // IllegalArgumentException
                assertThrows(IllegalArgumentException.class,
                                () -> scheduleManagementService.deleteSchedule(adminUserOrg2, created.getScheduleId()));
        }

        @Test
        public void testPaginatedListFiltering() {
                // Create 3 schedules across different dates
                scheduleManagementService.createSchedule(adminUserOrg1, new ScheduleCreateRequest(
                                empOrg1.getEmployeeId(), "2026-08-01", "09:00", "12:00", "Site 1", "Note 1"));
                scheduleManagementService.createSchedule(adminUserOrg1, new ScheduleCreateRequest(
                                empOrg1.getEmployeeId(), "2026-08-15", "09:00", "12:00", "Site 2", "Note 2"));
                scheduleManagementService.createSchedule(adminUserOrg1, new ScheduleCreateRequest(
                                empOrg1.getEmployeeId(), "2026-08-30", "09:00", "12:00", "Site 3", "Note 3"));

                // Filter date range 2026-08-10 to 2026-08-31
                ScheduleListResponse listResp = scheduleManagementService.getSchedules(
                                adminUserOrg1, "2026-08-10", "2026-08-31", empOrg1.getEmployeeId(), null, null, null, 0,
                                10);

                assertEquals(2, listResp.getTotalElements());
                assertEquals(2, listResp.getContent().size());
        }

        @Test
        public void testGetSchedulesByEmployee() {
                scheduleManagementService.createSchedule(adminUserOrg1, new ScheduleCreateRequest(
                                empOrg1.getEmployeeId(), "2026-08-25", "09:00", "17:00", "Office",
                                "Employee schedule"));

                List<ScheduleDto> list = scheduleManagementService.getSchedulesByEmployee(adminUserOrg1, empOrg1.getEmployeeId());
                assertEquals(1, list.size());
                assertEquals(empOrg1.getEmployeeId(), list.get(0).getEmployeeId());
        }
}
