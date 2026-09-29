package com.example.ems.employee.service.actioncenter;

import com.example.ems.employee.dto.dashboard.EmployeeActionDto;
import com.example.ems.employee.dto.dashboard.EmployeeActionLinkDto;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.MyDocumentType;
import com.example.ems.employee.entity.MyEmployeeDocument;
import com.example.ems.employee.repository.MyDocumentTypeRepository;
import com.example.ems.employee.repository.MyEmployeeDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DocumentActionProvider implements EmployeeActionProvider {

    private static final Logger log = LoggerFactory.getLogger(DocumentActionProvider.class);

    @Autowired
    private MyDocumentTypeRepository documentTypeRepository;

    @Autowired
    private MyEmployeeDocumentRepository employeeDocumentRepository;

    @Override
    public List<EmployeeActionDto> getActions(Employee employee, Long orgId) {
        if (employee == null || employee.getId() == null) {
            return Collections.emptyList();
        }

        try {
            List<MyDocumentType> allTypes = documentTypeRepository.findAll();
            List<MyEmployeeDocument> userDocs = employeeDocumentRepository.findByEmployeeId(employee.getId());

            List<EmployeeActionDto> actions = new ArrayList<>();
            LocalDate today = LocalDate.now();

            // Set of type IDs that have at least one non-rejected document uploaded
            Set<Long> uploadedTypeIds = userDocs.stream()
                    .filter(d -> d.getDocumentType() != null)
                    .filter(d -> !"REJECTED".equalsIgnoreCase(d.getVerificationStatus()))
                    .map(d -> d.getDocumentType().getId())
                    .collect(Collectors.toSet());

            // 1. Missing mandatory documents according to EMS rules
            for (MyDocumentType type : allTypes) {
                if (type.isMandatory() && !uploadedTypeIds.contains(type.getId())) {
                    actions.add(new EmployeeActionDto(
                            "DOC-" + type.getId(),
                            "DOCUMENT_UPLOAD",
                            "Upload " + type.getName(),
                            "OVERDUE",
                            "HIGH",
                            null,
                            "Document required for statutory compliance",
                            new EmployeeActionLinkDto("View Task", "/employee/documents")
                    ));
                }
            }

            // 2. Uploaded documents that are rejected or expired/expiring
            for (MyEmployeeDocument doc : userDocs) {
                String typeName = doc.getDocumentType() != null ? doc.getDocumentType().getName() : "Document";

                if ("REJECTED".equalsIgnoreCase(doc.getVerificationStatus())) {
                    String desc = (doc.getVerificationRemarks() != null && !doc.getVerificationRemarks().isBlank())
                            ? doc.getVerificationRemarks()
                            : "Document rejected. Please re-upload a clear copy.";
                    actions.add(new EmployeeActionDto(
                            "DOC-" + doc.getId(),
                            "DOCUMENT_UPLOAD",
                            "Re-upload " + typeName,
                            "OVERDUE",
                            "HIGH",
                            null,
                            desc,
                            new EmployeeActionLinkDto("View Task", "/employee/documents")
                    ));
                } else if (doc.getExpiryDate() != null) {
                    if (doc.getExpiryDate().isBefore(today)) {
                        actions.add(new EmployeeActionDto(
                                "DOC-" + doc.getId(),
                                "DOCUMENT_UPLOAD",
                                "Renew " + typeName,
                                "OVERDUE",
                                "HIGH",
                                doc.getExpiryDate().toString(),
                                "Document expired on " + doc.getExpiryDate(),
                                new EmployeeActionLinkDto("View Task", "/employee/documents")
                        ));
                    } else if (doc.getExpiryDate().isBefore(today.plusDays(30))) {
                        actions.add(new EmployeeActionDto(
                                "DOC-" + doc.getId(),
                                "DOCUMENT_UPLOAD",
                                "Renew " + typeName,
                                "DUE_SOON",
                                "NORMAL",
                                doc.getExpiryDate().toString(),
                                "Document expires soon on " + doc.getExpiryDate(),
                                new EmployeeActionLinkDto("View Task", "/employee/documents")
                        ));
                    }
                }
            }

            return actions;
        } catch (Exception ex) {
            log.warn("Error calculating document actions for employee #{}: {}", employee.getId(), ex.getMessage());
            return Collections.emptyList();
        }
    }
}
