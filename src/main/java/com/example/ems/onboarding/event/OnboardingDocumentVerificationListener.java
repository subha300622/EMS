package com.example.ems.onboarding.event;

import com.example.ems.onboarding.entity.OnboardingDocument;
import com.example.ems.onboarding.repository.OnboardingDocumentRepository;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.service.TeamOnboardingService;
import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import com.example.ems.finance.repository.EmployeeFinanceOnboardingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class OnboardingDocumentVerificationListener {

    @Autowired
    private OnboardingDocumentRepository onboardingDocumentRepository;

    @Autowired
    private EmployeeFinanceOnboardingRepository financeOnboardingRepository;

    @Autowired
    private TeamOnboardingService teamOnboardingService;

    @Autowired
    private com.example.ems.onboarding.service.OnboardingService onboardingService;

    @EventListener
    @Transactional
    public void handleDocumentVerified(DocumentVerifiedEvent event) {
        Onboarding onboarding = null;
        try {
            OnboardingDocument doc = onboardingDocumentRepository.findById(event.getDocumentId()).orElse(null);
            if (doc == null) {
                return;
            }

            onboarding = doc.getOnboarding();
            if (onboarding == null || onboarding.getEmployee() == null) {
                return;
            }

            Long employeeId = onboarding.getEmployee().getId();
            String documentType = doc.getDocumentType();
            String status = event.getStatus();
            String notes = event.getNotes();

            if (documentType != null) {
                Optional<EmployeeFinanceOnboarding> financeOpt = financeOnboardingRepository.findByEmployeeId(employeeId);
                if (financeOpt.isPresent()) {
                    EmployeeFinanceOnboarding finance = financeOpt.get();
                    boolean updated = false;

                    switch (documentType.toUpperCase()) {
                        case "BANK":
                            finance.setBankVerificationStatus(status);
                            finance.setBankVerificationNotes(notes);
                            updated = true;
                            break;
                        case "PAN":
                            finance.setPanVerificationStatus(status);
                            finance.setPanVerificationNotes(notes);
                            updated = true;
                            break;
                        case "UAN":
                            finance.setUanVerificationStatus(status);
                            finance.setUanVerificationNotes(notes);
                            updated = true;
                            break;
                    }

                    if (updated) {
                        // Update main status based on verification checks
                        if ("VERIFIED".equalsIgnoreCase(finance.getBankVerificationStatus()) &&
                            "VERIFIED".equalsIgnoreCase(finance.getPanVerificationStatus()) &&
                            "VERIFIED".equalsIgnoreCase(finance.getUanVerificationStatus())) {
                            finance.setStatus("APPROVED");
                        } else if ("REJECTED".equalsIgnoreCase(finance.getBankVerificationStatus()) ||
                                   "REJECTED".equalsIgnoreCase(finance.getPanVerificationStatus()) ||
                                   "REJECTED".equalsIgnoreCase(finance.getUanVerificationStatus())) {
                            finance.setStatus("SENT_BACK");
                        }
                        financeOnboardingRepository.save(finance);
                    }
                }
            }

            // Trigger standard checks
            teamOnboardingService.reconcileProgressCache(onboarding.getId());

            // Log event log projection SUCCESS
            onboardingService.logEvent(onboarding.getId(), "DOCUMENT_VERIFIED",
                    "Document ID: " + event.getDocumentId() + ", Status: " + event.getStatus(),
                    "SUCCESS", null, 0, null, false);

        } catch (Exception e) {
            if (onboarding != null) {
                onboardingService.logEvent(onboarding.getId(), "DOCUMENT_VERIFIED",
                        "Document ID: " + event.getDocumentId() + ", Status: " + event.getStatus(),
                        "FAILED", e.getMessage(), 1, "INTEGRATION", true);
            }
            throw e;
        }
    }
}
