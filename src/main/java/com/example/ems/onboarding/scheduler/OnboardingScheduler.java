package com.example.ems.onboarding.scheduler;

import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.entity.OnboardingTask;
import com.example.ems.onboarding.repository.OnboardingRepository;
import com.example.ems.onboarding.repository.OnboardingTaskRepository;
import com.example.ems.onboarding.service.TeamOnboardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class OnboardingScheduler {

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private OnboardingTaskRepository onboardingTaskRepository;

    @Autowired
    private TeamOnboardingService teamOnboardingService;

    // ── 1. DAILY OVERDUE TASK SCANNER & ESCALATION (Runs at midnight daily) ──
    @Scheduled(cron = "0 0 0 * * ?")
    public void scanAndEscalateOverdueTasks() {
        System.out.println("OnboardingScheduler: Scanning for overdue tasks...");
        LocalDate today = LocalDate.now();

        List<OnboardingTask> overdueTasks = onboardingTaskRepository.findAll().stream()
                .filter(t -> !"COMPLETED".equalsIgnoreCase(t.getStatus()) && t.getDueDate().isBefore(today))
                .toList();

        for (OnboardingTask task : overdueTasks) {
            task.setPriority("CRITICAL"); // Escalate priority
            onboardingTaskRepository.save(task);
            System.out.println("OnboardingScheduler: Escalated task '" + task.getTitle() + "' to CRITICAL for onboarding ID: " + task.getOnboarding().getId());
        }
    }

    // ── 2. NIGHTLY CACHE RECONCILIATION HEALING (Runs at 2 AM daily) ────────
    @Scheduled(cron = "0 0 2 * * ?")
    public void reconcileOnboardingCaches() {
        System.out.println("OnboardingScheduler: Running nightly cache reconciliation...");
        List<Onboarding> activeOnboardings = onboardingRepository.findAll().stream()
                .filter(o -> !"COMPLETED".equalsIgnoreCase(o.getStatus()))
                .toList();

        for (Onboarding o : activeOnboardings) {
            try {
                teamOnboardingService.reconcileProgressCache(o.getId());
            } catch (Exception e) {
                System.err.println("OnboardingScheduler: Failed to reconcile progress cache for onboarding ID: " + o.getId() + " - " + e.getMessage());
            }
        }
    }
}
