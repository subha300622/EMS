package com.example.ems.offboarding.service;

import com.example.ems.auth.entity.User;
import com.example.ems.offboarding.dto.FnfReportResponse;
import com.example.ems.offboarding.dto.FnfReportSummaryDto;
import com.example.ems.offboarding.entity.FnfSettlement;
import com.example.ems.offboarding.repository.ExitFnfSettlementRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.rls.PostgresRlsSessionBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FnfReportService {

    @Autowired
    private ExitFnfSettlementRepository fnfRepository;

    @Autowired(required = false)
    private PostgresRlsSessionBinder rlsSessionBinder;

    private void bindRls() {
        if (rlsSessionBinder != null) {
            try {
                rlsSessionBinder.bindCurrentTenant();
            } catch (Exception ignored) {
            }
        }
    }

    @Transactional(readOnly = true)
    public FnfReportResponse getAllReports(User currentUser, Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRls();
        Page<FnfSettlement> page = fnfRepository.findByOrganizationId(orgId, pageable);

        BigDecimal totalPaid = fnfRepository.sumTotalPaidSettlements(orgId);
        BigDecimal totalPending = fnfRepository.sumTotalPendingSettlements(orgId);
        BigDecimal totalAmount = (totalPaid != null ? totalPaid : BigDecimal.ZERO).add(totalPending != null ? totalPending : BigDecimal.ZERO);

        List<FnfReportSummaryDto> dtoList = page.getContent().stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());

        return new FnfReportResponse(page.getTotalElements(), totalAmount, totalPaid, totalPending, dtoList);
    }

    @Transactional(readOnly = true)
    public FnfReportResponse getPendingReports(User currentUser, Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRls();
        Page<FnfSettlement> page = fnfRepository.findPendingSettlements(orgId, pageable);

        BigDecimal totalPending = fnfRepository.sumTotalPendingSettlements(orgId);

        List<FnfReportSummaryDto> dtoList = page.getContent().stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());

        return new FnfReportResponse(page.getTotalElements(), totalPending, BigDecimal.ZERO, totalPending, dtoList);
    }

    @Transactional(readOnly = true)
    public FnfReportResponse getPaidReports(User currentUser, Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        bindRls();
        Page<FnfSettlement> page = fnfRepository.findPaidSettlements(orgId, pageable);

        BigDecimal totalPaid = fnfRepository.sumTotalPaidSettlements(orgId);

        List<FnfReportSummaryDto> dtoList = page.getContent().stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());

        return new FnfReportResponse(page.getTotalElements(), totalPaid, totalPaid, BigDecimal.ZERO, dtoList);
    }

    private FnfReportSummaryDto mapToSummaryDto(FnfSettlement s) {
        FnfReportSummaryDto dto = new FnfReportSummaryDto();
        dto.setFnfId(s.getId());
        dto.setExitId(s.getExit() != null ? s.getExit().getId() : null);
        if (s.getExit() != null && s.getExit().getEmployee() != null) {
            dto.setEmployeeId(s.getExit().getEmployee().getId());
            dto.setEmployeeName(s.getExit().getEmployee().getFullName());
            dto.setEmployeeCode(s.getExit().getEmployee().getEmployeeId());
            dto.setDepartment(s.getExit().getEmployee().getDepartment());
            dto.setDesignation(s.getExit().getEmployee().getDesignation());
            dto.setLastWorkingDate(s.getExit().getLastWorkingDate());
        }
        dto.setStatus(s.getStatus());
        dto.setTotalEarnings(s.getTotalEarnings());
        dto.setTotalDeductions(s.getTotalDeductions());
        dto.setNetSettlement(s.getNetSettlement());
        dto.setPaidAmount(s.getPaidAmount());
        dto.setPaymentDate(s.getPaymentDate());
        dto.setPaymentReference(s.getPaymentReference());
        return dto;
    }
}
