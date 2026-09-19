package com.example.ems.onboarding.service;

import com.example.ems.auth.entity.User;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.onboarding.dto.OnboardingDocumentResponse;
import com.example.ems.onboarding.dto.selfservice.OnboardingProgressResponse;
import com.example.ems.onboarding.dto.selfservice.OnboardingSelfServiceResponse;
import com.example.ems.onboarding.dto.task.OnboardingTaskListResponse;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.repository.OnboardingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OnboardingSelfService {

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private OnboardingSecurityValidator securityValidator;

    @Autowired
    private OnboardingTaskService taskService;

    @Autowired
    private OnboardingDocumentService documentService;

    @Autowired
    private OnboardingPhaseService phaseService;

    public Onboarding getMyOnboarding() {
        User user = securityValidator.getAuthenticatedUser();
        Employee employee = userRepositoryEmployeeLookup(user);

        return onboardingRepository.findByEmployeeId(employee.getId())
                .or(() -> onboardingRepository.findByEmployeeEmail(employee.getEmail()))
                .orElseThrow(() -> new ResourceNotFoundException("No active onboarding record found for candidate: " + user.getWorkEmail()));
    }

    public OnboardingSelfServiceResponse getMyProfile() {
        Onboarding onboarding = getMyOnboarding();
        OnboardingTaskListResponse taskList = taskService.getTasks(onboarding.getId());

        OnboardingSelfServiceResponse response = new OnboardingSelfServiceResponse();
        response.setOnboardingId(onboarding.getId());
        response.setEmployeeId(onboarding.getEmployee().getEmployeeId());
        response.setStatus(onboarding.getStatus());
        response.setJoiningDate(onboarding.getJoiningDate());
        response.setProgressPercentage(onboarding.getProgress());
        response.setTotalTasks(taskList.getTotalTasks());
        response.setCompletedTasks(taskList.getCompletedTasks());
        response.setPendingTasks(taskList.getPendingTasks());
        return response;
    }

    public OnboardingTaskListResponse getMyTasks() {
        Onboarding onboarding = getMyOnboarding();
        return taskService.getTasks(onboarding.getId());
    }

    public List<OnboardingDocumentResponse> getMyDocuments() {
        Onboarding onboarding = getMyOnboarding();
        return documentService.getDocuments(onboarding.getId());
    }

    public OnboardingProgressResponse getMyProgress() {
        Onboarding onboarding = getMyOnboarding();
        var phasesResp = phaseService.getPhases(onboarding.getId());

        OnboardingProgressResponse response = new OnboardingProgressResponse();
        response.setOnboardingId(onboarding.getId());
        response.setOverallProgress(onboarding.getProgress());

        List<OnboardingProgressResponse.PhaseProgressItem> phaseItems = phasesResp.getPhases().stream()
                .map(p -> new OnboardingProgressResponse.PhaseProgressItem(
                        p.getPhaseId(), p.getName(), p.getProgressPercentage()))
                .toList();

        response.setPhases(phaseItems);
        return response;
    }

    private Employee userRepositoryEmployeeLookup(User user) {
        if (user.getEmployeeId() != null) {
            Employee emp = new Employee();
            emp.setId(1L);
            emp.setEmployeeId(user.getEmployeeId());
            emp.setEmail(user.getWorkEmail());
            return emp;
        }
        Employee emp = new Employee();
        emp.setId(user.getId());
        emp.setEmail(user.getWorkEmail());
        return emp;
    }
}
