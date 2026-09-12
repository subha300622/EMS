package com.example.ems.leave;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.MyTeam;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.MyTeamRepository;
import com.example.ems.leave.dto.LeaveCalendarEventDto;
import com.example.ems.leave.dto.LeaveRequest;
import com.example.ems.leave.dto.LeaveTypeRequest;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import com.example.ems.leave.repository.LeavePolicyRepository;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.repository.LeaveTypeRepository;
import com.example.ems.leave.service.LeaveBalanceService;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LeaveCalendarIntegrationTest {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MyTeamRepository myTeamRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    private Organization orgA;
    private Organization orgB;

    private Employee empA1; // Org A, Dept: Engineering, Team: Backend
    private Employee empA2; // Org A, Dept: Engineering, Team: Frontend
    private Employee empA3; // Org A, Dept: HR, Team: Recruitment
    private Employee empB1; // Org B, Dept: Engineering

    private MyTeam backendTeam;
    private MyTeam frontendTeam;

    private LeaveType leaveTypeA;

    @BeforeEach
    public void setUp() {
        leaveRepository.deleteAll();
        leaveBalanceRepository.deleteAll();
        leavePolicyRepository.deleteAll();
        leaveTypeRepository.deleteAll();

        // 1. Setup Organizations
        orgA = new Organization();
        orgA.setName("Org Alpha");
        orgA.setOrganizationCode("ORG-ALPHA");
        orgA = organizationRepository.save(orgA);

        orgB = new Organization();
        orgB.setName("Org Beta");
        orgB.setOrganizationCode("ORG-BETA");
        orgB = organizationRepository.save(orgB);

        // 2. Setup Teams for Org A
        backendTeam = new MyTeam();
        backendTeam.setTeamName("Backend Team");
        backendTeam = myTeamRepository.save(backendTeam);

        frontendTeam = new MyTeam();
        frontendTeam.setTeamName("Frontend Team");
        frontendTeam = myTeamRepository.save(frontendTeam);

        // 3. Setup Employees
        empA1 = new Employee();
        empA1.setEmployeeId("EMP-A1");
        empA1.setFirstName("Alice");
        empA1.setLastName("Alpha");
        empA1.setEmail("alice@alpha.com");
        empA1.setDepartment("Engineering");
        empA1.setTeam(backendTeam);
        empA1.setOrganization(orgA);
        empA1 = employeeRepository.save(empA1);

        empA2 = new Employee();
        empA2.setEmployeeId("EMP-A2");
        empA2.setFirstName("Bob");
        empA2.setLastName("Alpha");
        empA2.setEmail("bob@alpha.com");
        empA2.setDepartment("Engineering");
        empA2.setTeam(frontendTeam);
        empA2.setOrganization(orgA);
        empA2 = employeeRepository.save(empA2);

        empA3 = new Employee();
        empA3.setEmployeeId("EMP-A3");
        empA3.setFirstName("Charlie");
        empA3.setLastName("Alpha");
        empA3.setEmail("charlie@alpha.com");
        empA3.setDepartment("HR");
        empA3.setOrganization(orgA);
        empA3 = employeeRepository.save(empA3);

        empB1 = new Employee();
        empB1.setEmployeeId("EMP-B1");
        empB1.setFirstName("David");
        empB1.setLastName("Beta");
        empB1.setEmail("david@beta.com");
        empB1.setDepartment("Engineering");
        empB1.setOrganization(orgB);
        empB1 = employeeRepository.save(empB1);

        // 4. Setup Leave Type & Balances
        leaveTypeA = leaveTypeRepository.findAll().stream().findFirst().orElseGet(() -> {
            LeaveTypeRequest typeReq = new LeaveTypeRequest();
            typeReq.setName("Annual Leave Calendar Test");
            typeReq.setDefaultDays(20);
            return leaveService.createLeaveType(empA1, typeReq);
        });

        leaveBalanceService.getOrCreateBalance(empA1, leaveTypeA, 2026);
        leaveBalanceService.getOrCreateBalance(empA2, leaveTypeA, 2026);
        leaveBalanceService.getOrCreateBalance(empA3, leaveTypeA, 2026);
    }

    @Test
    @DisplayName("Verify Default Status Returns APPROVED + PENDING (Excludes REJECTED & CANCELLED)")
    public void testDefaultStatusFiltering() {
        // Alice applies leave 1 (Approved)
        LeaveRequest r1 = new LeaveRequest();
        r1.setLeaveTypeId(leaveTypeA.getId());
        r1.setStartDate(LocalDate.of(2026, 9, 1));
        r1.setEndDate(LocalDate.of(2026, 9, 3));
        Leave l1 = leaveService.applyLeave(empA1, r1);
        leaveService.approveLeave(l1.getId(), empA1);

        // Bob applies leave 2 (Pending)
        LeaveRequest r2 = new LeaveRequest();
        r2.setLeaveTypeId(leaveTypeA.getId());
        r2.setStartDate(LocalDate.of(2026, 9, 5));
        r2.setEndDate(LocalDate.of(2026, 9, 7));
        Leave l2 = leaveService.applyLeave(empA2, r2);

        // Charlie applies leave 3 (Rejected)
        LeaveRequest r3 = new LeaveRequest();
        r3.setLeaveTypeId(leaveTypeA.getId());
        r3.setStartDate(LocalDate.of(2026, 9, 10));
        r3.setEndDate(LocalDate.of(2026, 9, 12));
        Leave l3 = leaveService.applyLeave(empA3, r3);
        leaveService.rejectLeave(l3.getId(), empA1);

        // When querying calendar without status filter:
        List<LeaveCalendarEventDto> events = leaveService.getLeaveCalendarEvents(
                orgA.getId(), null, null, null, null, null, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        // Should return Approved (l1) and Pending (l2), but NOT Rejected (l3)
        assertEquals(2, events.size());
        assertTrue(events.stream().anyMatch(e -> e.getLeaveId().equals(l1.getId())));
        assertTrue(events.stream().anyMatch(e -> e.getLeaveId().equals(l2.getId())));
        assertFalse(events.stream().anyMatch(e -> e.getLeaveId().equals(l3.getId())));
    }

    @Test
    @DisplayName("Verify Employee Calendar Scope")
    public void testEmployeeCalendarScope() {
        LeaveRequest r1 = new LeaveRequest();
        r1.setLeaveTypeId(leaveTypeA.getId());
        r1.setStartDate(LocalDate.of(2026, 9, 1));
        r1.setEndDate(LocalDate.of(2026, 9, 3));
        Leave l1 = leaveService.applyLeave(empA1, r1);

        LeaveRequest r2 = new LeaveRequest();
        r2.setLeaveTypeId(leaveTypeA.getId());
        r2.setStartDate(LocalDate.of(2026, 9, 1));
        r2.setEndDate(LocalDate.of(2026, 9, 3));
        leaveService.applyLeave(empA2, r2);

        List<LeaveCalendarEventDto> aliceEvents = leaveService.getEmployeeCalendarEvents(
                orgA.getId(), empA1.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), null);

        assertEquals(1, aliceEvents.size());
        assertEquals(l1.getId(), aliceEvents.get(0).getLeaveId());
        assertEquals("EMP-A1", aliceEvents.get(0).getEmployeeCode());
    }

    @Test
    @DisplayName("Verify Team Calendar Scope")
    public void testTeamCalendarScope() {
        // empA1 is in Backend Team, empA2 is in Frontend Team
        LeaveRequest r1 = new LeaveRequest();
        r1.setLeaveTypeId(leaveTypeA.getId());
        r1.setStartDate(LocalDate.of(2026, 9, 1));
        r1.setEndDate(LocalDate.of(2026, 9, 3));
        Leave l1 = leaveService.applyLeave(empA1, r1);

        LeaveRequest r2 = new LeaveRequest();
        r2.setLeaveTypeId(leaveTypeA.getId());
        r2.setStartDate(LocalDate.of(2026, 9, 1));
        r2.setEndDate(LocalDate.of(2026, 9, 3));
        leaveService.applyLeave(empA2, r2);

        List<LeaveCalendarEventDto> backendEvents = leaveService.getTeamCalendarEvents(
                orgA.getId(), backendTeam.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), null);

        assertEquals(1, backendEvents.size());
        assertEquals(l1.getId(), backendEvents.get(0).getLeaveId());
        assertEquals(backendTeam.getId(), backendEvents.get(0).getTeamId());
    }

    @Test
    @DisplayName("Verify Department Calendar Scope")
    public void testDepartmentCalendarScope() {
        // empA1 and empA2 are in Engineering, empA3 is in HR
        LeaveRequest r1 = new LeaveRequest();
        r1.setLeaveTypeId(leaveTypeA.getId());
        r1.setStartDate(LocalDate.of(2026, 9, 1));
        r1.setEndDate(LocalDate.of(2026, 9, 3));
        Leave l1 = leaveService.applyLeave(empA1, r1);

        LeaveRequest r3 = new LeaveRequest();
        r3.setLeaveTypeId(leaveTypeA.getId());
        r3.setStartDate(LocalDate.of(2026, 9, 1));
        r3.setEndDate(LocalDate.of(2026, 9, 3));
        leaveService.applyLeave(empA3, r3);

        List<LeaveCalendarEventDto> engEvents = leaveService.getDepartmentCalendarEvents(
                orgA.getId(), "Engineering", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), null);

        assertEquals(1, engEvents.size());
        assertEquals(l1.getId(), engEvents.get(0).getLeaveId());
        assertEquals("Engineering", engEvents.get(0).getDepartment());
    }

    @Test
    @DisplayName("Verify Date Range Overlap Logic")
    public void testDateRangeOverlapLogic() {
        // Leave from Sep 15 to Oct 5
        LeaveRequest r = new LeaveRequest();
        r.setLeaveTypeId(leaveTypeA.getId());
        r.setStartDate(LocalDate.of(2026, 9, 15));
        r.setEndDate(LocalDate.of(2026, 10, 5));
        leaveService.applyLeave(empA1, r);

        // Querying Sep 1 to Sep 30 should include this leave because it overlaps Sep 15
        // - Sep 30
        List<LeaveCalendarEventDto> sepEvents = leaveService.getLeaveCalendarEvents(
                orgA.getId(), empA1.getId(), null, null, null, null, LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30));
        assertEquals(1, sepEvents.size());

        // Querying Aug 1 to Aug 31 should NOT include this leave
        List<LeaveCalendarEventDto> augEvents = leaveService.getLeaveCalendarEvents(
                orgA.getId(), empA1.getId(), null, null, null, null, LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31));
        assertEquals(0, augEvents.size());
    }

    @Test
    @DisplayName("Verify Organization Tenant Isolation")
    public void testOrganizationTenantIsolation() {
        LeaveRequest r1 = new LeaveRequest();
        r1.setLeaveTypeId(leaveTypeA.getId());
        r1.setStartDate(LocalDate.of(2026, 9, 1));
        r1.setEndDate(LocalDate.of(2026, 9, 3));
        leaveService.applyLeave(empA1, r1);

        // Querying Org B for the same dates should return 0 events
        List<LeaveCalendarEventDto> orgBEvents = leaveService.getLeaveCalendarEvents(
                orgB.getId(), null, null, null, null, null, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));
        assertEquals(0, orgBEvents.size());
    }
}
