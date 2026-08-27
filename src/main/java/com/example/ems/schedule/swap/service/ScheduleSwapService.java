package com.example.ems.schedule.swap.service;

import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.schedule.entity.Schedule;
import com.example.ems.schedule.entity.ScheduleStatus;
import com.example.ems.schedule.repository.ScheduleRepository;
import com.example.ems.schedule.swap.dto.*;
import com.example.ems.schedule.swap.entity.ScheduleSwapRequest;
import com.example.ems.schedule.swap.entity.ScheduleSwapStatus;
import com.example.ems.schedule.swap.repository.ScheduleSwapRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleSwapService {

    @Autowired
    private ScheduleSwapRequestRepository swapRequestRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ApprovalWorkflowEngineService approvalWorkflowEngineService;

    public Employee resolveEmployeeForUser(User user) {
        return approvalWorkflowEngineService.resolveEmployeeForUser(user);
    }

    public Long resolveOrganizationId(User user) {
        return approvalWorkflowEngineService.resolveOrganizationId(user);
    }

    private ScheduleSwapResponseDto mapToResponseDto(ScheduleSwapRequest req) {
        Schedule source = req.getSourceSchedule();
        Schedule target = req.getTargetSchedule();

        String srcEmpId = req.getSourceEmployee().getEmployeeId() != null ? req.getSourceEmployee().getEmployeeId() : "EMP-" + req.getSourceEmployee().getId();
        String tgtEmpId = req.getTargetEmployee().getEmployeeId() != null ? req.getTargetEmployee().getEmployeeId() : "EMP-" + req.getTargetEmployee().getId();

        ScheduleSwapResponseDto.ScheduleInfo srcInfo = new ScheduleSwapResponseDto.ScheduleInfo(
                source.getScheduleId(),
                srcEmpId,
                req.getSourceEmployee().getFullName(),
                source.getDate() != null ? source.getDate().toString() : null,
                source.getStartTime() != null ? source.getStartTime().toString() : null,
                source.getEndTime() != null ? source.getEndTime().toString() : null,
                source.getLocation()
        );

        ScheduleSwapResponseDto.ScheduleInfo tgtInfo = new ScheduleSwapResponseDto.ScheduleInfo(
                target.getScheduleId(),
                tgtEmpId,
                req.getTargetEmployee().getFullName(),
                target.getDate() != null ? target.getDate().toString() : null,
                target.getStartTime() != null ? target.getStartTime().toString() : null,
                target.getEndTime() != null ? target.getEndTime().toString() : null,
                target.getLocation()
        );

        ScheduleSwapResponseDto.WorkflowInfo wfInfo = new ScheduleSwapResponseDto.WorkflowInfo(
                req.getWorkflowInstanceId(),
                "SCHEDULE_SWAP",
                req.getStatus() == ScheduleSwapStatus.PENDING_APPROVAL ? "TARGET_EMPLOYEE_CONSENT" : req.getStatus().name()
        );

        ScheduleSwapResponseDto dto = new ScheduleSwapResponseDto();
        dto.setRequestId(req.getRequestId());
        dto.setSourceSchedule(srcInfo);
        dto.setTargetSchedule(tgtInfo);
        dto.setReason(req.getReason());
        dto.setStatus(req.getStatus());
        dto.setWorkflow(wfInfo);

        return dto;
    }

    @Transactional
    public ScheduleSwapResponseDto createSwapRequest(User currentUser, ScheduleSwapCreateRequest request) {
        Employee requester = resolveEmployeeForUser(currentUser);
        Long orgId = resolveOrganizationId(currentUser);

        if (request.getSourceScheduleId() == null || request.getSourceScheduleId().trim().isEmpty()) {
            throw new IllegalArgumentException("sourceScheduleId is required");
        }
        if (request.getTargetScheduleId() == null || request.getTargetScheduleId().trim().isEmpty()) {
            throw new IllegalArgumentException("targetScheduleId is required");
        }

        Schedule sourceSchedule = scheduleRepository.findByScheduleIdAndOrganizationId(request.getSourceScheduleId().trim(), orgId)
                .orElseThrow(() -> new IllegalArgumentException("Source schedule not found with ID: " + request.getSourceScheduleId()));

        Schedule targetSchedule = scheduleRepository.findByScheduleIdAndOrganizationId(request.getTargetScheduleId().trim(), orgId)
                .orElseThrow(() -> new IllegalArgumentException("Target schedule not found with ID: " + request.getTargetScheduleId()));

        // Ownership validation
        if (!sourceSchedule.getEmployee().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("Source schedule does not belong to the requester");
        }

        // Schedule status validation
        if (sourceSchedule.getStatus() != ScheduleStatus.SCHEDULED) {
            throw new IllegalArgumentException("Source schedule status must be SCHEDULED for swapping");
        }
        if (targetSchedule.getStatus() != ScheduleStatus.SCHEDULED) {
            throw new IllegalArgumentException("Target schedule status must be SCHEDULED for swapping");
        }

        // Distinct employees validation (SWAP_002)
        if (sourceSchedule.getEmployee().getId().equals(targetSchedule.getEmployee().getId())) {
            throw new IllegalArgumentException("Source and target schedules belong to the same employee");
        }

        // Bidirectional duplicate active request check (SWAP_003)
        if (swapRequestRepository.existsActiveSwapForSchedules(orgId, sourceSchedule.getId(), targetSchedule.getId())) {
            throw new IllegalStateException("An active swap request already exists for these schedules");
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseGet(() -> {
                    Organization o = new Organization();
                    o.setId(orgId);
                    o.setName("Default Organization");
                    return o;
                });

        String requestId = "SSR-" + String.format("%05d", System.currentTimeMillis() % 100000);

        ScheduleSwapRequest swapRequest = new ScheduleSwapRequest();
        swapRequest.setRequestId(requestId);
        swapRequest.setOrganization(org);
        swapRequest.setSourceSchedule(sourceSchedule);
        swapRequest.setSourceEmployee(requester);
        swapRequest.setTargetSchedule(targetSchedule);
        swapRequest.setTargetEmployee(targetSchedule.getEmployee());
        swapRequest.setReason(request.getReason());
        swapRequest.setStatus(ScheduleSwapStatus.PENDING_APPROVAL);
        swapRequest.setCreatedBy(requester);
        swapRequest.setCreatedAt(Instant.now());
        swapRequest.setUpdatedAt(Instant.now());

        ScheduleSwapRequest savedRequest = swapRequestRepository.save(swapRequest);

        // Start Workflow
        Map<String, Object> context = new HashMap<>();
        context.put("targetEmployeeId", targetSchedule.getEmployee().getId());
        context.put("sourceEmployeeId", requester.getId());

        ApprovalWorkflowInstance instance = approvalWorkflowEngineService.startWorkflow(
                WorkflowType.SCHEDULE_SWAP,
                "SCHEDULE_SWAP_REQUEST",
                requestId,
                requester,
                context
        );

        savedRequest.setWorkflowInstanceId(instance.getWorkflowInstanceId());
        swapRequestRepository.save(savedRequest);

        return mapToResponseDto(savedRequest);
    }

    public ScheduleSwapListResponse getSwapRequests(User currentUser, int page, int size) {
        Long orgId = resolveOrganizationId(currentUser);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ScheduleSwapRequest> pageResult = swapRequestRepository.findByOrganizationId(orgId, pageable);
        List<ScheduleSwapResponseDto> dtos = pageResult.getContent().stream().map(this::mapToResponseDto).collect(Collectors.toList());

        return new ScheduleSwapListResponse(dtos, pageResult.getTotalElements(), pageResult.getNumber(), pageResult.getSize());
    }

    public ScheduleSwapListResponse getMySwapRequests(User currentUser, int page, int size) {
        Employee currentEmp = resolveEmployeeForUser(currentUser);
        Long orgId = resolveOrganizationId(currentUser);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ScheduleSwapRequest> pageResult = swapRequestRepository.findMySwapRequests(orgId, currentEmp.getId(), pageable);
        List<ScheduleSwapResponseDto> dtos = pageResult.getContent().stream().map(this::mapToResponseDto).collect(Collectors.toList());

        return new ScheduleSwapListResponse(dtos, pageResult.getTotalElements(), pageResult.getNumber(), pageResult.getSize());
    }

    public ScheduleSwapResponseDto getSwapRequestById(User currentUser, String requestId) {
        Long orgId = resolveOrganizationId(currentUser);
        ScheduleSwapRequest req = swapRequestRepository.findByRequestIdAndOrganizationId(requestId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Swap request not found with ID: " + requestId));
        return mapToResponseDto(req);
    }

    @Transactional
    public ScheduleSwapResponseDto cancelSwapRequest(User currentUser, String requestId) {
        Employee currentEmp = resolveEmployeeForUser(currentUser);
        Long orgId = resolveOrganizationId(currentUser);

        ScheduleSwapRequest req = swapRequestRepository.findByRequestIdAndOrganizationId(requestId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Swap request not found with ID: " + requestId));

        if (!req.getCreatedBy().getId().equals(currentEmp.getId()) && !req.getSourceEmployee().getId().equals(currentEmp.getId())) {
            throw new IllegalArgumentException("Only the swap requester can cancel this request");
        }

        if (req.getStatus() == ScheduleSwapStatus.COMPLETED || req.getStatus() == ScheduleSwapStatus.CANCELLED) {
            throw new IllegalStateException("Swap request cannot be cancelled in status: " + req.getStatus());
        }

        req.setStatus(ScheduleSwapStatus.CANCELLED);
        req.setUpdatedAt(Instant.now());
        swapRequestRepository.save(req);

        return mapToResponseDto(req);
    }

    @Transactional
    public void executeAtomicSwap(String requestId) {
        ScheduleSwapRequest req = swapRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Swap request not found for ID: " + requestId));

        if (req.getStatus() == ScheduleSwapStatus.COMPLETED) {
            return; // Already completed
        }

        Schedule source = req.getSourceSchedule();
        Schedule target = req.getTargetSchedule();

        // Reload fresh from repository to check status
        source = scheduleRepository.findById(source.getId())
                .orElseThrow(() -> new IllegalStateException("Source schedule no longer exists"));
        target = scheduleRepository.findById(target.getId())
                .orElseThrow(() -> new IllegalStateException("Target schedule no longer exists"));

        if (source.getStatus() != ScheduleStatus.SCHEDULED || target.getStatus() != ScheduleStatus.SCHEDULED) {
            req.setStatus(ScheduleSwapStatus.FAILED);
            req.setUpdatedAt(Instant.now());
            swapRequestRepository.save(req);
            throw new IllegalStateException("One or both schedules are no longer in SCHEDULED status");
        }

        // Swap shift details without swapping employee_id
        LocalDate tempDate = source.getDate();
        LocalTime tempStart = source.getStartTime();
        LocalTime tempEnd = source.getEndTime();
        String tempLoc = source.getLocation();
        String tempNotes = source.getNotes();

        source.setDate(target.getDate());
        source.setStartTime(target.getStartTime());
        source.setEndTime(target.getEndTime());
        source.setLocation(target.getLocation());
        source.setNotes(target.getNotes());

        target.setDate(tempDate);
        target.setStartTime(tempStart);
        target.setEndTime(tempEnd);
        target.setLocation(tempLoc);
        target.setNotes(tempNotes);

        scheduleRepository.save(source);
        scheduleRepository.save(target);

        req.setStatus(ScheduleSwapStatus.COMPLETED);
        req.setCompletedAt(Instant.now());
        req.setUpdatedAt(Instant.now());
        swapRequestRepository.save(req);
    }
}
