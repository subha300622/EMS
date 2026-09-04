package com.example.ems.asset.service;

import com.example.ems.approval.dto.ApprovalActionRequest;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.asset.dto.AssetDtos.AssetActionResultResponse;
import com.example.ems.asset.entity.MyAssetRequest;
import com.example.ems.asset.repository.MyAssetRequestRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AssetRequestService {

    @Autowired
    private MyAssetRequestRepository requestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired(required = false)
    private ApprovalWorkflowEngineService approvalEngineService;

    @Transactional(readOnly = true)
    public List<MyAssetRequest> getRequests(Long organizationId) {
        return requestRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MyAssetRequest getRequestById(Long organizationId, Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset request not found with ID: " + requestId));
    }

    @Transactional
    public MyAssetRequest createRequest(Long organizationId, Long employeeId, String category, String model, String reason, String priority, LocalDate requiredByDate) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found with ID: " + employeeId));

        MyAssetRequest request = new MyAssetRequest();
        request.setRequestNumber("REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.setEmployee(employee);
        request.setAssetCategory(category);
        request.setRequestedModel(model);
        request.setBusinessReason(reason);
        request.setPriority(priority != null ? priority : "MEDIUM");
        request.setRequiredByDate(requiredByDate != null ? requiredByDate : LocalDate.now().plusDays(7));
        request.setStatus("DRAFT");
        return requestRepository.save(request);
    }

    @Transactional
    public MyAssetRequest updateRequest(Long organizationId, Long requestId, String category, String model, String reason, String priority, LocalDate requiredByDate) {
        MyAssetRequest request = getRequestById(organizationId, requestId);
        if (!"DRAFT".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only DRAFT asset requests can be updated");
        }
        if (category != null) request.setAssetCategory(category);
        if (model != null) request.setRequestedModel(model);
        if (reason != null) request.setBusinessReason(reason);
        if (priority != null) request.setPriority(priority);
        if (requiredByDate != null) request.setRequiredByDate(requiredByDate);
        return requestRepository.save(request);
    }

    @Transactional
    public AssetActionResultResponse submitRequest(Long organizationId, Long requestId, String performedBy) {
        MyAssetRequest request = getRequestById(organizationId, requestId);
        if (!"DRAFT".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Request #" + requestId + " is not in DRAFT status");
        }

        request.setStatus("SUBMITTED");
        if (approvalEngineService != null) {
            try {
                ApprovalWorkflowInstance instance = approvalEngineService.startWorkflow(
                        WorkflowType.ASSET_ASSIGNMENT_APPROVAL, "ASSET_REQUEST", requestId.toString(), request.getEmployee(), null);
                if (instance != null) {
                    request.setStatus("PENDING_APPROVAL");
                    requestRepository.save(request);
                    return new AssetActionResultResponse(requestId, null, "SUBMIT", "PENDING_APPROVAL", true, instance.getId(), "Asset request submitted for approval");
                }
            } catch (Exception ignored) {}
        }
        request.setStatus("SUBMITTED");
        requestRepository.save(request);
        return new AssetActionResultResponse(requestId, null, "SUBMIT", "SUBMITTED", false, null, "Asset request submitted successfully");
    }

    @Autowired
    private com.example.ems.auth.repository.UserRepository userRepository;

    @Transactional
    public AssetActionResultResponse approveRequest(Long organizationId, Long requestId, ApprovalActionRequest actionReq, String performedBy) {
        MyAssetRequest request = getRequestById(organizationId, requestId);
        String comment = actionReq != null && actionReq.getComment() != null ? actionReq.getComment() : "Approved";

        if (approvalEngineService != null) {
            try {
                com.example.ems.auth.entity.User actor = userRepository.findByWorkEmail(performedBy).orElse(null);
                if (actor != null) {
                    approvalEngineService.approveInstanceTask(actor, requestId.toString(), comment);
                }
            } catch (Exception ignored) {}
        }
        request.setStatus("APPROVED");
        request.setManagerComments(comment);
        requestRepository.save(request);
        return new AssetActionResultResponse(requestId, "APPROVED", "Asset request #" + requestId + " approved: " + comment);
    }

    @Transactional
    public AssetActionResultResponse rejectRequest(Long organizationId, Long requestId, ApprovalActionRequest actionReq, String performedBy) {
        MyAssetRequest request = getRequestById(organizationId, requestId);
        String comment = actionReq != null && actionReq.getComment() != null ? actionReq.getComment() : "Rejected";

        if (approvalEngineService != null) {
            try {
                com.example.ems.auth.entity.User actor = userRepository.findByWorkEmail(performedBy).orElse(null);
                if (actor != null) {
                    approvalEngineService.rejectInstanceTask(actor, requestId.toString(), comment);
                }
            } catch (Exception ignored) {}
        }
        request.setStatus("REJECTED");
        request.setManagerComments(comment);
        requestRepository.save(request);
        return new AssetActionResultResponse(requestId, "REJECTED", "Asset request #" + requestId + " rejected: " + comment);
    }

    @Transactional
    public AssetActionResultResponse sendBackRequest(Long organizationId, Long requestId, ApprovalActionRequest actionReq, String performedBy) {
        MyAssetRequest request = getRequestById(organizationId, requestId);
        String comment = actionReq != null && actionReq.getComment() != null ? actionReq.getComment() : "Changes requested";

        if (approvalEngineService != null) {
            try {
                com.example.ems.auth.entity.User actor = userRepository.findByWorkEmail(performedBy).orElse(null);
                if (actor != null) {
                    approvalEngineService.requestChanges(actor, requestId.toString(), comment);
                }
            } catch (Exception ignored) {}
        }
        request.setStatus("CHANGES_REQUESTED");
        request.setManagerComments(comment);
        requestRepository.save(request);
        return new AssetActionResultResponse(requestId, "CHANGES_REQUESTED", "Asset request #" + requestId + " sent back: " + comment);
    }

    @Transactional
    public AssetActionResultResponse cancelRequest(Long organizationId, Long requestId, String performedBy) {
        MyAssetRequest request = getRequestById(organizationId, requestId);
        request.setStatus("CANCELLED");
        requestRepository.save(request);
        return new AssetActionResultResponse(requestId, "CANCELLED", "Asset request #" + requestId + " cancelled");
    }
}
