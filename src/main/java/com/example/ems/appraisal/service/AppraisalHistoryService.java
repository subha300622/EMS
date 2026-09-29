package com.example.ems.appraisal.service;

import com.example.ems.appraisal.dto.AppraisalHistoryResponseDto;
import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalHistory;
import com.example.ems.appraisal.repository.AppraisalHistoryRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppraisalHistoryService {

    @Autowired
    private AppraisalHistoryRepository historyRepository;

    @Transactional
    public AppraisalHistory recordHistory(
            Appraisal appraisal,
            Employee employee,
            String changeType,
            String oldValue,
            String newValue,
            Employee changedBy,
            String comments) {
        AppraisalHistory history = new AppraisalHistory();
        history.setOrganization(appraisal.getOrganization());
        history.setAppraisal(appraisal);
        history.setEmployee(employee != null ? employee : appraisal.getEmployee());
        history.setChangeType(changeType);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setChangedBy(changedBy);
        history.setComments(comments);
        history.setChangedAt(LocalDateTime.now());
        return historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<AppraisalHistoryResponseDto> getHistoryForAppraisal(Long appraisalId) {
        return historyRepository.findByAppraisalIdOrderByChangedAtDesc(appraisalId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppraisalHistoryResponseDto> getHistoryForEmployee(Long employeeId) {
        Long orgId = TenantContext.requireOrganizationId();
        return historyRepository.findByOrganizationIdAndEmployeeIdOrderByChangedAtDesc(orgId, employeeId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AppraisalHistoryResponseDto mapToDto(AppraisalHistory h) {
        AppraisalHistoryResponseDto dto = new AppraisalHistoryResponseDto();
        dto.setId(h.getId());
        dto.setAppraisalId(h.getAppraisal() != null ? h.getAppraisal().getId() : null);
        if (h.getEmployee() != null) {
            dto.setEmployeeId(h.getEmployee().getId());
            dto.setEmployeeName(h.getEmployee().getFullName());
        }
        dto.setChangeType(h.getChangeType());
        dto.setOldValue(h.getOldValue());
        dto.setNewValue(h.getNewValue());
        if (h.getChangedBy() != null) {
            dto.setChangedById(h.getChangedBy().getId());
            dto.setChangedByName(h.getChangedBy().getFullName());
        }
        dto.setComments(h.getComments());
        dto.setChangedAt(h.getChangedAt());
        return dto;
    }
}
