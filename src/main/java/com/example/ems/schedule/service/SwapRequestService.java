package com.example.ems.schedule.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.schedule.dto.ShiftSwapRequestDto;
import com.example.ems.schedule.dto.ShiftSwapUserDto;
import com.example.ems.schedule.entity.MyScheduleChangeRequest;
import com.example.ems.schedule.entity.MyShift;
import com.example.ems.schedule.entity.MyScheduleTimelineEvent;
import com.example.ems.schedule.entity.MyShiftTemplate;
import com.example.ems.schedule.repository.MyScheduleChangeRequestRepository;
import com.example.ems.schedule.repository.MyShiftRepository;
import com.example.ems.schedule.repository.MyScheduleTimelineEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SwapRequestService {

    @Autowired
    private MyScheduleChangeRequestRepository changeRequestRepository;

    @Autowired
    private MyShiftRepository shiftRepository;

    @Autowired
    private MyScheduleTimelineEventRepository timelineRepository;

    @Autowired
    private com.example.ems.employee.repository.EmployeeRepository employeeRepository;

    public List<ShiftSwapRequestDto> getSwapRequests(List<MyScheduleChangeRequest> changeRequests) {
        return changeRequests.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public void approveSwap(Long requestId) {
        MyScheduleChangeRequest req = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Swap request not found with ID: " + requestId));

        if (!"PENDING_MANAGER_APPROVAL".equalsIgnoreCase(req.getStatus())) {
            throw new IllegalStateException("Swap request has already been processed. Current status: " + req.getStatus());
        }

        req.setStatus("APPROVED");
        changeRequestRepository.save(req);

        // Apply shift change
        Employee employee = req.getEmployee();
        MyShiftTemplate requestedTemplate = req.getRequestedShift();

        Optional<MyShift> existingShift = shiftRepository.findByEmployeeEmailAndDate(employee.getEmail(), req.getRequestedDate());
        if (requestedTemplate == null || requestedTemplate.getId() == 105L || "NONE".equalsIgnoreCase(requestedTemplate.getName())) {
            existingShift.ifPresent(shiftRepository::delete);
        } else {
            MyShift shift = existingShift.orElseGet(() -> {
                MyShift s = new MyShift();
                s.setEmployee(employee);
                s.setDate(req.getRequestedDate());
                return s;
            });
            shift.setTemplate(requestedTemplate);
            shift.setStatus("ASSIGNED");
            shift.setUpdatedAt(LocalDateTime.now());
            shiftRepository.save(shift);
        }

        // Timeline event
        MyScheduleTimelineEvent event = new MyScheduleTimelineEvent();
        event.setEmployee(employee);
        event.setEvent("SHIFT_CHANGE_APPROVED");
        event.setPerformedBy("Manager");
        event.setPerformedAt(LocalDateTime.now());
        event.setDescription("Shift change approved to " + (requestedTemplate != null ? requestedTemplate.getName() : "NONE") + " on " + req.getRequestedDate());
        timelineRepository.save(event);
    }

    @Transactional
    public void rejectSwap(Long requestId) {
        MyScheduleChangeRequest req = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Swap request not found with ID: " + requestId));

        if (!"PENDING_MANAGER_APPROVAL".equalsIgnoreCase(req.getStatus())) {
            throw new IllegalStateException("Swap request has already been processed. Current status: " + req.getStatus());
        }

        req.setStatus("REJECTED");
        changeRequestRepository.save(req);

        // Timeline event
        MyScheduleTimelineEvent event = new MyScheduleTimelineEvent();
        event.setEmployee(req.getEmployee());
        event.setEvent("SHIFT_CHANGE_REJECTED");
        event.setPerformedBy("Manager");
        event.setPerformedAt(LocalDateTime.now());
        event.setDescription("Shift change request rejected on " + req.getRequestedDate());
        timelineRepository.save(event);
    }

    private ShiftSwapRequestDto mapToDto(MyScheduleChangeRequest req) {
        ShiftSwapRequestDto dto = new ShiftSwapRequestDto();
        dto.setRequestId(req.getId());

        String status = "PENDING";
        if ("APPROVED".equalsIgnoreCase(req.getStatus())) {
            status = "APPROVED";
        } else if ("REJECTED".equalsIgnoreCase(req.getStatus())) {
            status = "REJECTED";
        }
        dto.setStatus(status);

        if (req.getEmployee() != null) {
            Employee emp = req.getEmployee();
            dto.setRequester(new ShiftSwapUserDto(emp.getId(), emp.getFullName(), emp.getDesignation(), emp.getDepartment()));
            Employee mgr = emp.getManager();
            if (mgr == null) {
                mgr = employeeRepository.findByEmail("manager@company.com").orElse(null);
            }
            if (mgr != null) {
                dto.setReceiver(new ShiftSwapUserDto(mgr.getId(), mgr.getFullName(), mgr.getDesignation(), mgr.getDepartment()));
            } else {
                dto.setReceiver(new ShiftSwapUserDto(999L, "System Manager", "MANAGER", "Operations"));
            }
        }

        dto.setRequesterShiftType(req.getCurrentShift() != null ? mapTemplateToShiftType(req.getCurrentShift()) : "NONE");
        dto.setReceiverShiftType(req.getRequestedShift() != null ? mapTemplateToShiftType(req.getRequestedShift()) : "NONE");
        dto.setRequesterShiftDate(req.getRequestedDate() != null ? req.getRequestedDate().toString() : "");
        dto.setReceiverShiftDate(req.getRequestedDate() != null ? req.getRequestedDate().toString() : "");

        return dto;
    }

    private String mapTemplateToShiftType(MyShiftTemplate temp) {
        if (temp == null) return "NONE";
        if (temp.getId() == 101L || "GENERAL_SHIFT".equalsIgnoreCase(temp.getName())) return "FULL_DAY";
        if (temp.getId() == 102L || "EVENING_SHIFT".equalsIgnoreCase(temp.getName())) return "EVENING";
        if (temp.getId() == 103L || "NIGHT_SHIFT".equalsIgnoreCase(temp.getName())) return "NIGHT";
        if (temp.getId() == 104L || "MORNING_SHIFT".equalsIgnoreCase(temp.getName())) return "MORNING";
        return temp.getName();
    }
}
