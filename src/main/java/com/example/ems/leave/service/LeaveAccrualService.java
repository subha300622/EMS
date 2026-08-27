package com.example.ems.leave.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.entity.LeaveAccrualRule;
import com.example.ems.leave.entity.LeaveAccrualTransaction;
import com.example.ems.leave.entity.LeaveBalance;
import com.example.ems.leave.repository.LeaveAccrualRuleRepository;
import com.example.ems.leave.repository.LeaveAccrualTransactionRepository;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveAccrualService {

    @Autowired
    private LeaveAccrualRuleRepository accrualRuleRepository;

    @Autowired
    private LeaveAccrualTransactionRepository transactionRepository;

    @Autowired
    private LeaveBalanceRepository balanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveBalanceService balanceService;

    @Transactional
    public List<LeaveAccrualTransaction> runAccrualsForOrganization(Long organizationId) {
        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(e -> e.getOrganization() == null || organizationId.equals(e.getOrganization().getId()))
                .toList();

        List<LeaveAccrualRule> rules = accrualRuleRepository.findByOrganizationId(organizationId);
        if (rules.isEmpty()) {
            rules = accrualRuleRepository.findAll();
        }

        List<LeaveAccrualTransaction> txns = new ArrayList<>();
        for (Employee emp : employees) {
            for (LeaveAccrualRule rule : rules) {
                if (rule.isActive() && rule.getLeaveType() != null) {
                    LeaveAccrualTransaction txn = accrueForEmployeeAndRule(emp, rule);
                    if (txn != null) txns.add(txn);
                }
            }
        }
        return txns;
    }

    @Transactional
    public List<LeaveAccrualTransaction> accrueForEmployee(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        Long orgId = emp.getOrganization() != null ? emp.getOrganization().getId() : 1L;
        List<LeaveAccrualRule> rules = accrualRuleRepository.findByOrganizationId(orgId);
        if (rules.isEmpty()) rules = accrualRuleRepository.findAll();

        List<LeaveAccrualTransaction> txns = new ArrayList<>();
        for (LeaveAccrualRule rule : rules) {
            if (rule.isActive() && rule.getLeaveType() != null) {
                LeaveAccrualTransaction txn = accrueForEmployeeAndRule(emp, rule);
                if (txn != null) txns.add(txn);
            }
        }
        return txns;
    }

    private LeaveAccrualTransaction accrueForEmployeeAndRule(Employee employee, LeaveAccrualRule rule) {
        Double credit = rule.getCreditAmount() != null ? rule.getCreditAmount() : 1.0;
        int currentYear = LocalDate.now().getYear();

        LeaveBalance balance = balanceService.getOrCreateBalance(employee, rule.getLeaveType(), currentYear);
        balance.setTotalEntitlement(balance.getTotalEntitlement() + credit);
        balance.setUpdatedAt(LocalDateTime.now());
        balanceRepository.save(balance);

        String periodStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        LeaveAccrualTransaction txn = new LeaveAccrualTransaction();
        txn.setEmployee(employee);
        txn.setLeaveType(rule.getLeaveType());
        txn.setOrganization(employee.getOrganization());
        txn.setAccruedAmount(credit);
        txn.setPeriod(periodStr);
        txn.setAccruedAt(LocalDateTime.now());

        return transactionRepository.save(txn);
    }

    public List<LeaveAccrualTransaction> getAccrualHistory(Long organizationId) {
        return transactionRepository.findByOrganizationId(organizationId);
    }
}
