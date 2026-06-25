package com.example.ems.attendance.service;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AttendanceConcurrencyTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private com.example.ems.attendance.repository.AttendanceLogRepository attendanceLogRepository;

    private Employee employee;

    @BeforeEach
    public void setUp() {
        // Create a unique employee for this test to avoid conflicting with seeded data
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        employee = new Employee();
        employee.setFullName("Concurrency Test Employee");
        employee.setEmail("concurrency." + uniqueId + "@company.com");
        employee.setEmployeeId("EMP-CONC-" + uniqueId);
        employee.setPhone("555-" + uniqueId);
        employee.setGender("MALE");
        employee.setDob(LocalDate.of(1995, 5, 5));
        employee.setAddress("123 Test Street");
        employee.setEmergencyContact("9876543210");
        employee.setDepartment("Engineering");
        employee.setDesignation("Software Engineer");
        employee.setAnnualSalary(BigDecimal.valueOf(90000));
        employee.setJoiningDate(LocalDate.now());
        employee.setLocation("Remote");
        employee.setEmploymentType("FULL_TIME");
        employee.setStatus("ACTIVE");

        employee = employeeRepository.save(employee);
    }

    @AfterEach
    public void tearDown() {
        if (employee != null && employee.getId() != null) {
            attendanceRepository.deleteAll(attendanceRepository.findByEmployeeId(employee.getId()));
            attendanceLogRepository.deleteAll(attendanceLogRepository.findByEmployeeId(employee.getId()));
            employeeRepository.delete(employee);
        }
    }

    @Test
    public void testConcurrentCheckInsResultInSingleRecord() throws InterruptedException, ExecutionException {
        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        List<Future<String>> futures = new ArrayList<>();
        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger collisionCounter = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await(); // wait for start signal to execute simultaneously
                try {
                    attendanceService.checkIn(employee, "Punched in via Concurrency Test");
                    successCounter.incrementAndGet();
                    return "SUCCESS";
                } catch (IllegalArgumentException e) {
                    if ("Already checked in today".equals(e.getMessage())) {
                        collisionCounter.incrementAndGet();
                        return "COLLISION";
                    }
                    return "ERROR: " + e.getMessage();
                } catch (Exception e) {
                    return "ERROR: " + e.getClass().getName() + " - " + e.getMessage();
                } finally {
                    endLatch.countDown();
                }
            }));
        }

        // Trigger concurrent execution
        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        // Output results
        List<String> results = new ArrayList<>();
        for (Future<String> future : futures) {
            results.add(future.get());
        }

        // Verify thread results
        assertEquals(1, successCounter.get(), "Exactly one check-in must succeed");
        assertEquals(threadCount - 1, collisionCounter.get(), "All other concurrent attempts must be caught as collisions");

        // Verify final database state (the absolute truth)
        List<Attendance> savedRecords = attendanceRepository.findByEmployeeId(employee.getId());
        assertEquals(1, savedRecords.size(), "Database must contain exactly ONE attendance record for the employee and date");
        assertEquals("Punched in via Concurrency Test", savedRecords.get(0).getNotes());
    }
}
