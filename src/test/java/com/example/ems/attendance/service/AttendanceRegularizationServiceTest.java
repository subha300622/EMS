package com.example.ems.attendance.service;

import com.example.ems.attendance.entity.AttendanceRegularization;
import com.example.ems.attendance.repository.AttendanceRegularizationRepository;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class AttendanceRegularizationServiceTest {

    @Mock
    private AttendanceRegularizationRepository regularizationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private AttendanceRegularizationService service;

    private User adminUser;
    private User employeeUser;
    private User normalUserNoPerms;
    private Employee employee;
    private AttendanceRegularization reg1;
    private AttendanceRegularization reg2;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        adminUser = new User();
        adminUser.setWorkEmail("admin@company.com");

        employeeUser = new User();
        employeeUser.setWorkEmail("employee@company.com");

        normalUserNoPerms = new User();
        normalUserNoPerms.setWorkEmail("no-perms@company.com");

        employee = new Employee();
        employee.setId(10L);
        employee.setEmail("employee@company.com");

        reg1 = new AttendanceRegularization();
        reg1.setId(101L);
        reg1.setEmployee(employee);
        reg1.setStatus("PENDING");

        reg2 = new AttendanceRegularization();
        reg2.setId(102L);
        reg2.setEmployee(employee);
        reg2.setStatus("APPROVED");
    }

    @Test
    public void testGetRegularizationsForUser_AdminAccess_All() {
        when(roleService.hasPermission(adminUser.getWorkEmail(), PermissionRegistry.ATTENDANCE_READ)).thenReturn(true);
        when(regularizationRepository.findAll()).thenReturn(List.of(reg1, reg2));

        List<AttendanceRegularization> result = service.getRegularizationsForUser(adminUser, null);

        assertEquals(2, result.size());
        verify(regularizationRepository, times(1)).findAll();
        verifyNoMoreInteractions(regularizationRepository);
    }

    @Test
    public void testGetRegularizationsForUser_AdminAccess_WithStatus() {
        when(roleService.hasPermission(adminUser.getWorkEmail(), PermissionRegistry.ATTENDANCE_MANAGE)).thenReturn(true);
        when(regularizationRepository.findByStatus("PENDING")).thenReturn(List.of(reg1));

        List<AttendanceRegularization> result = service.getRegularizationsForUser(adminUser, "PENDING");

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
        verify(regularizationRepository, times(1)).findByStatus("PENDING");
    }

    @Test
    public void testGetRegularizationsForUser_EmployeeAccess_All() {
        when(roleService.hasPermission(employeeUser.getWorkEmail(), PermissionRegistry.ATTENDANCE_SELF_READ)).thenReturn(true);
        when(employeeRepository.findByEmail(employeeUser.getWorkEmail())).thenReturn(Optional.of(employee));
        when(regularizationRepository.findByEmployeeId(10L)).thenReturn(List.of(reg1, reg2));

        List<AttendanceRegularization> result = service.getRegularizationsForUser(employeeUser, null);

        assertEquals(2, result.size());
        verify(regularizationRepository, times(1)).findByEmployeeId(10L);
    }

    @Test
    public void testGetRegularizationsForUser_EmployeeAccess_WithStatus() {
        when(roleService.hasPermission(employeeUser.getWorkEmail(), PermissionRegistry.EMPLOYEE_ATTENDANCE_READ)).thenReturn(true);
        when(employeeRepository.findByEmail(employeeUser.getWorkEmail())).thenReturn(Optional.of(employee));
        when(regularizationRepository.findByEmployeeIdAndStatus(10L, "APPROVED")).thenReturn(List.of(reg2));

        List<AttendanceRegularization> result = service.getRegularizationsForUser(employeeUser, "APPROVED");

        assertEquals(1, result.size());
        assertEquals(102L, result.get(0).getId());
        verify(regularizationRepository, times(1)).findByEmployeeIdAndStatus(10L, "APPROVED");
    }

    @Test
    public void testGetRegularizationsForUser_EmployeeProfileMissing() {
        when(roleService.hasPermission(employeeUser.getWorkEmail(), PermissionRegistry.ATTENDANCE_SELF_READ)).thenReturn(true);
        when(employeeRepository.findByEmail(employeeUser.getWorkEmail())).thenReturn(Optional.empty());

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            service.getRegularizationsForUser(employeeUser, null);
        });

        assertEquals("Employee profile not found for authenticated user.", exception.getMessage());
        verify(regularizationRepository, never()).findByEmployeeId(anyLong());
    }

    @Test
    public void testGetRegularizationsForUser_NoPermission() {
        when(roleService.hasPermission(eq(normalUserNoPerms.getWorkEmail()), anyString())).thenReturn(false);

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            service.getRegularizationsForUser(normalUserNoPerms, null);
        });

        assertTrue(exception.getMessage().contains("Access Denied"));
        verify(regularizationRepository, never()).findAll();
    }
}
