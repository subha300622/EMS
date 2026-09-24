package com.example.ems.employee.service;

import com.example.ems.config.BaseCacheService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.common.service.HrDashboardCacheService;
import com.example.ems.common.service.ManagerDashboardCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Cache facade for the Employee module, extending {@link BaseCacheService}.
 */
@Service
public class EmployeeCacheService extends BaseCacheService {

    private static final String PREFIX = "ems:%s:employee:v1:";



    @Autowired
    @Lazy
    private HrDashboardCacheService hrDashboardCacheService;

    @Autowired
    @Lazy
    private ManagerDashboardCacheService managerDashboardCacheService;

    // ── Key builders ─────────────────────────────────────────────────────────
    private String keyAll() { return String.format(PREFIX + "all", env); }
    private String keyById(Long id) { return String.format(PREFIX + "id:%d", env, id); }
    private String keyDepartment(String dept) { return String.format(PREFIX + "department:%s", env, dept != null ? dept.toLowerCase() : "none"); }
    private String keyManager(Long managerId) { return String.format(PREFIX + "manager:%d", env, managerId); }
    private String keySearch(String query) { return String.format(PREFIX + "search:%s", env, query != null ? query.trim().toLowerCase() : ""); }
    private String keyTimeline(Long employeeId) { return String.format(PREFIX + "timeline:%d", env, employeeId); }

    // ── Cached GETs ──────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<Employee> getAllEmployees(Supplier<List<Employee>> dbFallback) {
        return (List<Employee>) get(keyAll(), CacheCategory.LIST, List.class, dbFallback);
    }

    public Optional<Employee> getEmployeeById(Long id, Supplier<Optional<Employee>> dbFallback) {
        Employee emp = get(keyById(id), CacheCategory.PROFILE, Employee.class, () -> dbFallback.get().orElse(null));
        return Optional.ofNullable(emp);
    }

    @SuppressWarnings("unchecked")
    public List<Employee> getEmployeesByDepartment(String department, Supplier<List<Employee>> dbFallback) {
        return (List<Employee>) get(keyDepartment(department), CacheCategory.LIST, List.class, dbFallback);
    }

    @SuppressWarnings("unchecked")
    public List<Employee> getEmployeesByManager(Long managerId, Supplier<List<Employee>> dbFallback) {
        return (List<Employee>) get(keyManager(managerId), CacheCategory.LIST, List.class, dbFallback);
    }

    @SuppressWarnings("unchecked")
    public List<Employee> searchEmployees(String query, Supplier<List<Employee>> dbFallback) {
        return (List<Employee>) get(keySearch(query), CacheCategory.DEFAULT, List.class, dbFallback);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getEmployeeTimeline(Long employeeId, Supplier<List<Map<String, Object>>> dbFallback) {
        return (List<Map<String, Object>>) get(keyTimeline(employeeId), CacheCategory.REPORT, List.class, dbFallback);
    }

    // ── Eviction & Cache Sync ────────────────────────────────────────────────

    /**
     * Evicts all employee lists, specific profile, timeline, and associated dashboards.
     */
    public void evictAllRelatedCaches(Employee employee) {
        log.info("[Cache] Evicting related caches for Employee update");
        
        // Evict general list caches
        evict(keyAll(), CacheCategory.LIST);
        clearL1(CacheCategory.LIST);
        clearL1(CacheCategory.DEFAULT); // clears searches

        if (employee != null) {
            Long empId = employee.getId();
            evict(keyById(empId), CacheCategory.PROFILE);
            evict(keyTimeline(empId), CacheCategory.REPORT);

            if (employee.getDepartment() != null) {
                evict(keyDepartment(employee.getDepartment()), CacheCategory.LIST);
            }
            if (employee.getManager() != null) {
                evict(keyManager(employee.getManager().getId()), CacheCategory.LIST);
                // Invalidate the manager's dashboard
                managerDashboardCacheService.evictDashboardCache(employee.getManager());
            }
        }

        // Invalidate HR Dashboard
        hrDashboardCacheService.evictAllDashboard();
    }

    /**
     * Evicts specific employee profile and timeline caches.
     */
    public void evictProfileAndTimeline(Long employeeId) {
        if (employeeId != null) {
            evict(keyById(employeeId), CacheCategory.PROFILE);
            evict(keyTimeline(employeeId), CacheCategory.REPORT);
        }
    }

    /**
     * Evicts general employee list and search caches across L1 and L2.
     */
    public void evictGeneralListsAndSearch() {
        evict(keyAll(), CacheCategory.LIST);
        clearL1(CacheCategory.LIST);
        clearL1(CacheCategory.DEFAULT);
    }

    /**
     * Evicts department employee roster list cache.
     */
    public void evictDepartment(String department) {
        if (department != null && !department.isBlank()) {
            evict(keyDepartment(department), CacheCategory.LIST);
        }
    }

    /**
     * Evicts manager direct-report list cache.
     */
    public void evictManagerReports(Long managerId) {
        if (managerId != null) {
            evict(keyManager(managerId), CacheCategory.LIST);
        }
    }

    /**
     * Evicts HR dashboard caches.
     */
    public void evictHrDashboard() {
        hrDashboardCacheService.evictAllDashboard();
    }

    /**
     * Evicts manager dashboard caches for a specific manager ID.
     */
    public void evictManagerDashboard(Long managerId) {
        if (managerId != null) {
            managerDashboardCacheService.evictDashboardCache(managerId);
        }
    }

    /**
     * Evicts caches for generic employee lifecycle transitions (join, activate, suspend, terminate).
     */
    public void evictEmployeeLifecycle(Long employeeId) {
        log.info("[Cache] Evicting lifecycle caches for employee: {}", employeeId);
        evictProfileAndTimeline(employeeId);
        evictGeneralListsAndSearch();
        evictHrDashboard();
    }

    /**
     * Evicts caches affected by a manager change (employee, old manager direct reports/dashboard,
     * new manager direct reports/dashboard, general lists, and HR dashboard).
     */
    public void evictManagerChange(Long employeeId, Long oldManagerId, Long newManagerId) {
        log.info("[Cache] Evicting manager change caches for employee: {}, oldManager: {}, newManager: {}",
                employeeId, oldManagerId, newManagerId);
        evictProfileAndTimeline(employeeId);
        evictGeneralListsAndSearch();
        evictManagerReports(oldManagerId);
        evictManagerReports(newManagerId);
        evictManagerDashboard(oldManagerId);
        evictManagerDashboard(newManagerId);
        evictHrDashboard();
    }

    /**
     * Evicts caches affected by a department transfer (employee, old department roster,
     * new department roster, general lists, and HR dashboard).
     */
    public void evictDepartmentTransfer(Long employeeId, String oldDepartment, String newDepartment) {
        log.info("[Cache] Evicting department transfer caches for employee: {}, oldDept: {}, newDept: {}",
                employeeId, oldDepartment, newDepartment);
        evictProfileAndTimeline(employeeId);
        evictGeneralListsAndSearch();
        evictDepartment(oldDepartment);
        evictDepartment(newDepartment);
        evictHrDashboard();
    }
}
