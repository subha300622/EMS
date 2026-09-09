package com.example.ems.increment.service;

import com.example.ems.appraisal.entity.SalaryRevision;
import com.example.ems.appraisal.repository.SalaryRevisionRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.increment.dto.IncrementRecommendationResponse;
import com.example.ems.increment.entity.IncrementRecommendation;
import com.example.ems.increment.entity.IncrementRecommendationStatus;
import com.example.ems.increment.event.SalaryRevisionCreatedEvent;
import com.example.ems.increment.repository.IncrementRecommendationRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class IncrementImplementationService {

    @Autowired
    private IncrementRecommendationRepository recommendationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRevisionRepository salaryRevisionRepository;

    @Autowired
    private IncrementRecommendationService recommendationService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public IncrementRecommendationResponse implementIncrement(Long recommendationId) {
        Long orgId = TenantContext.requireOrganizationId();
        IncrementRecommendation rec = recommendationRepository.findByIdAndOrgId(recommendationId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Increment recommendation not found with ID: " + recommendationId));

        // Strict validation: cannot implement in any state other than APPROVED
        if (rec.getStatus() == IncrementRecommendationStatus.IMPLEMENTED) {
            throw new BadRequestException("This increment recommendation has already been implemented.");
        }
        if (rec.getStatus() != IncrementRecommendationStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED recommendations can be implemented. Current status: " + rec.getStatus());
        }

        Employee employee = rec.getEmployee();
        if (employee == null || !"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new BadRequestException("Employee is inactive or not found.");
        }

        // Lock employee record & apply compensation revision
        SalaryRevision salaryRevision = new SalaryRevision();
        salaryRevision.setEmployee(employee);
        salaryRevision.setPreviousSalary(rec.getCurrentSalary());
        salaryRevision.setNewSalary(rec.getRecommendedSalary());
        salaryRevision.setChangePercentage(rec.getIncrementPercentage());
        salaryRevision.setEffectiveDate(rec.getEffectiveDate());
        salaryRevision.setReason("ANNUAL_INCREMENT_CYCLE_" + (rec.getCycle() != null ? rec.getCycle().getName() : ""));
        salaryRevision.setCreatedAt(LocalDateTime.now());

        SalaryRevision savedRevision = salaryRevisionRepository.save(salaryRevision);

        // Update employee compensation
        employee.setAnnualSalary(rec.getRecommendedSalary());
        employeeRepository.save(employee);

        // Transition recommendation status to IMPLEMENTED (version is checked via @Version)
        rec.setStatus(IncrementRecommendationStatus.IMPLEMENTED);
        IncrementRecommendation updatedRec = recommendationRepository.save(rec);

        // Publish event for Payroll, Documents, and Notifications modules
        try {
            eventPublisher.publishEvent(new SalaryRevisionCreatedEvent(
                    this,
                    orgId,
                    employee.getId(),
                    rec.getId(),
                    savedRevision.getId(),
                    rec.getCurrentSalary(),
                    rec.getRecommendedSalary(),
                    rec.getIncrementPercentage(),
                    rec.getIncrementAmount(),
                    rec.getEffectiveDate()
            ));
        } catch (Exception ignored) {}

        return recommendationService.mapToResponse(updatedRec);
    }
}
