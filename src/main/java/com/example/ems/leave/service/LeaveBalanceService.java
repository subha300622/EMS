package com.example.ems.leave.service;

import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.dto.BalanceAdjustmentRequest;
import com.example.ems.leave.entity.LeaveBalance;
import com.example.ems.leave.entity.LeaveBalanceAdjustment;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveBalanceAdjustmentRepository;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import com.example.ems.leave.repository.LeaveTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveBalanceService {

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveBalanceAdjustmentRepository adjustmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    public LeaveBalance getOrCreateBalance(Employee employee, LeaveType leaveType, Integer year) {
        return leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employee.getId(), leaveType.getId(), year)
                .orElseGet(() -> {
                    LeaveBalance b = new LeaveBalance();
                    b.setEmployee(employee);
                    b.setLeaveType(leaveType);
                    b.setOrganization(employee.getOrganization());
                    b.setYear(year);
                    b.setTotalEntitlement(leaveType.getDefaultDays() != null ? leaveType.getDefaultDays().doubleValue() : 0.0);
                    b.setUsedBalance(0.0);
                    b.setPendingBalance(0.0);
                    return leaveBalanceRepository.save(b);
                });
    }

    @Transactional
    public void reserveBalance(Employee employee, LeaveType leaveType, Integer year, Double days) {
        LeaveBalance balance = getOrCreateBalance(employee, leaveType, year);
        balance.setPendingBalance(balance.getPendingBalance() + days);
        balance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceRepository.save(balance);
    }

    @Transactional
    public void commitBalance(Employee employee, LeaveType leaveType, Integer year, Double days) {
        LeaveBalance balance = getOrCreateBalance(employee, leaveType, year);
        balance.setPendingBalance(Math.max(0.0, balance.getPendingBalance() - days));
        balance.setUsedBalance(balance.getUsedBalance() + days);
        balance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceRepository.save(balance);
    }

    @Transactional
    public void releasePendingBalance(Employee employee, LeaveType leaveType, Integer year, Double days) {
        LeaveBalance balance = getOrCreateBalance(employee, leaveType, year);
        balance.setPendingBalance(Math.max(0.0, balance.getPendingBalance() - days));
        balance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceRepository.save(balance);
    }

    @Transactional
    public void refundApprovedBalance(Employee employee, LeaveType leaveType, Integer year, Double days) {
        LeaveBalance balance = getOrCreateBalance(employee, leaveType, year);
        balance.setUsedBalance(Math.max(0.0, balance.getUsedBalance() - days));
        balance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceRepository.save(balance);
    }

    @Transactional
    public LeaveBalanceAdjustment adjustBalance(User currentUser, BalanceAdjustmentRequest request) {
        String empIdStr = request.getEmployeeId();
        Employee emp = null;
        if (empIdStr != null) {
            try {
                Long numId = Long.parseLong(empIdStr);
                emp = employeeRepository.findById(numId).orElse(null);
            } catch (NumberFormatException ignored) {}
            if (emp == null) {
                emp = employeeRepository.findByEmployeeId(empIdStr)
                        .or(() -> employeeRepository.findByEmail(empIdStr))
                        .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + empIdStr));
            }
        } else {
            throw new IllegalArgumentException("Employee ID is required");
        }

        LeaveType lt = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found with ID: " + request.getLeaveTypeId()));

        LeaveBalance balance = getOrCreateBalance(emp, lt, LocalDateTime.now().getYear());
        balance.setTotalEntitlement(balance.getTotalEntitlement() + request.getAdjustment());
        balance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceRepository.save(balance);

        Employee adminEmp = currentUser != null && currentUser.getWorkEmail() != null
                ? employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null)
                : null;

        LeaveBalanceAdjustment adj = new LeaveBalanceAdjustment();
        adj.setEmployee(emp);
        adj.setLeaveType(lt);
        adj.setOrganization(emp.getOrganization());
        adj.setAdjustmentAmount(request.getAdjustment());
        adj.setReason(request.getReason());
        adj.setAdjustedBy(adminEmp);
        adj.setAdjustedAt(LocalDateTime.now());

        return adjustmentRepository.save(adj);
    }

    public List<LeaveBalance> getEmployeeBalances(Long employeeId) {
        return getEmployeeBalances(employeeId, null);
    }

    public List<LeaveBalance> getEmployeeBalances(Long employeeId, Integer year) {
        if (year != null) {
            return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year);
        }
        return leaveBalanceRepository.findByEmployeeId(employeeId);
    }

    public List<LeaveBalance> getOrganizationBalances(Long organizationId) {
        return leaveBalanceRepository.findByOrganizationId(organizationId);
    }

    public List<LeaveBalanceAdjustment> getAdjustments(Long organizationId) {
        return adjustmentRepository.findByOrganizationId(organizationId);
    }
}
