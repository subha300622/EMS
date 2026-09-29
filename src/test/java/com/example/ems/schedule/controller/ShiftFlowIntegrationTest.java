package com.example.ems.schedule.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.schedule.dto.*;
import com.example.ems.schedule.entity.MyScheduleChangeRequest;
import com.example.ems.schedule.entity.MyShift;
import com.example.ems.schedule.entity.ShiftType;
import com.example.ems.schedule.repository.MyScheduleChangeRequestRepository;
import com.example.ems.schedule.repository.MyShiftRepository;
import com.example.ems.schedule.service.MyScheduleService;
import com.example.ems.schedule.service.TeamScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ShiftFlowIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MyShiftRepository shiftRepository;

    @Autowired
    private MyScheduleChangeRequestRepository changeRequestRepository;

    @Autowired
    private TeamScheduleService teamScheduleService;

    @Autowired
    private MyScheduleService myScheduleService;

    private Employee manager;
    private Employee employee;
    private User managerUser;
    private User employeeUser;

    @BeforeEach
    public void setUp() {
        // Seed default templates if not present
        myScheduleService.seedScheduleData("seed.test@company.com");

        Role mgrRole = roleRepository.findByName("MANAGER").orElseGet(() -> {
            Role r = new Role();
            r.setName("MANAGER");
            return roleRepository.save(r);
        });

        Role empRole = roleRepository.findByName("EMPLOYEE").orElseGet(() -> {
            Role r = new Role();
            r.setName("EMPLOYEE");
            return roleRepository.save(r);
        });

        manager = new Employee();
        manager.setFullName("Sarah Manager");
        manager.setEmail("sarah.mgr@company.com");
        manager.setEmployeeId("MGR100");
        manager.setDesignation("Engineering Manager");
        manager.setDepartment("Engineering");
        manager = employeeRepository.save(manager);

        managerUser = new User();
        managerUser.setWorkEmail("sarah.mgr@company.com");
        managerUser.setFullName("Sarah Manager");
        managerUser.setRole(mgrRole);
        managerUser = userRepository.save(managerUser);

        employee = new Employee();
        employee.setFullName("John Shift");
        employee.setEmail("john.shift@company.com");
        employee.setEmployeeId("EMP100");
        employee.setManager(manager);
        employee.setDesignation("Software Engineer");
        employee.setDepartment("Engineering");
        employee = employeeRepository.save(employee);

        employeeUser = new User();
        employeeUser.setWorkEmail("john.shift@company.com");
        employeeUser.setFullName("John Shift");
        employeeUser.setRole(empRole);
        employeeUser = userRepository.save(employeeUser);
    }

    @Test
    public void testCompleteShiftLifecycle() {
        LocalDate testDate = LocalDate.of(2026, 7, 10);

        // 1. Manager assigns MORNING shift to Employee
        AssignShiftRequest assignReq = new AssignShiftRequest();
        assignReq.setEmployeeId(employee.getId());
        assignReq.setDate(testDate);
        assignReq.setShiftType(ShiftType.MORNING);

        teamScheduleService.assignShift(assignReq);

        // Verify shift assigned in database
        MyShift assignedShift = shiftRepository.findByEmployeeEmailAndDate(employee.getEmail(), testDate)
                .orElseThrow(() -> new AssertionError("Shift not found after assignment"));
        assertEquals("ASSIGNED", assignedShift.getStatus());
        assertEquals("MORNING_SHIFT", assignedShift.getTemplate().getName());

        // 2. Employee requests a shift change from MORNING (104) to EVENING (102)
        ChangeRequestPayload changePayload = new ChangeRequestPayload();
        changePayload.setCurrentShiftId(104L); // MORNING_SHIFT
        changePayload.setRequestedShiftId(102L); // EVENING_SHIFT
        changePayload.setRequestedDate(testDate.toString());
        changePayload.setReason("Doctor Appointment in the morning");

        ChangeRequestResponse changeResp = myScheduleService.createChangeRequest(employee.getEmail(), changePayload);

        assertNotNull(changeResp.getRequestId());
        assertEquals("PENDING_MANAGER_APPROVAL", changeResp.getStatus());

        // Verify change request stored in DB
        MyScheduleChangeRequest changeReqDb = changeRequestRepository.findById(changeResp.getRequestId())
                .orElseThrow();
        assertEquals("PENDING_MANAGER_APPROVAL", changeReqDb.getStatus());
        assertEquals(employee.getId(), changeReqDb.getEmployee().getId());

        // 3. Manager approves the shift swap request
        teamScheduleService.approveSwap(changeResp.getRequestId());

        // Verify swap request status updated to APPROVED
        MyScheduleChangeRequest updatedReq = changeRequestRepository.findById(changeResp.getRequestId())
                .orElseThrow();
        assertEquals("APPROVED", updatedReq.getStatus());

        // Verify assigned shift updated to EVENING_SHIFT
        MyShift updatedShift = shiftRepository.findByEmployeeEmailAndDate(employee.getEmail(), testDate)
                .orElseThrow();
        assertEquals("EVENING_SHIFT", updatedShift.getTemplate().getName());

        // 4. Employee retrieves shift timeline and policies
        ScheduleTimelineResponse timeline = myScheduleService.getTimeline(employee.getEmail());
        assertNotNull(timeline.getActivities());
        assertTrue(timeline.getActivities().size() >= 2);

        SchedulePoliciesResponse policies = myScheduleService.getPolicies();
        assertNotNull(policies.getPolicy());

        // 5. Manager views team schedule overview & grid
        TeamScheduleResponse teamSchedule = teamScheduleService.getTeamSchedule(
                testDate.minusDays(1), testDate.plusDays(1), null, manager.getId(), 0, 10, managerUser);
        assertNotNull(teamSchedule.getOverview());
        assertNotNull(teamSchedule.getGrid());

        // 6. Manager removes shift assignment by setting ShiftType to NONE
        AssignShiftRequest removeReq = new AssignShiftRequest();
        removeReq.setEmployeeId(employee.getId());
        removeReq.setDate(testDate);
        removeReq.setShiftType(ShiftType.NONE);

        teamScheduleService.assignShift(removeReq);

        // Verify shift is deleted
        assertTrue(shiftRepository.findByEmployeeEmailAndDate(employee.getEmail(), testDate).isEmpty());
    }
}
