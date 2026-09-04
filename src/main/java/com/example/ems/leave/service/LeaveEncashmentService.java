package com.example.ems.leave.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.leave.dto.CreateEncashmentRequest;
import com.example.ems.leave.entity.LeaveBalance;
import com.example.ems.leave.entity.LeaveEncashment;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import com.example.ems.leave.repository.LeaveEncashmentRepository;
import com.example.ems.leave.repository.LeaveTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveEncashmentService {

    @Autowired
    private LeaveEncashmentRepository encashmentRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeaveBalanceService balanceService;

    @Autowired
    private LeaveBalanceRepository balanceRepository;

    @Transactional
    public LeaveEncashment requestEncashment(Employee requester, CreateEncashmentRequest request) {
        LeaveType lt = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found with ID: " + request.getLeaveTypeId()));

        Double days = request.getDaysEncashed();
        if (days == null || days <= 0) {
            throw new IllegalArgumentException("Encashment days must be greater than zero");
        }

        LeaveBalance balance = balanceService.getOrCreateBalance(requester, lt, LocalDate.now().getYear());
        if (balance.getAvailableBalance() < days) {
            throw new IllegalArgumentException("Insufficient available balance for encashment. Available: " + balance.getAvailableBalance() + " days");
        }

        // Reserve encashment balance
        balance.setPendingBalance(balance.getPendingBalance() + days);
        balanceRepository.save(balance);

        LeaveEncashment enc = new LeaveEncashment();
        enc.setEmployee(requester);
        enc.setLeaveType(lt);
        enc.setOrganization(requester.getOrganization());
        enc.setDaysEncashed(days);
        enc.setReason(request.getReason());
        enc.setStatus("PENDING");
        enc.setRequestedAt(LocalDateTime.now());
        enc.setUpdatedAt(LocalDateTime.now());

        return encashmentRepository.save(enc);
    }

    public List<LeaveEncashment> getEncashments(Long organizationId) {
        return encashmentRepository.findByOrganizationId(organizationId);
    }

    public LeaveEncashment getEncashmentById(Long encashmentId) {
        return encashmentRepository.findById(encashmentId)
                .orElseThrow(() -> new IllegalArgumentException("Encashment request not found with ID: " + encashmentId));
    }

    @Transactional
    public LeaveEncashment approveEncashment(Long encashmentId) {
        LeaveEncashment enc = getEncashmentById(encashmentId);
        if (!"PENDING".equalsIgnoreCase(enc.getStatus())) {
            throw new IllegalStateException("Encashment request is not in PENDING status");
        }

        // Commit balance deduction
        balanceService.commitBalance(enc.getEmployee(), enc.getLeaveType(), LocalDate.now().getYear(), enc.getDaysEncashed());

        enc.setStatus("APPROVED");
        enc.setUpdatedAt(LocalDateTime.now());
        return encashmentRepository.save(enc);
    }

    @Transactional
    public LeaveEncashment rejectEncashment(Long encashmentId) {
        LeaveEncashment enc = getEncashmentById(encashmentId);
        if (!"PENDING".equalsIgnoreCase(enc.getStatus())) {
            throw new IllegalStateException("Encashment request is not in PENDING status");
        }

        // Release pending balance reservation
        balanceService.releasePendingBalance(enc.getEmployee(), enc.getLeaveType(), LocalDate.now().getYear(), enc.getDaysEncashed());

        enc.setStatus("REJECTED");
        enc.setUpdatedAt(LocalDateTime.now());
        return encashmentRepository.save(enc);
    }

    @Transactional
    public LeaveEncashment cancelEncashment(Long encashmentId) {
        LeaveEncashment enc = getEncashmentById(encashmentId);
        if ("PENDING".equalsIgnoreCase(enc.getStatus())) {
            balanceService.releasePendingBalance(enc.getEmployee(), enc.getLeaveType(), LocalDate.now().getYear(), enc.getDaysEncashed());
        }
        enc.setStatus("CANCELLED");
        enc.setUpdatedAt(LocalDateTime.now());
        return encashmentRepository.save(enc);
    }
}
