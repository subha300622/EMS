package com.example.ems.onboarding.scheduler;

import com.example.ems.onboarding.entity.OnboardingTemplate;
import com.example.ems.onboarding.entity.OnboardingTemplateTask;
import com.example.ems.onboarding.repository.OnboardingTemplateRepository;
import com.example.ems.onboarding.repository.OnboardingTemplateTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Order(10)
public class OnboardingTemplateSeeder implements CommandLineRunner {

    @Autowired
    private OnboardingTemplateRepository templateRepository;

    @Autowired
    private OnboardingTemplateTaskRepository templateTaskRepository;

    @Override
    public void run(String... args) throws Exception {
        if (templateRepository.count() > 0) {
            System.out.println("OnboardingTemplateSeeder: Template already seeded.");
            return;
        }

        System.out.println("OnboardingTemplateSeeder: Seeding default versioned onboarding template...");

        OnboardingTemplate template = new OnboardingTemplate();
        template.setName("Standard Employee Onboarding Template");
        template.setVersion(1);
        template.setActive(true);
        template.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        OnboardingTemplate savedTemplate = templateRepository.save(template);

        // Pre-Joining Phase Tasks (6 tasks)
        seedTask(savedTemplate, "Submit Personal Documents", "Please upload your Government ID and Bank Account details.", "PRE_JOINING", "EMPLOYEE", "1 hr", -3, "HIGH", 24);
        seedTask(savedTemplate, "Sign NDA and Offer Letter", "Review and sign employment agreement.", "PRE_JOINING", "EMPLOYEE", "30 mins", -5, "CRITICAL", 48);
        seedTask(savedTemplate, "Background Verification", "HR triggers background screening check.", "PRE_JOINING", "HR", "15 mins", -2, "MEDIUM", 120);
        seedTask(savedTemplate, "IT Asset Allocation Request", "Allocate laptop and standard work setup.", "PRE_JOINING", "IT", "20 mins", -4, "HIGH", 72);
        seedTask(savedTemplate, "Corporate Email Setup", "Provision company corporate email address.", "PRE_JOINING", "IT", "15 mins", -1, "CRITICAL", 24);
        seedTask(savedTemplate, "Bank Account Details Submission", "Submit details for salary credit.", "PRE_JOINING", "EMPLOYEE", "20 mins", -3, "MEDIUM", 72);

        // Day 1 Phase Tasks (7 tasks)
        seedTask(savedTemplate, "Welcome 1:1 Meeting", "Initial manager welcome check-in.", "DAY_1", "MANAGER", "30 mins", 0, "HIGH", 8);
        seedTask(savedTemplate, "Workspace Tour and Team Introduction", "Guided tour of the floor and team intro.", "DAY_1", "MANAGER", "45 mins", 0, "LOW", 8);
        seedTask(savedTemplate, "Collect Access Badge", "Collect physical office entry badge.", "DAY_1", "ADMIN", "15 mins", 0, "MEDIUM", 4);
        seedTask(savedTemplate, "Day 1 HR Briefing & Orientation", "HR orientation and portal registration.", "DAY_1", "HR", "2 hrs", 0, "HIGH", 8);
        seedTask(savedTemplate, "Login Credential Activation", "Activate Active Directory and VPN access.", "DAY_1", "IT", "30 mins", 0, "CRITICAL", 4);
        seedTask(savedTemplate, "Verify Personal Details in EMS Portal", "Confirm details in the system portal.", "DAY_1", "EMPLOYEE", "20 mins", 0, "MEDIUM", 12);
        seedTask(savedTemplate, "Setup Developer Environment", "Clone repo, build code, install SDKs.", "DAY_1", "EMPLOYEE", "3 hrs", 0, "HIGH", 16);

        // Week 1 Phase Tasks (8 tasks)
        seedTask(savedTemplate, "Complete Security Compliance Training", "Mandatory security awareness training.", "WEEK_1", "EMPLOYEE", "2 hrs", 7, "HIGH", 168);
        seedTask(savedTemplate, "Review Team Goals & OKRs", "Align on performance deliverables.", "WEEK_1", "EMPLOYEE", "1 hr", 5, "MEDIUM", 120);
        seedTask(savedTemplate, "Install Required Development Tools", "Get compiler licenses and tools.", "WEEK_1", "EMPLOYEE", "1 hr", 3, "LOW", 72);
        seedTask(savedTemplate, "1:1 Sync with Onboarding Buddy", "Casual coffee chat and buddy check-in.", "WEEK_1", "EMPLOYEE", "30 mins", 4, "LOW", 96);
        seedTask(savedTemplate, "EMS Time Logging Training", "Learn how to record weekly timecards.", "WEEK_1", "EMPLOYEE", "30 mins", 6, "MEDIUM", 144);
        seedTask(savedTemplate, "Week 1 Manager Retrospective", "Feedback session on first week experience.", "WEEK_1", "MANAGER", "30 mins", 7, "HIGH", 48);
        seedTask(savedTemplate, "Review Project Documentation", "Read documentation on architecture.", "WEEK_1", "EMPLOYEE", "4 hrs", 4, "LOW", 96);
        seedTask(savedTemplate, "Understand Code Review Process", "Learn about code styling rules.", "WEEK_1", "EMPLOYEE", "1 hr", 5, "MEDIUM", 120);

        // Month 1 Phase Tasks (12 tasks)
        seedTask(savedTemplate, "Complete Employee Code of Conduct", "Ethics and compliance verification.", "MONTH_1", "EMPLOYEE", "1 hr", 14, "HIGH", 336);
        seedTask(savedTemplate, "First Project Contribution", "Submit first pull request in repository.", "MONTH_1", "EMPLOYEE", "8 hrs", 21, "HIGH", 504);
        seedTask(savedTemplate, "Day 30 HR Check-in", "HR survey on onboarding feedback.", "MONTH_1", "HR", "30 mins", 30, "MEDIUM", 48);
        seedTask(savedTemplate, "Day 30 Manager Performance Review", "Review progress against goals.", "MONTH_1", "MANAGER", "1 hr", 30, "HIGH", 48);
        seedTask(savedTemplate, "Share Onboarding Process Feedback", "Complete checklist survey form.", "MONTH_1", "EMPLOYEE", "15 mins", 28, "LOW", 72);
        seedTask(savedTemplate, "Attend Department Welcome Lunch", "Informal department team lunch.", "MONTH_1", "MANAGER", "1 hr", 15, "LOW", 168);
        seedTask(savedTemplate, "Read Architecture Guidelines", "In-depth codebase guide.", "MONTH_1", "EMPLOYEE", "2 hrs", 12, "LOW", 288);
        seedTask(savedTemplate, "Request Database Production Read Access", "Obtain read permissions for databases.", "MONTH_1", "IT", "15 mins", 10, "MEDIUM", 120);
        seedTask(savedTemplate, "Setup 1:1s with Cross-functional Leads", "Meet with product and design leads.", "MONTH_1", "EMPLOYEE", "2 hrs", 20, "LOW", 480);
        seedTask(savedTemplate, "Shadow Senior Engineer on Code Review", "Learn style guidelines in practice.", "MONTH_1", "EMPLOYEE", "2 hrs", 15, "MEDIUM", 360);
        seedTask(savedTemplate, "Understand Security & PII Policies", "Training on handling personal data.", "MONTH_1", "EMPLOYEE", "1 hr", 18, "HIGH", 432);
        seedTask(savedTemplate, "First Bug Fix Deployment to Sandbox", "Build and deploy patch fix.", "MONTH_1", "EMPLOYEE", "4 hrs", 25, "MEDIUM", 600);

        System.out.println("OnboardingTemplateSeeder: Default onboarding template tasks successfully seeded.");
    }

    private void seedTask(OnboardingTemplate template, String title, String description, String phase, String owner, String estTime, int dueDays, String priority, int slaHours) {
        OnboardingTemplateTask task = new OnboardingTemplateTask();
        task.setTemplate(template);
        task.setTitle(title);
        task.setDescription(description);
        task.setPhase(phase);
        task.setOwner(owner);
        task.setEstimatedTime(estTime);
        task.setDueDaysAfterJoining(dueDays);
        task.setPriority(priority);
        task.setSlaHours(slaHours);
        templateTaskRepository.save(task);
    }
}
