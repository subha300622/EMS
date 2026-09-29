package com.example.ems.increment.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.increment.dto.IncrementLetterResponse;
import com.example.ems.increment.entity.IncrementLetter;
import com.example.ems.increment.entity.IncrementRecommendation;
import com.example.ems.increment.entity.IncrementRecommendationStatus;
import com.example.ems.increment.repository.IncrementLetterRepository;
import com.example.ems.increment.repository.IncrementRecommendationRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class IncrementLetterService {

    @Autowired
    private IncrementRecommendationRepository recommendationRepository;

    @Autowired
    private IncrementLetterRepository letterRepository;

    @Transactional
    public IncrementLetterResponse generateLetter(Long recommendationId) {
        Long orgId = TenantContext.requireOrganizationId();
        IncrementRecommendation rec = recommendationRepository.findByIdAndOrgId(recommendationId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Increment recommendation not found with ID: " + recommendationId));

        if (rec.getStatus() != IncrementRecommendationStatus.IMPLEMENTED) {
            throw new BadRequestException("Increment letter can only be generated after salary implementation (Status: IMPLEMENTED).");
        }

        // Return existing if already generated
        return letterRepository.findByRecommendationIdAndOrgId(recommendationId, orgId)
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    Employee emp = rec.getEmployee();
                    String refNo = "INC-LET-" + rec.getId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                    StringBuilder sb = new StringBuilder();
                    sb.append("OFFICIAL INCREMENT LETTER\n\n");
                    sb.append("Employee Name: ").append(emp != null ? emp.getFullName() : "").append("\n");
                    sb.append("Employee ID: ").append(emp != null ? emp.getEmployeeId() : "").append("\n");
                    sb.append("Department: ").append(emp != null ? emp.getDepartment() : "").append("\n");
                    sb.append("Designation: ").append(emp != null ? emp.getDesignation() : "").append("\n\n");
                    sb.append("We are pleased to inform you that following your appraisal performance, your compensation has been revised as follows:\n");
                    sb.append("- Previous Annual Salary: ₹").append(rec.getCurrentSalary()).append("\n");
                    sb.append("- Increment Percentage: ").append(rec.getIncrementPercentage()).append("%\n");
                    sb.append("- Increment Amount: ₹").append(rec.getIncrementAmount()).append("\n");
                    sb.append("- Revised Annual Salary: ₹").append(rec.getRecommendedSalary()).append("\n");
                    sb.append("- Effective Date: ").append(rec.getEffectiveDate()).append("\n\n");
                    sb.append("Thank you for your valuable contributions to our organization.");

                    IncrementLetter letter = new IncrementLetter();
                    letter.setOrganization(rec.getOrganization());
                    letter.setRecommendation(rec);
                    letter.setEmployee(emp);
                    letter.setLetterReference(refNo);
                    letter.setContent(sb.toString());
                    letter.setGeneratedAt(LocalDateTime.now());

                    IncrementLetter saved = letterRepository.save(letter);
                    return mapToResponse(saved);
                });
    }

    private IncrementLetterResponse mapToResponse(IncrementLetter letter) {
        IncrementLetterResponse resp = new IncrementLetterResponse();
        resp.setId(letter.getId());
        resp.setRecommendationId(letter.getRecommendation() != null ? letter.getRecommendation().getId() : null);
        resp.setEmployeeId(letter.getEmployee() != null ? letter.getEmployee().getId() : null);
        resp.setEmployeeName(letter.getEmployee() != null ? letter.getEmployee().getFullName() : null);
        resp.setLetterReference(letter.getLetterReference());
        resp.setDocumentUrl(letter.getDocumentUrl());
        resp.setContent(letter.getContent());
        resp.setGeneratedAt(letter.getGeneratedAt());
        return resp;
    }
}
