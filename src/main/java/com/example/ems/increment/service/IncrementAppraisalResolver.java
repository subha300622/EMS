package com.example.ems.increment.service;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.increment.entity.IncrementPolicy;
import org.springframework.stereotype.Component;

@Component
public class IncrementAppraisalResolver {

    private final AppraisalRepository appraisalRepository;

    public IncrementAppraisalResolver(AppraisalRepository appraisalRepository) {
        this.appraisalRepository = appraisalRepository;
    }

    /**
     * Resolves and validates an appraisal according to the increment policy,
     * tenant ownership, employee ownership, and finalized status.
     */
    public Appraisal resolve(IncrementPolicy policy, Employee employee, Long appraisalId, Long organizationId) {
        boolean appraisalRequired = policy != null && Boolean.TRUE.equals(policy.getAppraisalRequired());

        if (appraisalId == null) {
            if (appraisalRequired) {
                throw new BadRequestException("Appraisal is required for this increment policy, but appraisal ID was not provided.");
            }
            return null;
        }

        Appraisal appraisal = appraisalRepository.findById(appraisalId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + appraisalId));

        // Tenant boundary validation
        if (appraisal.getOrganization() == null || !appraisal.getOrganization().getId().equals(organizationId)) {
            throw new BadRequestException("Appraisal does not belong to the current organization.");
        }

        // Employee ownership validation
        if (appraisal.getEmployee() == null || !appraisal.getEmployee().getId().equals(employee.getId())) {
            throw new BadRequestException("Appraisal does not belong to the specified employee.");
        }

        // Finalized status validation
        if (appraisal.getStatus() != AppraisalStatus.COMPLETED && appraisal.getStatus() != AppraisalStatus.PUBLISHED) {
            throw new BadRequestException("Increment recommendation is allowed only for finalized appraisals (COMPLETED or PUBLISHED).");
        }

        return appraisal;
    }
}
