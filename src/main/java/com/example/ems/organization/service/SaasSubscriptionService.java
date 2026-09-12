package com.example.ems.organization.service;

import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.Subscription;
import com.example.ems.organization.entity.SubscriptionStatus;
import com.example.ems.organization.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;

@Service
public class SaasSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Value("${app.plans.starter.employee-limit:25}")
    private int starterEmployeeLimit;

    @Value("${app.plans.starter.trial-days:14}")
    private int starterTrialDays;

    @Value("${app.plans.growth.employee-limit:100}")
    private int growthEmployeeLimit;

    @Value("${app.plans.growth.trial-days:14}")
    private int growthTrialDays;

    @Value("${app.plans.enterprise.employee-limit:10000}")
    private int enterpriseEmployeeLimit;

    @Value("${app.plans.enterprise.trial-days:14}")
    private int enterpriseTrialDays;

    public SaasSubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public Subscription createTrialSubscription(Organization organization, String requestedPlan, String billingCycle) {
        String plan = requestedPlan != null ? requestedPlan.trim().toUpperCase() : "STARTER";
        
        int employeeLimit;
        int trialDays;
        String planName;

        switch (plan) {
            case "GROWTH":
                employeeLimit = growthEmployeeLimit;
                trialDays = growthTrialDays;
                planName = "Growth Plan";
                break;
            case "ENTERPRISE":
                employeeLimit = enterpriseEmployeeLimit;
                trialDays = enterpriseTrialDays;
                planName = "Enterprise Plan";
                break;
            case "STARTER":
            default:
                plan = "STARTER";
                employeeLimit = starterEmployeeLimit;
                trialDays = starterTrialDays;
                planName = "Starter Plan";
                break;
        }

        Subscription sub = new Subscription();
        sub.setOrganization(organization);
        sub.setPlanCode(plan);
        sub.setPlanName(planName);
        sub.setStatus(SubscriptionStatus.TRIAL);
        sub.setStartDate(LocalDate.now());
        sub.setExpiryDate(LocalDate.now().plusDays(trialDays));
        sub.setTrial(true);
        sub.setTrialEnd(LocalDate.now().plusDays(trialDays));
        sub.setEmployeeLimit(employeeLimit);
        sub.setAutoRenew(false);
        sub.setBillingInfo(new HashMap<>());
        sub.setLimitsInfo(new HashMap<>());
        sub.setFeaturesInfo(new HashMap<>());
        sub.setPaymentInfo(new HashMap<>());
        sub.setNotes("14-day trial subscription provisioned automatically on signup.");

        return subscriptionRepository.save(sub);
    }
}
