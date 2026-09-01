package com.example.ems.asset.service;

import com.example.ems.approval.dto.ApprovalActionRequest;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.asset.dto.AssetDtos.AssetActionResultResponse;
import com.example.ems.asset.entity.MyAsset;
import com.example.ems.asset.entity.MyAssetReturnRequest;
import com.example.ems.asset.repository.MyAssetRepository;
import com.example.ems.asset.repository.MyAssetReturnRequestRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AssetReturnRequestService {

    @Autowired
    private MyAssetReturnRequestRepository returnRequestRepository;

    @Autowired
    private MyAssetRepository myAssetRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired(required = false)
    private ApprovalWorkflowEngineService approvalEngineService;

    @Transactional(readOnly = true)
    public List<MyAssetReturnRequest> getReturnRequests(Long organizationId) {
        return returnRequestRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MyAssetReturnRequest getReturnRequestById(Long organizationId, Long requestId) {
        return returnRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return request not found with ID: " + requestId));
    }

    @Transactional
    public MyAssetReturnRequest createReturnRequest(Long organizationId, Long assetId, Long employeeId, String returnReason, String condition, List<String> accessories, String comments) {
        MyAsset asset = myAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with ID: " + assetId));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found with ID: " + employeeId));

        MyAssetReturnRequest request = new MyAssetReturnRequest();
        request.setReturnReference("RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.setAsset(asset);
        request.setEmployee(employee);
        request.setReturnReason(returnReason);
        request.setAssetCondition(condition != null ? condition : "GOOD");
        request.setAccessoriesReturned(accessories);
        request.setComments(comments);
        request.setStatus("DRAFT");
        return returnRequestRepository.save(request);
    }

    @Transactional
    public MyAssetReturnRequest updateReturnRequest(Long organizationId, Long requestId, String returnReason, String condition, List<String> accessories, String comments) {
        MyAssetReturnRequest request = getReturnRequestById(organizationId, requestId);
        if (!"DRAFT".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only DRAFT return requests can be updated");
        }
        if (returnReason != null) request.setReturnReason(returnReason);
        if (condition != null) request.setAssetCondition(condition);
        if (accessories != null) request.setAccessoriesReturned(accessories);
        if (comments != null) request.setComments(comments);
        return returnRequestRepository.save(request);
    }

    @Transactional
    public AssetActionResultResponse submitReturnRequest(Long organizationId, Long requestId, String performedBy) {
        MyAssetReturnRequest request = getReturnRequestById(organizationId, requestId);
        if (!"DRAFT".equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Return request #" + requestId + " is not in DRAFT status");
        }

        request.setStatus("PENDING_APPROVAL");
        if (approvalEngineService != null) {
            try {
                ApprovalWorkflowInstance instance = approvalEngineService.startWorkflow(
                        WorkflowType.ASSET_ASSIGNMENT_APPROVAL, "ASSET_RETURN_REQUEST", requestId.toString(), request.getEmployee(), null);
                if (instance != null) {
                    returnRequestRepository.save(request);
                    return new AssetActionResultResponse(requestId, null, "SUBMIT_RETURN", "PENDING_APPROVAL", true, instance.getId(), "Return request submitted for approval");
                }
            } catch (Exception ignored) {}
        }
        request.setStatus("SUBMITTED");
        returnRequestRepository.save(request);
        return new AssetActionResultResponse(requestId, null, "SUBMIT_RETURN", "SUBMITTED", false, null, "Return request submitted successfully");
    }

    @Autowired
    private com.example.ems.auth.repository.UserRepository userRepository;

    @Transactional
    public AssetActionResultResponse approveReturnRequest(Long organizationId, Long requestId, ApprovalActionRequest actionReq, String performedBy) {
        MyAssetReturnRequest request = getReturnRequestById(organizationId, requestId);
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
        request.setComments(comment);
        returnRequestRepository.save(request);
        return new AssetActionResultResponse(requestId, "RETURN_APPROVED", "Return request #" + requestId + " approved: " + comment);
    }

    @Transactional
    public AssetActionResultResponse rejectReturnRequest(Long organizationId, Long requestId, ApprovalActionRequest actionReq, String performedBy) {
        MyAssetReturnRequest request = getReturnRequestById(organizationId, requestId);
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
        request.setComments(comment);
        returnRequestRepository.save(request);
        return new AssetActionResultResponse(requestId, "RETURN_REJECTED", "Return request #" + requestId + " rejected: " + comment);
    }

    @Transactional
    public AssetActionResultResponse sendBackReturnRequest(Long organizationId, Long requestId, ApprovalActionRequest actionReq, String performedBy) {
        MyAssetReturnRequest request = getReturnRequestById(organizationId, requestId);
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
        request.setComments(comment);
        returnRequestRepository.save(request);
        return new AssetActionResultResponse(requestId, "RETURN_SENT_BACK", "Return request #" + requestId + " sent back: " + comment);
    }

    @Transactional
    public AssetActionResultResponse cancelReturnRequest(Long organizationId, Long requestId, String performedBy) {
        MyAssetReturnRequest request = getReturnRequestById(organizationId, requestId);
        request.setStatus("CANCELLED");
        returnRequestRepository.save(request);
        return new AssetActionResultResponse(requestId, "RETURN_CANCELLED", "Return request #" + requestId + " cancelled");
    }
}
