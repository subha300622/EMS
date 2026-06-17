package com.example.ems.payroll.service;

import com.example.ems.payroll.entity.FnfSettlement;
import com.example.ems.payroll.repository.FnfSettlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FnfSettlementService {

    @Autowired
    private FnfSettlementRepository fnfSettlementRepository;

    @Transactional
    public FnfSettlement createSettlement(FnfSettlement settlement) {
        BigDecimal gratuity = settlement.getGratuity() != null ? settlement.getGratuity() : BigDecimal.ZERO;
        BigDecimal noticePay = settlement.getNoticePay() != null ? settlement.getNoticePay() : BigDecimal.ZERO;
        BigDecimal unpaidSalary = settlement.getUnpaidSalary() != null ? settlement.getUnpaidSalary() : BigDecimal.ZERO;
        BigDecimal otherDeductions = settlement.getOtherDeductions() != null ? settlement.getOtherDeductions() : BigDecimal.ZERO;

        BigDecimal netAmount = gratuity.add(noticePay).add(unpaidSalary).subtract(otherDeductions);
        settlement.setNetAmount(netAmount);
        settlement.setStatus("PENDING");
        settlement.setCreatedAt(LocalDateTime.now());
        settlement.setUpdatedAt(LocalDateTime.now());

        return fnfSettlementRepository.save(settlement);
    }

    public List<FnfSettlement> getAllSettlements() {
        return fnfSettlementRepository.findAll();
    }

    public Optional<FnfSettlement> getSettlementById(Long id) {
        return fnfSettlementRepository.findById(id);
    }

    public Optional<FnfSettlement> getSettlementByEmployeeId(Long employeeId) {
        return fnfSettlementRepository.findByEmployeeId(employeeId);
    }

    @Transactional
    public Optional<FnfSettlement> approveSettlement(Long id) {
        return fnfSettlementRepository.findById(id).map(settlement -> {
            settlement.setStatus("APPROVED");
            settlement.setUpdatedAt(LocalDateTime.now());
            return fnfSettlementRepository.save(settlement);
        });
    }

    @Transactional
    public Optional<FnfSettlement> rejectSettlement(Long id) {
        return fnfSettlementRepository.findById(id).map(settlement -> {
            settlement.setStatus("REJECTED");
            settlement.setUpdatedAt(LocalDateTime.now());
            return fnfSettlementRepository.save(settlement);
        });
    }

    @Transactional
    public Optional<FnfSettlement> processSettlement(Long id) {
        return fnfSettlementRepository.findById(id).map(settlement -> {
            settlement.setStatus("PROCESSED");
            settlement.setUpdatedAt(LocalDateTime.now());
            return fnfSettlementRepository.save(settlement);
        });
    }
}
