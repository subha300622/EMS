package com.example.ems.onboarding.service;

import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.onboarding.dto.phase.OnboardingPhaseListResponse;
import com.example.ems.onboarding.dto.phase.PhaseUpdateRequest;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.entity.OnboardingTask;
import com.example.ems.onboarding.repository.OnboardingTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OnboardingPhaseService {

    @Autowired
    private OnboardingTaskRepository taskRepository;

    @Autowired
    private OnboardingSecurityValidator securityValidator;

    @Autowired
    private OnboardingAuditLogService auditLogService;

    public OnboardingPhaseListResponse getPhases(Long onboardingId) {
        securityValidator.validateAndGetOnboarding(onboardingId);
        List<OnboardingTask> tasks = taskRepository.findByOnboardingId(onboardingId);

        Map<String, List<OnboardingTask>> grouped = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getPhase() != null ? t.getPhase() : "General"));

        List<OnboardingPhaseListResponse.PhaseItem> phaseItems = new ArrayList<>();
        long phaseCounter = 1;

        for (Map.Entry<String, List<OnboardingTask>> entry : grouped.entrySet()) {
            String phaseName = entry.getKey();
            List<OnboardingTask> phaseTasks = entry.getValue();

            int total = phaseTasks.size();
            int completed = (int) phaseTasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
            double progress = total > 0 ? ((double) completed / total) * 100 : 0.0;

            String status = completed == total ? "COMPLETED" : (completed > 0 ? "IN_PROGRESS" : "PENDING");

            OnboardingPhaseListResponse.PhaseItem item = new OnboardingPhaseListResponse.PhaseItem();
            item.setPhaseId(phaseCounter++);
            item.setName(phaseName);
            item.setStatus(status);
            item.setTotalTasks(total);
            item.setCompletedTasks(completed);
            item.setProgressPercentage(Math.round(progress * 100.0) / 100.0);
            phaseItems.add(item);
        }

        OnboardingPhaseListResponse response = new OnboardingPhaseListResponse();
        response.setOnboardingId(onboardingId);
        response.setPhases(phaseItems);
        return response;
    }

    public OnboardingPhaseListResponse.PhaseItem getPhaseDetails(Long onboardingId, Long phaseId) {
        OnboardingPhaseListResponse response = getPhases(onboardingId);
        return response.getPhases().stream()
                .filter(p -> p.getPhaseId().equals(phaseId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Phase not found with ID: " + phaseId));
    }

    @Transactional
    public OnboardingPhaseListResponse.PhaseItem updatePhase(Long onboardingId, Long phaseId, PhaseUpdateRequest request) {
        Onboarding onboarding = securityValidator.validateAndGetOnboarding(onboardingId);
        OnboardingPhaseListResponse.PhaseItem targetPhase = getPhaseDetails(onboardingId, phaseId);

        List<OnboardingTask> tasks = taskRepository.findByOnboardingId(onboardingId).stream()
                .filter(t -> targetPhase.getName().equalsIgnoreCase(t.getPhase()))
                .collect(Collectors.toList());

        if (request.getStatus() != null && "COMPLETED".equalsIgnoreCase(request.getStatus())) {
            for (OnboardingTask t : tasks) {
                t.setStatus("COMPLETED");
                taskRepository.save(t);
            }
            targetPhase.setStatus("COMPLETED");
            targetPhase.setCompletedTasks(tasks.size());
            targetPhase.setProgressPercentage(100.0);
        } else if (request.getStatus() != null) {
            targetPhase.setStatus(request.getStatus().toUpperCase());
        }

        auditLogService.logAction(onboarding, "PHASE_UPDATED", "ONBOARDING_PHASE", phaseId,
                "Updated phase '" + targetPhase.getName() + "' to status " + targetPhase.getStatus());

        return targetPhase;
    }
}
