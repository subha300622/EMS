package com.example.ems.offboarding.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.RecommendationResponse;
import com.example.ems.offboarding.entity.ExitRecommendation;
import com.example.ems.offboarding.entity.Offboarding;
import com.example.ems.offboarding.repository.ExitRecommendationRepository;
import com.example.ems.offboarding.repository.OffboardingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class ExitRecommendationService {

    @Autowired
    private ExitRecommendationRepository recommendationRepository;

    @Autowired
    private OffboardingRepository offboardingRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private void checkLockStatus(Long employeeId) {
        Optional<Offboarding> offboardingOpt = offboardingRepository.findByEmployeeId(employeeId);
        if (offboardingOpt.isPresent()) {
            String status = offboardingOpt.get().getStatus();
            if ("COMPLETED".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status)) {
                throw new IllegalStateException("Recommendation is locked because offboarding is COMPLETED or APPROVED.");
            }
        }
        Optional<ExitRecommendation> recOpt = recommendationRepository.findByEmployeeId(employeeId);
        if (recOpt.isPresent() && recOpt.get().isLocked()) {
            throw new IllegalStateException("Recommendation is locked and cannot be modified.");
        }
    }

    @Transactional
    public ExitRecommendation submitRecommendation(Long employeeId, Double rating, String text, Long managerId) {
        checkLockStatus(employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with ID: " + managerId));

        ExitRecommendation rec = recommendationRepository.findByEmployeeId(employeeId)
                .orElseGet(() -> {
                    ExitRecommendation newRec = new ExitRecommendation();
                    newRec.setEmployee(employee);
                    return newRec;
                });

        rec.setRating(rating);
        rec.setRecommendation(text);
        rec.setCreatedBy(manager);
        rec.setUpdatedAt(LocalDateTime.now());

        // Check if the exit is already complete (although checkLockStatus does this, sync the locked flag here)
        Optional<Offboarding> offboardingOpt = offboardingRepository.findByEmployeeId(employeeId);
        if (offboardingOpt.isPresent()) {
            String status = offboardingOpt.get().getStatus();
            if ("COMPLETED".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status)) {
                rec.setLocked(true);
            }
        }

        return recommendationRepository.save(rec);
    }

    @Transactional
    public ExitRecommendation updateRecommendation(Long employeeId, Double rating, String text) {
        checkLockStatus(employeeId);

        ExitRecommendation rec = recommendationRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found for employee with ID: " + employeeId));

        if (rating != null) {
            rec.setRating(rating);
        }
        if (text != null) {
            rec.setRecommendation(text);
        }
        rec.setUpdatedAt(LocalDateTime.now());

        return recommendationRepository.save(rec);
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendation(Long employeeId) {
        ExitRecommendation rec = recommendationRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found for employee with ID: " + employeeId));

        RecommendationResponse resp = new RecommendationResponse();
        resp.setEmployeeId(rec.getEmployee().getId());
        resp.setEmployeeName(rec.getEmployee().getFullName());
        resp.setRating(rec.getRating());
        resp.setRecommendation(rec.getRecommendation());

        if (rec.getCreatedBy() != null) {
            resp.setCreatedBy("Manager ID " + rec.getCreatedBy().getId());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        resp.setCreatedAt(rec.getCreatedAt().format(formatter));

        return resp;
    }
}
