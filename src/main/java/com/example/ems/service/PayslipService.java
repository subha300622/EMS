package com.example.ems.service;

import com.example.ems.entity.Payroll;
import com.example.ems.entity.Payslip;
import com.example.ems.repository.PayrollRepository;
import com.example.ems.repository.PayslipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayslipService {

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    public List<Payslip> generatePayslips(Integer month, Integer year) {
        List<Payroll> eligiblePayrolls = payrollRepository.findByMonthAndYear(month, year).stream()
                .filter(p -> "PROCESSED".equalsIgnoreCase(p.getStatus()) || "PAID".equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());

        List<Payslip> generated = new ArrayList<>();

        for (Payroll payroll : eligiblePayrolls) {
            Optional<Payslip> existing = payslipRepository.findByPayrollId(payroll.getId());
            if (existing.isEmpty()) {
                Payslip ps = new Payslip();
                ps.setPayroll(payroll);
                
                String num = "PS-" + year + "-" + String.format("%02d", month) + "-" + payroll.getEmployee().getId();
                ps.setPayslipNumber(num);
                ps.setGeneratedAt(LocalDateTime.now());
                
                generated.add(payslipRepository.save(ps));
            }
        }
        return generated;
    }

    public List<Payslip> getPayslipsByEmployeeId(Long employeeId) {
        return payslipRepository.findByPayrollEmployeeId(employeeId);
    }

    public Optional<Payslip> getPayslipById(Long id) {
        return payslipRepository.findById(id);
    }
}
