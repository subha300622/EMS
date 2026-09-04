package com.example.ems.recruitment.service;

import com.example.ems.audit.service.AuditLogService;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.recruitment.dto.ApplicationResponse;
import com.example.ems.recruitment.dto.CandidateConversionRequest;
import com.example.ems.recruitment.entity.*;
import com.example.ems.recruitment.repository.ApplicationRepository;
import com.example.ems.recruitment.repository.CandidateRepository;
import com.example.ems.recruitment.repository.OfferRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class EmployeeConversionService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private AuditLogService auditLogService;

    public ApplicationResponse convertToEmployee(Long applicationId, CandidateConversionRequest request) {
        Long orgId = TenantContext.requireOrganizationId();

        Application app = applicationRepository.findByOrganizationIdAndId(orgId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        if (app.getStatus() != ApplicationStatus.OFFER_ACCEPTED && app.getStatus() != ApplicationStatus.JOINING_PENDING) {
            throw new BadRequestException("Candidate can only be converted to employee if offer is accepted");
        }

        Candidate candidate = app.getCandidate();
        if (candidate == null) {
            throw new BadRequestException("Application has no associated candidate");
        }

        boolean alreadyEmployee = employeeRepository.existsByEmailAndOrganizationId(candidate.getEmail(), orgId);
        if (alreadyEmployee) {
            throw new ConflictException("An employee with email " + candidate.getEmail() + " already exists in this organization");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        if (department.getOrganization() != null && !orgId.equals(department.getOrganization().getId())) {
            throw new BadRequestException("Department does not belong to your organization");
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findByIdAndOrganizationId(request.getManagerId(), orgId)
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.getManagerId()));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Offer offer = offerRepository.findByOrganizationIdAndApplicationId(orgId, applicationId).orElse(null);

        Employee employee = new Employee();
        employee.setOrganization(org);
        employee.setFullName(candidate.getFullName());
        employee.setEmail(candidate.getEmail());
        employee.setPhone(candidate.getPhone());

        String[] nameParts = candidate.getFullName().trim().split("\\s+", 2);
        employee.setFirstName(nameParts[0]);
        employee.setLastName(nameParts.length > 1 ? nameParts[1] : "");

        employee.setDepartment(department.getName());
        if (manager != null) {
            employee.setManager(manager);
        }

        if (offer != null) {
            employee.setDesignation(offer.getDesignation());
            employee.setAnnualSalary(offer.getAnnualSalary());
        } else if (app.getJob() != null) {
            employee.setDesignation(app.getJob().getTitle());
        }

        employee.setJoiningDate(request.getJoiningDate() != null ? request.getJoiningDate() : LocalDate.now());
        employee.setStatus("ACTIVE");
        employee.setEmployeeId(generateEmployeeId(orgId));

        Employee savedEmployee = employeeRepository.save(employee);

        candidate.setTalentPoolStatus(TalentPoolStatus.HIRED);
        candidateRepository.save(candidate);

        ApplicationStatus oldStatus = app.getStatus();
        app.setStatus(ApplicationStatus.JOINED);
        Application updatedApp = applicationRepository.save(app);

        applicationService.recordStatusHistory(updatedApp, oldStatus, ApplicationStatus.JOINED, "HR",
                "Converted candidate to Employee. Assigned Employee ID: " + savedEmployee.getEmployeeId());

        auditLogService.logAction("HR", "hr@company.com", "CONVERT_TO_EMPLOYEE", "Employee",
                savedEmployee.getId().toString(), "127.0.0.1", "Converted candidate " + candidate.getFullName() + " to Employee " + savedEmployee.getEmployeeId());

        return new ApplicationResponse(updatedApp);
    }

    private String generateEmployeeId(Long orgId) {
        String prefix = "EMP-";
        String candidateId;
        int counter = 1001;
        do {
            candidateId = prefix + counter++;
        } while (employeeRepository.existsByEmployeeIdAndOrganizationId(candidateId, orgId));
        return candidateId;
    }
}
