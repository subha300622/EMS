package com.example.ems.finance.service;

import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import com.example.ems.finance.repository.EmployeeFinanceOnboardingRepository;
import com.example.ems.payroll.entity.SalaryStructure;
import com.example.ems.payroll.repository.SalaryStructureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FinanceReconciliationService {

    @Autowired
    private EmployeeFinanceOnboardingRepository repository;

    @Autowired
    private SalaryStructureRepository salaryStructureRepository;

    @Transactional
    public Map<String, Object> reconcileFinance() {
        List<EmployeeFinanceOnboarding> all = repository.findAll();
        int syncedSalaryStructures = 0;
        int syncedStatuses = 0;

        for (EmployeeFinanceOnboarding ob : all) {
            // 1. Sync SalaryStructure table
            if (Boolean.TRUE.equals(ob.getSalaryStructureAssigned()) && ob.getEmployee() != null) {
                Long empId = ob.getEmployee().getId();
                Optional<SalaryStructure> ssOpt = salaryStructureRepository.findByEmployeeId(empId);
                SalaryStructure ss = ssOpt.orElseGet(SalaryStructure::new);
                if (ss.getEmployeeId() == null ||
                    (ss.getBasicSalary() != null && ss.getBasicSalary().compareTo(ob.getBasicSalary()) != 0) ||
                    (ss.getHra() != null && ss.getHra().compareTo(ob.getHra()) != 0) ||
                    (ss.getAllowances() != null && ss.getAllowances().compareTo(ob.getAllowances()) != 0)) {
                    
                    ss.setEmployeeId(empId);
                    ss.setBasicSalary(ob.getBasicSalary());
                    ss.setHra(ob.getHra());
                    ss.setAllowances(ob.getAllowances());
                    salaryStructureRepository.save(ss);
                    syncedSalaryStructures++;
                }
            }

            // 2. Reconcile overall status if payroll is activated
            if (Boolean.TRUE.equals(ob.getPayrollActivated()) && !"APPROVED".equalsIgnoreCase(ob.getStatus())) {
                ob.setStatus("APPROVED");
                repository.save(ob);
                syncedStatuses++;
            }
        }

        return Map.of(
            "status", "SUCCESS",
            "syncedSalaryStructures", syncedSalaryStructures,
            "syncedStatuses", syncedStatuses,
            "totalProcessed", all.size()
        );
    }
}
