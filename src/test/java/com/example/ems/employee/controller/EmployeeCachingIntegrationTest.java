package com.example.ems.employee.controller;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.service.EmployeeService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Service-level Integration tests for the Employee caching layer.
 * Verifies that the caching facade operates correctly on reads, updates, and invalidations
 * by bypassing controller security filters.
 *
 * <p>Intentionally non-transactional at the test class level to allow service transactions
 * to commit, which triggers transaction-aware event listeners.</p>
 */
@SpringBootTest
public class EmployeeCachingIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;


    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Employee testEmployee;
    private String uniqueEmail;

    @BeforeEach
    public void setup() {
        uniqueEmail = "john.caching." + System.nanoTime() + "@example.com";
        String uniqueEmpId = "EMP-CACH-" + System.nanoTime();

        Employee employee = new Employee();
        employee.setFullName("John Caching");
        employee.setEmail(uniqueEmail);
        employee.setEmployeeId(uniqueEmpId);
        employee.setDepartment("Engineering");
        employee.setDesignation("Software Engineer");
        employee.setAnnualSalary(new BigDecimal("120000.00"));
        employee.setJoiningDate(LocalDate.now());
        employee.setDob(LocalDate.of(1995, 5, 15));
        employee.setGender("Male");
        
        testEmployee = employeeRepository.save(employee);
    }

    @AfterEach
    public void cleanup() {
        if (testEmployee != null && testEmployee.getId() != null) {
            try {
                employeeRepository.deleteById(testEmployee.getId());
            } catch (Exception ignored) {
                // Employee might have already been deleted in test cases
            }
        }
    }

    @Test
    public void testGetEmployeeById_cachesResult() throws Exception {
        // First fetch -> DB load & cache write
        Optional<Employee> opt1 = employeeService.getEmployeeById(testEmployee.getId());
        assertTrue(opt1.isPresent());
        assertEquals("John Caching", opt1.get().getFullName());

        // Wait for async cache write to complete
        TimeUnit.MILLISECONDS.sleep(150);

        // Update directly in DB via SQL to bypass cache eviction and avoid mutating the Java object reference
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.createNativeQuery("UPDATE employees SET full_name = 'Updated In DB Only' WHERE id = :empId")
                    .setParameter("empId", testEmployee.getId())
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();
        });

        // Second fetch -> should hit Cache (L1/L2) and return cached name, NOT the updated DB name
        Optional<Employee> opt2 = employeeService.getEmployeeById(testEmployee.getId());
        assertTrue(opt2.isPresent());
        assertEquals("John Caching", opt2.get().getFullName());
    }

    @Test
    public void testDeleteEmployee_evictsCache() throws Exception {
        // Fetch to cache it
        employeeService.getEmployeeById(testEmployee.getId());
        TimeUnit.MILLISECONDS.sleep(150);

        // Delete via service -> should trigger eviction after commit
        employeeService.deleteEmployee(testEmployee.getId());
        TimeUnit.MILLISECONDS.sleep(150);

        // Fetch again -> should be empty
        Optional<Employee> opt = employeeService.getEmployeeById(testEmployee.getId());
        assertFalse(opt.isPresent());
    }
}
