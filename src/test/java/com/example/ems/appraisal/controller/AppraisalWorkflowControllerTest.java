package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.*;
import com.example.ems.appraisal.repository.*;
import com.example.ems.appraisal.service.AppraisalService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AppraisalWorkflowControllerTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalCycleRepository cycleRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private AppraisalService appraisalService;

    @Autowired
    private IncrementRepository incrementRepository;


    private Employee employee;
    private Employee manager;
    private AppraisalCycle cycle;
    private Appraisal appraisal;

    @BeforeEach
    public void setup() {
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE");
                    return roleRepository.save(r);
                });

        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("MANAGER");
                    return roleRepository.save(r);
                });

        manager = new Employee();
        manager.setFullName("Test Manager");
        manager.setEmail("manager@workflow.com");
        manager = employeeRepository.save(manager);

        employee = new Employee();
        employee.setFullName("Test Employee");
        employee.setEmail("employee@workflow.com");
        employee.setAnnualSalary(BigDecimal.valueOf(5000));
        employee.setManager(manager);
        employee = employeeRepository.save(employee);

        User user = new User();
        user.setWorkEmail("employee@workflow.com");
        user.setRole(employeeRole);
        userRepository.save(user);

        User managerUser = new User();
        managerUser.setWorkEmail("manager@workflow.com");
        managerUser.setRole(managerRole);
        userRepository.save(managerUser);

        cycle = new AppraisalCycle();
        cycle.setName("Workflow appraisal 2026");
        cycle.setStartDate(LocalDate.now().minusMonths(1));
        cycle.setEndDate(LocalDate.now().plusMonths(6));
        cycle.setStatus("ACTIVE");
        cycle = cycleRepository.save(cycle);

        appraisal = new Appraisal();
        appraisal.setEmployee(employee);
        appraisal.setCycle(cycle);
        appraisal.setStatus(AppraisalStatus.DRAFT);
        appraisal = appraisalRepository.save(appraisal);
    }

    @Test
    public void testDraftToSubmittedTransition() {
        AppraisalResponse response = appraisalService.submitAppraisal(appraisal.getId(), "employee@workflow.com");
        assertNotNull(response);
        assertEquals(AppraisalStatus.SUBMITTED.name(), response.getStatus());

        List<Map<String, Object>> timeline = appraisalService.getTimeline(appraisal.getId());
        assertFalse(timeline.isEmpty());
        assertEquals(AppraisalStatus.SUBMITTED.name(), timeline.get(0).get("state"));
    }

    @Test
    public void testReopenAppraisalSuccessAndRestrictions() {
        appraisalService.submitAppraisal(appraisal.getId(), "employee@workflow.com");
        
        AppraisalResponse response = appraisalService.reopenAppraisal(appraisal.getId(), "manager@workflow.com");
        assertEquals(AppraisalStatus.DRAFT.name(), response.getStatus());

        appraisal.setStatus(AppraisalStatus.FINANCE_APPROVED);
        appraisalRepository.save(appraisal);

        assertThrows(Exception.class, () -> {
            appraisalService.reopenAppraisal(appraisal.getId(), "manager@workflow.com");
        });
    }

    @Test
    public void testSaveDraftAndHistoryAudit() {
        AppraisalSelfReviewRequest draftReq = new AppraisalSelfReviewRequest();
        draftReq.setSelfRating(4.5);
        draftReq.setSelfReview("Self review comment");
        AppraisalResponse response = appraisalService.saveDraft(appraisal.getId(), draftReq, "employee@workflow.com");
        assertEquals(AppraisalStatus.DRAFT.name(), response.getStatus());
        assertEquals(4.5, response.getSelfRating());

        List<Map<String, Object>> history = appraisalService.getHistory(appraisal.getId());
        assertFalse(history.isEmpty());
        assertTrue(history.stream().anyMatch(h -> "selfRating".equals(h.get("fieldName"))));
    }

    @Test
    public void testCycleFreezeBlockRules() {
        cycle.setStatus("FROZEN");
        cycleRepository.save(cycle);

        assertThrows(Exception.class, () -> {
            appraisalService.submitAppraisal(appraisal.getId(), "employee@workflow.com");
        });
    }

    @Test
    public void testDecoupledPayrollExecutionSuccess() {
        appraisal.setStatus(AppraisalStatus.FINANCE_APPROVED);
        appraisalRepository.save(appraisal);

        Increment inc = new Increment();
        inc.setEmployee(employee);
        inc.setAppraisal(appraisal);
        inc.setCurrentSalary(BigDecimal.valueOf(5000));
        inc.setIncrementPercentage(BigDecimal.valueOf(10));
        inc.setIncrementAmount(BigDecimal.valueOf(500));
        inc.setNewSalary(BigDecimal.valueOf(5500));
        inc.setEffectiveDate(LocalDate.now().plusMonths(1));
        inc.setStatus("APPROVED");
        incrementRepository.save(inc);

        SalaryRevision revision = appraisalService.executePayrollDecoupled(appraisal.getId(), "manager@workflow.com");
        assertNotNull(revision);
        assertTrue(BigDecimal.valueOf(5500.0).compareTo(revision.getNewSalary()) == 0);
        
        Appraisal updated = appraisalRepository.findById(appraisal.getId()).get();
        assertEquals(AppraisalStatus.PROCESSED, updated.getStatus());

        // Verify duplicate prevention
        assertThrows(Exception.class, () -> {
            appraisalService.executePayrollDecoupled(appraisal.getId(), "manager@workflow.com");
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBulkApproveItemizedLogs() {
        List<Long> ids = List.of(appraisal.getId(), 999L);
        Map<String, Object> bulkResult = appraisalService.bulkApprove(ids, "manager@workflow.com");
        assertNotNull(bulkResult);
        
        List<Long> success = (List<Long>) bulkResult.get("success");
        List<Map<String, Object>> failed = (List<Map<String, Object>>) bulkResult.get("failed");
        
        assertTrue(success.contains(appraisal.getId()));
        assertFalse(failed.isEmpty());
        assertEquals(999L, failed.get(0).get("id"));
    }

    @Test
    public void testDiscussionCommentsLoop() {
        appraisalService.addComment(appraisal.getId(), "Discussion message", "employee@workflow.com");
        List<Map<String, Object>> comments = appraisalService.getComments(appraisal.getId());
        assertEquals(1, comments.size());
        assertEquals("Discussion message", comments.get(0).get("comment"));
    }
}
