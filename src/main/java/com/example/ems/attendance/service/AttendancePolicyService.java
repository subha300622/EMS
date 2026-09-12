package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.policy.AttendancePolicyDto;
import com.example.ems.attendance.dto.policy.CreateAttendancePolicyRequest;
import com.example.ems.attendance.dto.policy.UpdateAttendancePolicyRequest;
import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.entity.AttendancePolicyStatus;
import com.example.ems.attendance.entity.ExceedGraceAction;
import com.example.ems.attendance.entity.GracePeriodType;
import com.example.ems.attendance.exception.AttendanceNotFoundException;
import com.example.ems.attendance.repository.AttendancePolicyRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class AttendancePolicyService {

    private static final Logger log = LoggerFactory.getLogger(AttendancePolicyService.class);

    @Autowired
    private AttendancePolicyRepository attendancePolicyRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public AttendancePolicy getActivePolicy() {
        Long organizationId = TenantContext.requireOrganizationId();
        return getActivePolicy(organizationId);
    }

    /**
     * Resolves the active attendance policy for the given organization.
     * If no active policy is configured in the database, returns a programmatic system default policy.
     */
    @Transactional(readOnly = true)
    public AttendancePolicy getActivePolicy(Long organizationId) {
        if (organizationId != null) {
            List<AttendancePolicy> activePolicies = attendancePolicyRepository.findActivePoliciesForOrganization(organizationId);
            if (!activePolicies.isEmpty()) {
                return activePolicies.get(0);
            }
        }
        return createSystemDefaultPolicy(organizationId);
    }

    public AttendancePolicy createSystemDefaultPolicy(Long organizationId) {
        AttendancePolicy policy = new AttendancePolicy();
        policy.setName("System Standard Policy");
        policy.setOfficeStartTime(LocalTime.of(9, 0));
        policy.setOfficeEndTime(LocalTime.of(18, 0));
        policy.setGracePeriodMinutes(15);
        policy.setMinimumWorkingMinutes(480);
        policy.setHalfDayThreshold(240);
        policy.setLateThreshold(15);
        policy.setEarlyCheckoutThreshold(15);
        policy.setMaximumBreakMinutes(60);
        policy.setLateGraceMinutes(10);
        policy.setEarlyExitGraceMinutes(10);
        policy.setGraceOccurrencesPerPeriod(3);
        policy.setGracePeriodType(GracePeriodType.MONTHLY);
        policy.setAllowLateGrace(true);
        policy.setAllowEarlyExitGrace(true);
        policy.setExceedGraceAction(ExceedGraceAction.MARK_LATE);
        policy.setMaxMonthlyPermissions(4);
        policy.setMaxDailyPermissionMinutes(120);
        policy.setMaxMonthlyPermissionMinutes(480);
        policy.setStatus(AttendancePolicyStatus.ACTIVE);
        return policy;
    }

    @Transactional
    public AttendancePolicyDto createPolicy(CreateAttendancePolicyRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Organization not found with ID: " + organizationId));

        validatePolicyParameters(
                request.getOfficeStartTime(),
                request.getOfficeEndTime(),
                request.getGracePeriodMinutes(),
                request.getEarlyCheckoutThreshold(),
                request.getHalfDayThreshold(),
                request.getMinimumWorkingMinutes()
        );

        AttendancePolicy policy = new AttendancePolicy();
        policy.setOrganization(organization);
        policy.setName(request.getName());
        policy.setOfficeStartTime(request.getOfficeStartTime());
        policy.setOfficeEndTime(request.getOfficeEndTime());
        policy.setGracePeriodMinutes(request.getGracePeriodMinutes() != null ? request.getGracePeriodMinutes() : 15);
        policy.setMinimumWorkingMinutes(request.getMinimumWorkingMinutes() != null ? request.getMinimumWorkingMinutes() : 480);
        policy.setHalfDayThreshold(request.getHalfDayThreshold() != null ? request.getHalfDayThreshold() : 240);
        policy.setLateThreshold(request.getLateThreshold() != null ? request.getLateThreshold() : 15);
        policy.setEarlyCheckoutThreshold(request.getEarlyCheckoutThreshold() != null ? request.getEarlyCheckoutThreshold() : 15);
        policy.setMaximumBreakMinutes(request.getMaximumBreakMinutes() != null ? request.getMaximumBreakMinutes() : 60);

        policy.setLateGraceMinutes(request.getLateGraceMinutes() != null ? request.getLateGraceMinutes() : 10);
        policy.setEarlyExitGraceMinutes(request.getEarlyExitGraceMinutes() != null ? request.getEarlyExitGraceMinutes() : 10);
        policy.setGraceOccurrencesPerPeriod(request.getGraceOccurrencesPerPeriod() != null ? request.getGraceOccurrencesPerPeriod() : 3);
        policy.setGracePeriodType(request.getGracePeriodType() != null ? request.getGracePeriodType() : GracePeriodType.MONTHLY);
        policy.setAllowLateGrace(request.getAllowLateGrace() != null ? request.getAllowLateGrace() : true);
        policy.setAllowEarlyExitGrace(request.getAllowEarlyExitGrace() != null ? request.getAllowEarlyExitGrace() : true);
        policy.setExceedGraceAction(request.getExceedGraceAction() != null ? request.getExceedGraceAction() : ExceedGraceAction.MARK_LATE);
        policy.setMaxMonthlyPermissions(request.getMaxMonthlyPermissions() != null ? request.getMaxMonthlyPermissions() : 4);
        policy.setMaxDailyPermissionMinutes(request.getMaxDailyPermissionMinutes() != null ? request.getMaxDailyPermissionMinutes() : 120);
        policy.setMaxMonthlyPermissionMinutes(request.getMaxMonthlyPermissionMinutes() != null ? request.getMaxMonthlyPermissionMinutes() : 480);

        policy.setStatus(AttendancePolicyStatus.DRAFT);

        policy = attendancePolicyRepository.save(policy);
        return AttendancePolicyDto.fromEntity(policy);
    }

    @Transactional(readOnly = true)
    public AttendancePolicyDto getPolicyById(Long id) {
        Long organizationId = TenantContext.requireOrganizationId();
        AttendancePolicy policy = attendancePolicyRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance policy not found with ID: " + id));
        return AttendancePolicyDto.fromEntity(policy);
    }

    @Transactional(readOnly = true)
    public Page<AttendancePolicyDto> getAllPolicies(AttendancePolicyStatus status, int page, int size) {
        Long organizationId = TenantContext.requireOrganizationId();
        int safePage = Math.max(0, page);
        int safeSize = (size <= 0 || size > 100) ? 20 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<AttendancePolicy> resultPage;
        if (status != null) {
            resultPage = attendancePolicyRepository.findByOrganizationIdAndStatus(organizationId, status, pageable);
        } else {
            resultPage = attendancePolicyRepository.findByOrganizationId(organizationId, pageable);
        }
        return resultPage.map(AttendancePolicyDto::fromEntity);
    }

    @Transactional
    public AttendancePolicyDto updatePolicy(Long id, UpdateAttendancePolicyRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        AttendancePolicy policy = attendancePolicyRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance policy not found with ID: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            policy.setName(request.getName());
        }
        if (request.getOfficeStartTime() != null) {
            policy.setOfficeStartTime(request.getOfficeStartTime());
        }
        if (request.getOfficeEndTime() != null) {
            policy.setOfficeEndTime(request.getOfficeEndTime());
        }
        validatePolicyParameters(
                policy.getOfficeStartTime(),
                policy.getOfficeEndTime(),
                policy.getGracePeriodMinutes(),
                policy.getEarlyCheckoutThreshold(),
                policy.getHalfDayThreshold(),
                policy.getMinimumWorkingMinutes()
        );

        if (request.getGracePeriodMinutes() != null) policy.setGracePeriodMinutes(request.getGracePeriodMinutes());
        if (request.getMinimumWorkingMinutes() != null) policy.setMinimumWorkingMinutes(request.getMinimumWorkingMinutes());
        if (request.getHalfDayThreshold() != null) policy.setHalfDayThreshold(request.getHalfDayThreshold());
        if (request.getLateThreshold() != null) policy.setLateThreshold(request.getLateThreshold());
        if (request.getEarlyCheckoutThreshold() != null) policy.setEarlyCheckoutThreshold(request.getEarlyCheckoutThreshold());
        if (request.getMaximumBreakMinutes() != null) policy.setMaximumBreakMinutes(request.getMaximumBreakMinutes());

        if (request.getLateGraceMinutes() != null) policy.setLateGraceMinutes(request.getLateGraceMinutes());
        if (request.getEarlyExitGraceMinutes() != null) policy.setEarlyExitGraceMinutes(request.getEarlyExitGraceMinutes());
        if (request.getGraceOccurrencesPerPeriod() != null) policy.setGraceOccurrencesPerPeriod(request.getGraceOccurrencesPerPeriod());
        if (request.getGracePeriodType() != null) policy.setGracePeriodType(request.getGracePeriodType());
        if (request.getAllowLateGrace() != null) policy.setAllowLateGrace(request.getAllowLateGrace());
        if (request.getAllowEarlyExitGrace() != null) policy.setAllowEarlyExitGrace(request.getAllowEarlyExitGrace());
        if (request.getExceedGraceAction() != null) policy.setExceedGraceAction(request.getExceedGraceAction());
        if (request.getMaxMonthlyPermissions() != null) policy.setMaxMonthlyPermissions(request.getMaxMonthlyPermissions());
        if (request.getMaxDailyPermissionMinutes() != null) policy.setMaxDailyPermissionMinutes(request.getMaxDailyPermissionMinutes());
        if (request.getMaxMonthlyPermissionMinutes() != null) policy.setMaxMonthlyPermissionMinutes(request.getMaxMonthlyPermissionMinutes());

        policy = attendancePolicyRepository.save(policy);
        return AttendancePolicyDto.fromEntity(policy);
    }

    @Transactional
    public AttendancePolicyDto activatePolicy(Long id) {
        Long organizationId = TenantContext.requireOrganizationId();
        AttendancePolicy policy = attendancePolicyRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance policy not found with ID: " + id));

        // Deactivate existing active policies for this tenant to ensure a clean single active policy
        List<AttendancePolicy> currentActive = attendancePolicyRepository.findActivePoliciesForOrganization(organizationId);
        for (AttendancePolicy p : currentActive) {
            if (!p.getId().equals(policy.getId())) {
                p.setStatus(AttendancePolicyStatus.INACTIVE);
                attendancePolicyRepository.save(p);
            }
        }

        policy.setStatus(AttendancePolicyStatus.ACTIVE);
        policy = attendancePolicyRepository.save(policy);
        log.info("Activated AttendancePolicy ID={} for Organization ID={}", policy.getId(), organizationId);
        return AttendancePolicyDto.fromEntity(policy);
    }

    @Transactional
    public AttendancePolicyDto deactivatePolicy(Long id) {
        Long organizationId = TenantContext.requireOrganizationId();
        AttendancePolicy policy = attendancePolicyRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new AttendanceNotFoundException("Attendance policy not found with ID: " + id));

        policy.setStatus(AttendancePolicyStatus.INACTIVE);
        policy = attendancePolicyRepository.save(policy);
        log.info("Deactivated AttendancePolicy ID={} for Organization ID={}", policy.getId(), organizationId);
        return AttendancePolicyDto.fromEntity(policy);
    }

    private void validatePolicyParameters(LocalTime start, LocalTime end, Integer gracePeriod, Integer earlyThreshold, Integer halfDayThreshold, Integer minimumWorkingMinutes) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new IllegalArgumentException("Office end time (" + end + ") must be after office start time (" + start + ").");
        }
        if (gracePeriod != null && gracePeriod < 0) {
            throw new IllegalArgumentException("Grace period minutes cannot be negative.");
        }
        if (earlyThreshold != null && earlyThreshold < 0) {
            throw new IllegalArgumentException("Early checkout threshold minutes cannot be negative.");
        }
        if (halfDayThreshold != null && halfDayThreshold < 0) {
            throw new IllegalArgumentException("Half day threshold minutes cannot be negative.");
        }
        if (minimumWorkingMinutes != null && minimumWorkingMinutes < 0) {
            throw new IllegalArgumentException("Minimum working minutes cannot be negative.");
        }
    }
}
