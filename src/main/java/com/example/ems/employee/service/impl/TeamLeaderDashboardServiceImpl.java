package com.example.ems.employee.service.impl;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.dto.teamleader.*;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.Team;
import com.example.ems.employee.entity.TeamMember;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.TeamMemberRepository;
import com.example.ems.employee.repository.TeamRepository;
import com.example.ems.employee.service.TeamLeaderDashboardService;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TeamLeaderDashboardServiceImpl implements TeamLeaderDashboardService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    private Long getTenantId() {
        return TenantContext.requireOrganizationId();
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
    }

    private Employee getCurrentEmployee(Long tenantId) {
        String email = getCurrentUserEmail();
        if (email == null) return null;
        return employeeRepository.findByEmailAndOrganizationId(email, tenantId)
                .or(() -> employeeRepository.findByEmail(email))
                .orElse(null);
    }

    private List<Team> findTeamsLedByEmployee(Employee tl, Long tenantId) {
        if (tl == null) return Collections.emptyList();
        List<Team> teams = teamRepository.findByTeamLeadIdAndOrganizationIdAndDeletedFalse(tl.getId(), tenantId);
        if (teams.isEmpty()) {
            teams = teamRepository.findByTeamLeadIdAndDeletedFalse(tl.getId());
        }
        return teams;
    }

    private List<Employee> getDirectReports(Employee tl, Long tenantId) {
        List<Team> teams = findTeamsLedByEmployee(tl, tenantId);
        if (teams.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> teamIds = teams.stream().map(Team::getId).distinct().toList();
        List<TeamMember> members = teamMemberRepository.findByTeamIdInAndStatus(teamIds, "ACTIVE");
        if (members.isEmpty()) {
            members = teamMemberRepository.findByTeamIdIn(teamIds);
        }

        // Filter out the TL if present and there are other members
        List<Employee> reports = members.stream()
                .map(TeamMember::getEmployee)
                .filter(Objects::nonNull)
                .filter(e -> tl == null || !e.getId().equals(tl.getId()))
                .distinct()
                .collect(Collectors.toList());

        if (reports.isEmpty() && !members.isEmpty()) {
            reports = members.stream()
                    .map(TeamMember::getEmployee)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return reports;
    }

    @Override
    public TeamLeaderDashboardResponse getDashboard() {
        Long tenantId = getTenantId();
        Employee tl = getCurrentEmployee(tenantId);
        List<Employee> directReports = getDirectReports(tl, tenantId);

        if (directReports.isEmpty()) {
            return new TeamLeaderDashboardResponse(
                    new TeamLeaderDashboardResponse.Summary(0, 0, 0, 0, 0,
                            new TeamLeaderDashboardResponse.TeamPerformance(0.0, 5.0, 0.0)),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        }

        int totalMembers = directReports.size();
        int activeDirectReports = totalMembers;
        List<Long> memberIds = directReports.stream().map(Employee::getId).toList();

        // 1. Attendance for today
        LocalDate today = LocalDate.now();
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(memberIds, today, tenantId);
        Map<Long, Attendance> attendanceMap = attendances.stream()
                .filter(a -> a.getEmployee() != null)
                .collect(Collectors.toMap(a -> a.getEmployee().getId(), a -> a, (a1, a2) -> a1));

        int presentToday = 0;
        for (Employee emp : directReports) {
            Attendance att = attendanceMap.get(emp.getId());
            if (att != null) {
                String s = att.getStatus();
                if ("PRESENT".equalsIgnoreCase(s) || "COMPLETED".equalsIgnoreCase(s) || "WORKING".equalsIgnoreCase(s)) {
                    presentToday++;
                }
            }
        }
        int attendancePercentage = totalMembers > 0 ? (int) Math.round((presentToday * 100.0) / totalMembers) : 0;

        // 2. Pending leaves
        List<Leave> pendingLeaves = leaveRepository.findByEmployeeIdInAndStatusIn(
                memberIds, List.of("PENDING_TL_RECOMMENDATION", "PENDING"));
        int pendingLeaveCount = pendingLeaves.size();

        // 3. Performance
        double totalRating = 0.0;
        int ratedCount = 0;
        for (Employee emp : directReports) {
            List<Appraisal> appraisals = appraisalRepository.findByOrganizationIdAndEmployeeId(tenantId, emp.getId());
            if (appraisals.isEmpty()) {
                appraisals = appraisalRepository.findByEmployeeId(emp.getId());
            }
            Double rating = appraisals.stream()
                    .map(Appraisal::getFinalRating)
                    .filter(Objects::nonNull)
                    .reduce((first, second) -> second)
                    .orElse(null);
            if (rating != null) {
                totalRating += rating;
                ratedCount++;
            }
        }
        double avgRating = ratedCount > 0 ? Math.round((totalRating / ratedCount) * 10.0) / 10.0 : 0.0;
        TeamLeaderDashboardResponse.TeamPerformance performance = new TeamLeaderDashboardResponse.TeamPerformance(
                avgRating, 5.0, avgRating);

        // 4. Team roster
        List<TeamMemberRosterDto> roster = directReports.stream().map(emp -> {
            Attendance att = attendanceMap.get(emp.getId());
            String attStatus = att != null && att.getStatus() != null ? att.getStatus() : "ABSENT";
            return new TeamMemberRosterDto(
                    emp.getId(),
                    emp.getEmployeeId(),
                    emp.getFullName(),
                    emp.getEmail(),
                    emp.getDepartment(),
                    emp.getDesignation(),
                    emp.getStatus(),
                    attStatus,
                    false
            );
        }).collect(Collectors.toList());

        // 5. Pending leave reviews
        List<TeamPendingLeaveReviewDto> leaveReviews = pendingLeaves.stream().map(l -> new TeamPendingLeaveReviewDto(
                l.getId(),
                l.getEmployee() != null ? l.getEmployee().getId() : null,
                l.getEmployee() != null ? l.getEmployee().getFullName() : null,
                l.getLeaveType() != null ? l.getLeaveType().getName() : null,
                l.getStartDate() != null ? l.getStartDate().toString() : null,
                l.getEndDate() != null ? l.getEndDate().toString() : null,
                l.getDurationDays(),
                l.getReason(),
                l.getStatus(),
                l.getAppliedAt() != null ? l.getAppliedAt().toString() : null
        )).collect(Collectors.toList());

        TeamLeaderDashboardResponse.Summary summary = new TeamLeaderDashboardResponse.Summary(
                totalMembers, activeDirectReports, presentToday, attendancePercentage, pendingLeaveCount, performance);

        return new TeamLeaderDashboardResponse(summary, roster, leaveReviews);
    }

    @Override
    public List<TeamMemberRosterDto> getTeamMembers() {
        Long tenantId = getTenantId();
        Employee tl = getCurrentEmployee(tenantId);
        List<Employee> directReports = getDirectReports(tl, tenantId);

        LocalDate today = LocalDate.now();
        List<Long> memberIds = directReports.stream().map(Employee::getId).toList();
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(memberIds, today, tenantId);
        Map<Long, Attendance> attendanceMap = attendances.stream()
                .filter(a -> a.getEmployee() != null)
                .collect(Collectors.toMap(a -> a.getEmployee().getId(), a -> a, (a1, a2) -> a1));

        return directReports.stream().map(emp -> {
            Attendance att = attendanceMap.get(emp.getId());
            String attStatus = att != null && att.getStatus() != null ? att.getStatus() : "ABSENT";
            return new TeamMemberRosterDto(
                    emp.getId(),
                    emp.getEmployeeId(),
                    emp.getFullName(),
                    emp.getEmail(),
                    emp.getDepartment(),
                    emp.getDesignation(),
                    emp.getStatus(),
                    attStatus,
                    false
            );
        }).collect(Collectors.toList());
    }

    @Override
    public TeamMemberRosterDto getTeamMemberById(Long memberEmployeeId) {
        Long tenantId = getTenantId();
        Employee tl = getCurrentEmployee(tenantId);
        List<Employee> directReports = getDirectReports(tl, tenantId);

        Employee member = directReports.stream()
                .filter(e -> e.getId().equals(memberEmployeeId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Access denied: Employee " + memberEmployeeId + " is not a member of your team"));

        LocalDate today = LocalDate.now();
        Optional<Attendance> attOpt = attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(member.getId(), today, tenantId);
        String attStatus = attOpt.map(Attendance::getStatus).orElse("ABSENT");

        return new TeamMemberRosterDto(
                member.getId(),
                member.getEmployeeId(),
                member.getFullName(),
                member.getEmail(),
                member.getDepartment(),
                member.getDesignation(),
                member.getStatus(),
                attStatus,
                false
        );
    }

    @Override
    public TeamLeaderAttendanceTodayResponse getAttendanceToday() {
        Long tenantId = getTenantId();
        Employee tl = getCurrentEmployee(tenantId);
        List<Employee> directReports = getDirectReports(tl, tenantId);

        int totalMembers = directReports.size();
        if (totalMembers == 0) {
            return new TeamLeaderAttendanceTodayResponse(0, 0, 0, 0, 0, 0);
        }

        LocalDate today = LocalDate.now();
        List<Long> memberIds = directReports.stream().map(Employee::getId).toList();
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdInAndDateAndOrganizationId(memberIds, today, tenantId);
        Map<Long, Attendance> attendanceMap = attendances.stream()
                .filter(a -> a.getEmployee() != null)
                .collect(Collectors.toMap(a -> a.getEmployee().getId(), a -> a, (a1, a2) -> a1));

        int present = 0;
        int remote = 0;
        int onLeave = 0;
        int absent = 0;

        for (Employee emp : directReports) {
            Attendance att = attendanceMap.get(emp.getId());
            if (att == null) {
                absent++;
            } else {
                boolean isRemote = "REMOTE".equalsIgnoreCase(att.getLocation())
                        || "REMOTE".equalsIgnoreCase(att.getAttendanceType())
                        || (att.getNotes() != null && att.getNotes().toUpperCase().contains("REMOTE"));

                String s = att.getStatus() != null ? att.getStatus().toUpperCase() : "ABSENT";
                if ("LEAVE".equals(s) || "ON_LEAVE".equals(s)) {
                    onLeave++;
                } else if ("ABSENT".equals(s)) {
                    absent++;
                } else if (isRemote) {
                    remote++;
                } else if ("PRESENT".equals(s) || "COMPLETED".equals(s) || "WORKING".equals(s)) {
                    present++;
                } else {
                    present++;
                }
            }
        }

        int percentage = totalMembers > 0 ? (int) Math.round((present * 100.0) / totalMembers) : 0;
        return new TeamLeaderAttendanceTodayResponse(totalMembers, present, remote, onLeave, absent, percentage);
    }

    @Override
    public TeamLeaderPerformanceResponse getPerformance() {
        Long tenantId = getTenantId();
        Employee tl = getCurrentEmployee(tenantId);
        List<Employee> directReports = getDirectReports(tl, tenantId);

        double totalRating = 0.0;
        int ratedCount = 0;
        List<TeamLeaderPerformanceResponse.MemberPerformanceDto> memberRatings = new ArrayList<>();

        for (Employee emp : directReports) {
            List<Appraisal> appraisals = appraisalRepository.findByOrganizationIdAndEmployeeId(tenantId, emp.getId());
            if (appraisals.isEmpty()) {
                appraisals = appraisalRepository.findByEmployeeId(emp.getId());
            }
            Double rating = appraisals.stream()
                    .map(Appraisal::getFinalRating)
                    .filter(Objects::nonNull)
                    .reduce((first, second) -> second)
                    .orElse(null);
            if (rating != null) {
                totalRating += rating;
                ratedCount++;
            }
            memberRatings.add(new TeamLeaderPerformanceResponse.MemberPerformanceDto(emp.getId(), emp.getFullName(), rating));
        }

        double avgRating = ratedCount > 0 ? Math.round((totalRating / ratedCount) * 10.0) / 10.0 : 0.0;
        return new TeamLeaderPerformanceResponse(avgRating, 5.0, avgRating, memberRatings);
    }

    @Override
    @Transactional
    public void recommendLeave(Long leaveId, TeamLeaveRecommendationRequest request) {
        if (request == null || request.getDecision() == null || request.getDecision().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision is required");
        }
        String decision = request.getDecision().trim().toUpperCase();
        if (!"RECOMMEND".equals(decision) && !"NOT_RECOMMEND".equals(decision)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid decision: must be RECOMMEND or NOT_RECOMMEND");
        }

        Long tenantId = getTenantId();
        Employee tl = getCurrentEmployee(tenantId);
        List<Employee> directReports = getDirectReports(tl, tenantId);
        Set<Long> allowedMemberIds = directReports.stream().map(Employee::getId).collect(Collectors.toSet());

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found: " + leaveId));

        if (leave.getOrganization() != null && !leave.getOrganization().getId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found: " + leaveId);
        }

        if (leave.getEmployee() == null || !allowedMemberIds.contains(leave.getEmployee().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied: Leave " + leaveId + " does not belong to a member of your team");
        }

        // Check if already processed
        String currentStatus = leave.getStatus();
        if (!"PENDING_TL_RECOMMENDATION".equalsIgnoreCase(currentStatus) && !"PENDING".equalsIgnoreCase(currentStatus)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Leave request " + leaveId + " has already been processed (status: " + currentStatus + ")");
        }

        if ("RECOMMEND".equals(decision)) {
            leave.setStatus("TL_RECOMMENDED");
        } else {
            leave.setStatus("TL_NOT_RECOMMENDED");
        }
        if (request.getComment() != null) {
            leave.setManagerComment(request.getComment());
        }
        leave.setUpdatedAt(LocalDateTime.now());
        leaveRepository.save(leave);
    }
}
