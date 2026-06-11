package com.example.ems.leave.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.leave.dto.LeaveRequest;
import com.example.ems.leave.dto.LeaveTypeRequest;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.repository.LeaveTypeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    // ── LEAVE TYPE CONFIGURATION ─────────────────────────────────────────────

    public LeaveType createLeaveType(LeaveTypeRequest request) {
        if (leaveTypeRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Leave type name already exists");
        }
        LeaveType leaveType = new LeaveType();
        leaveType.setName(request.getName());
        leaveType.setDescription(request.getDescription());
        leaveType.setDefaultDays(request.getDefaultDays());
        leaveType.setActive(true);
        return leaveTypeRepository.save(leaveType);
    }

    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }

    public LeaveType updateLeaveType(Long id, LeaveTypeRequest request) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found"));
        
        Optional<LeaveType> existing = leaveTypeRepository.findByName(request.getName());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new IllegalArgumentException("Leave type name already exists");
        }

        leaveType.setName(request.getName());
        leaveType.setDescription(request.getDescription());
        leaveType.setDefaultDays(request.getDefaultDays());
        return leaveTypeRepository.save(leaveType);
    }

    public Optional<LeaveType> getLeaveTypeById(Long id) {
        return leaveTypeRepository.findById(id);
    }

    public void deleteLeaveType(Long id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found"));
        leaveTypeRepository.delete(leaveType);
    }

    public LeaveType deactivateLeaveType(Long id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found"));
        leaveType.setActive(false);
        return leaveTypeRepository.save(leaveType);
    }

    public LeaveType activateLeaveType(Long id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found"));
        leaveType.setActive(true);
        return leaveTypeRepository.save(leaveType);
    }

    // ── LEAVE APPLICATIONS ───────────────────────────────────────────────────

    public Leave applyLeave(Employee employee, LeaveRequest request) {
        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found"));

        if (!leaveType.isActive()) {
            throw new IllegalArgumentException("This leave type is inactive");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        long duration = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        long remaining = getRemainingBalance(employee.getId(), leaveType, request.getStartDate().getYear());

        if (duration > remaining) {
            throw new IllegalArgumentException("Insufficient leave balance. Remaining: " + remaining + " days, Requested: " + duration + " days");
        }

        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setLeaveType(leaveType);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());
        leave.setStatus("PENDING");
        leave.setAppliedAt(LocalDateTime.now());
        leave.setUpdatedAt(LocalDateTime.now());

        return leaveRepository.save(leave);
    }

    public List<Leave> getAllLeaves() {
        return leaveRepository.findAll();
    }

    public Optional<Leave> getLeaveById(Long id) {
        return leaveRepository.findById(id);
    }

    public List<Leave> getLeavesByEmployeeId(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId);
    }

    public List<Leave> getPendingLeaves() {
        return leaveRepository.findByStatus("PENDING");
    }

    public Leave approveLeave(Long id, Employee approver) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (!"PENDING".equalsIgnoreCase(leave.getStatus())) {
            throw new IllegalArgumentException("Only PENDING leave requests can be approved");
        }

        leave.setStatus("APPROVED");
        leave.setApprovedBy(approver);
        leave.setUpdatedAt(LocalDateTime.now());
        return leaveRepository.save(leave);
    }

    public Leave rejectLeave(Long id, Employee approver) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (!"PENDING".equalsIgnoreCase(leave.getStatus())) {
            throw new IllegalArgumentException("Only PENDING leave requests can be rejected");
        }

        leave.setStatus("REJECTED");
        leave.setApprovedBy(approver);
        leave.setUpdatedAt(LocalDateTime.now());
        return leaveRepository.save(leave);
    }

    public Leave cancelLeave(Long id, Employee employee) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (!leave.getEmployee().getId().equals(employee.getId())) {
            throw new SecurityException("You can only cancel your own leave requests");
        }

        if ("REJECTED".equalsIgnoreCase(leave.getStatus()) || "CANCELLED".equalsIgnoreCase(leave.getStatus())) {
            throw new IllegalArgumentException("Leave request is already " + leave.getStatus().toLowerCase());
        }

        leave.setStatus("CANCELLED");
        leave.setUpdatedAt(LocalDateTime.now());
        return leaveRepository.save(leave);
    }

    // ── LEAVE BALANCE & STATS ────────────────────────────────────────────────

    public Map<String, Object> getLeaveBalance(Long employeeId) {
        int currentYear = LocalDate.now().getYear();
        List<LeaveType> activeTypes = leaveTypeRepository.findAll().stream()
                .filter(LeaveType::isActive)
                .collect(Collectors.toList());

        Map<String, Object> balanceMap = new LinkedHashMap<>();
        for (LeaveType type : activeTypes) {
            long used = getUsedApprovedDays(employeeId, type.getId(), currentYear);
            long total = type.getDefaultDays();
            long remaining = total - used;

            Map<String, Object> typeDetails = new LinkedHashMap<>();
            typeDetails.put("leaveTypeId", type.getId());
            typeDetails.put("total", total);
            typeDetails.put("used", used);
            typeDetails.put("remaining", remaining);

            balanceMap.put(type.getName(), typeDetails);
        }

        return balanceMap;
    }

    public Map<String, Object> getLeaveStats() {
        List<Leave> all = leaveRepository.findAll();
        long total = all.size();
        long pending = all.stream().filter(l -> "PENDING".equalsIgnoreCase(l.getStatus())).count();
        long approved = all.stream().filter(l -> "APPROVED".equalsIgnoreCase(l.getStatus())).count();
        long rejected = all.stream().filter(l -> "REJECTED".equalsIgnoreCase(l.getStatus())).count();
        long cancelled = all.stream().filter(l -> "CANCELLED".equalsIgnoreCase(l.getStatus())).count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalApplications", total);
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        stats.put("cancelled", cancelled);
        return stats;
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────────────────────

    private long getRemainingBalance(Long employeeId, LeaveType type, int year) {
        long used = getUsedApprovedDays(employeeId, type.getId(), year);
        return type.getDefaultDays() - used;
    }

    private long getUsedApprovedDays(Long employeeId, Long leaveTypeId, int year) {
        List<Leave> approvedLeaves = leaveRepository.findByEmployeeIdAndLeaveTypeIdAndStatus(employeeId, leaveTypeId, "APPROVED");
        long usedDays = 0;
        for (Leave leave : approvedLeaves) {
            // Check if within the requested year
            if (leave.getStartDate().getYear() == year || leave.getEndDate().getYear() == year) {
                // Determine overlapping start & end dates in that calendar year
                LocalDate overlapStart = leave.getStartDate().getYear() < year ? LocalDate.of(year, 1, 1) : leave.getStartDate();
                LocalDate overlapEnd = leave.getEndDate().getYear() > year ? LocalDate.of(year, 12, 31) : leave.getEndDate();
                
                if (!overlapStart.isAfter(overlapEnd)) {
                    usedDays += ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
                }
            }
        }
        return usedDays;
    }
}
