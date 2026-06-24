package com.example.ems.onboarding.service;

import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.repository.OnboardingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class OnboardingReconciliationService {

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private TeamOnboardingService teamOnboardingService;

    @Transactional
    public Map<String, Object> reconcileOnboardings() {
        List<Onboarding> activeOnboardings = onboardingRepository.findAll().stream()
                .filter(o -> !"COMPLETED".equalsIgnoreCase(o.getStatus()))
                .toList();

        int successCount = 0;
        int failCount = 0;

        for (Onboarding o : activeOnboardings) {
            try {
                teamOnboardingService.reconcileProgressCache(o.getId());
                successCount++;
            } catch (Exception e) {
                failCount++;
                System.err.println("OnboardingReconciliationService: Failed to reconcile progress cache for onboarding ID: " + o.getId() + " - " + e.getMessage());
            }
        }

        return Map.of(
            "status", "SUCCESS",
            "totalProcessed", activeOnboardings.size(),
            "reconciledCount", successCount,
            "failedCount", failCount
        );
    }
}
