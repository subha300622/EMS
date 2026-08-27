package com.example.ems.employee.service;

import com.example.ems.auth.entity.User;
import com.example.ems.employee.dto.TeamDtos;
import com.example.ems.employee.entity.*;
import com.example.ems.employee.repository.*;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.*;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamAuditLogRepository teamAuditLogRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Long resolveOrganizationId(User user) {
        if (user.getOrganizationId() != null) {
            return user.getOrganizationId();
        }
        if (user.getOrganization() != null) {
            return user.getOrganization().getId();
        }
        throw new IllegalArgumentException("User does not belong to any valid organization");
    }

    private Organization resolveOrganization(User user) {
        Long orgId = resolveOrganizationId(user);
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));
    }

    // 1. Create Team
    @Transactional
    public TeamDtos.TeamResponseDto createTeam(TeamDtos.TeamCreateRequest request, User currentUser) {
        if (request.getTeamName() == null || request.getTeamName().isBlank()) {
            throw new IllegalArgumentException("teamName is required");
        }
        if (request.getTeamCode() == null || request.getTeamCode().isBlank()) {
            throw new IllegalArgumentException("teamCode is required");
        }

        Long orgId = resolveOrganizationId(currentUser);
        Organization org = resolveOrganization(currentUser);

        String trimmedName = request.getTeamName().trim();
        String trimmedCode = request.getTeamCode().trim().toUpperCase();

        if (teamRepository.existsByTeamNameAndOrganizationIdAndDeletedFalse(trimmedName, orgId)) {
            throw new IllegalArgumentException("Team name already exists within the organization");
        }
        if (teamRepository.existsByTeamCodeAndOrganizationIdAndDeletedFalse(trimmedCode, orgId)) {
            throw new IllegalArgumentException("Team code already exists within the organization");
        }

        Department dept = null;
        if (request.getDepartmentId() != null && request.getDepartmentId() > 0) {
            dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + request.getDepartmentId()));
            if (!"ACTIVE".equalsIgnoreCase(dept.getStatus())) {
                throw new IllegalArgumentException("Department is not active");
            }
        }

        Employee teamLead = null;
        if (request.getTeamLeadEmployeeId() != null && request.getTeamLeadEmployeeId() > 0) {
            teamLead = employeeRepository.findById(request.getTeamLeadEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getTeamLeadEmployeeId()));
            if (!"ACTIVE".equalsIgnoreCase(teamLead.getStatus())) {
                throw new IllegalArgumentException("Team lead employee is not active");
            }
        }

        Team team = new Team();
        team.setTeamName(trimmedName);
        team.setTeamCode(trimmedCode);
        team.setDescription(request.getDescription());
        team.setDepartment(dept);
        team.setTeamLead(teamLead);
        team.setOrganization(org);
        team.setStatus("ACTIVE");
        team.setCreatedBy(currentUser.getWorkEmail());

        Team savedTeam = teamRepository.save(team);

        // If team lead was provided, automatically add lead as active member
        if (teamLead != null) {
            TeamMember leadMember = new TeamMember(savedTeam, teamLead, LocalDate.now(), true);
            teamMemberRepository.save(leadMember);
        }

        // Record Audit Log
        recordAudit(savedTeam.getId(), "CREATE", null, savedTeam.getTeamName(), currentUser, "Created team " + savedTeam.getTeamName());

        return mapToResponseDto(savedTeam);
    }

    // 2. Get Team
    @Transactional(readOnly = true)
    public TeamDtos.TeamResponseDto getTeam(Long teamId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));
        return mapToResponseDto(team);
    }

    // 3. List Teams
    @Transactional(readOnly = true)
    public Page<TeamDtos.TeamResponseDto> listTeams(String search, String status, Long departmentId, int page, int size, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Specification<Team> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organization").get("id"), orgId));
            predicates.add(cb.equal(root.get("deleted"), false));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("teamName")), pattern);
                Predicate codeLike = cb.like(cb.lower(root.get("teamCode")), pattern);
                predicates.add(cb.or(nameLike, codeLike));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
            }

            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Team> teamPage = teamRepository.findAll(spec, pageable);
        return teamPage.map(this::mapToResponseDto);
    }

    // 4. Update Team
    @Transactional
    public TeamDtos.TeamResponseDto updateTeam(Long teamId, TeamDtos.TeamUpdateRequest request, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));

        if (request.getTeamName() == null || request.getTeamName().isBlank()) {
            throw new IllegalArgumentException("teamName is required");
        }
        if (request.getTeamCode() == null || request.getTeamCode().isBlank()) {
            throw new IllegalArgumentException("teamCode is required");
        }

        String trimmedName = request.getTeamName().trim();
        String trimmedCode = request.getTeamCode().trim().toUpperCase();

        if (teamRepository.existsByTeamNameAndOrganizationIdAndIdNotAndDeletedFalse(trimmedName, orgId, teamId)) {
            throw new IllegalArgumentException("Team name already exists within the organization");
        }
        if (teamRepository.existsByTeamCodeAndOrganizationIdAndIdNotAndDeletedFalse(trimmedCode, orgId, teamId)) {
            throw new IllegalArgumentException("Team code already exists within the organization");
        }

        String oldVal = team.getTeamName() + " (" + team.getTeamCode() + ")";

        team.setTeamName(trimmedName);
        team.setTeamCode(trimmedCode);
        team.setDescription(request.getDescription());

        // Department update (supports explicit null or 0 to disconnect)
        if (request.getDepartmentId() != null && request.getDepartmentId() > 0) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + request.getDepartmentId()));
            if (!"ACTIVE".equalsIgnoreCase(dept.getStatus())) {
                throw new IllegalArgumentException("Department is not active");
            }
            // Check existing active members compatibility before attaching department
            validateActiveMembersDepartmentCompatibility(teamId, dept);
            team.setDepartment(dept);
        } else {
            team.setDepartment(null);
        }

        // Team Lead update (supports null or 0 to clear lead)
        if (request.getTeamLeadEmployeeId() != null && request.getTeamLeadEmployeeId() > 0) {
            Employee lead = employeeRepository.findById(request.getTeamLeadEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getTeamLeadEmployeeId()));
            if (!"ACTIVE".equalsIgnoreCase(lead.getStatus())) {
                throw new IllegalArgumentException("Employee is not active");
            }
            updateTeamLeadInternal(team, lead, currentUser);
        } else {
            updateTeamLeadInternal(team, null, currentUser);
        }

        Team updated = teamRepository.save(team);
        recordAudit(teamId, "UPDATE", oldVal, updated.getTeamName() + " (" + updated.getTeamCode() + ")", currentUser, "Updated team details");

        return mapToResponseDto(updated);
    }

    // 5. Change Team Status
    @Transactional
    public TeamDtos.TeamResponseDto changeTeamStatus(Long teamId, String status, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));

        if (status == null || (!"ACTIVE".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status))) {
            throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE");
        }

        String oldStatus = team.getStatus();
        String newStatus = status.trim().toUpperCase();
        team.setStatus(newStatus);
        Team updated = teamRepository.save(team);

        recordAudit(teamId, "STATUS_CHANGE", oldStatus, newStatus, currentUser, "Changed team status to " + newStatus);
        return mapToResponseDto(updated);
    }

    // 6. Delete Team (Soft delete with active member guard)
    @Transactional
    public void deleteTeam(Long teamId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));

        long activeMembers = teamMemberRepository.countByTeamIdAndStatus(teamId, "ACTIVE");
        if (activeMembers > 0) {
            throw new ActiveMembersExistException("TEAM_HAS_ACTIVE_MEMBERS", "Team cannot be deleted while active members are assigned.");
        }

        team.setDeleted(true);
        team.setStatus("INACTIVE");
        teamRepository.save(team);

        recordAudit(teamId, "DELETE", "ACTIVE", "DELETED", currentUser, "Soft deleted team");
    }

    // 7. Add Employee to Team
    @Transactional
    public TeamDtos.MemberDto addMember(Long teamId, Long employeeId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));

        if (!"ACTIVE".equalsIgnoreCase(team.getStatus())) {
            throw new IllegalArgumentException("Cannot add member to an INACTIVE team");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        if (!"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new IllegalArgumentException("Employee is not active");
        }

        // Validate organization boundary
        if (employee.getOrganization() != null && !employee.getOrganization().getId().equals(orgId)) {
            throw new IllegalArgumentException("Employee belongs to a different organization");
        }

        boolean alreadyMember = teamMemberRepository.existsByTeamIdAndEmployeeIdAndStatus(teamId, employeeId, "ACTIVE");
        if (alreadyMember) {
            throw new IllegalArgumentException("Employee is already an active member of this team");
        }

        // If team has department, employee department must match
        if (team.getDepartment() != null) {
            validateEmployeeDepartmentMatch(employee, team.getDepartment());
        }

        TeamMember member = new TeamMember(team, employee, LocalDate.now(), false);
        TeamMember saved = teamMemberRepository.save(member);

        recordAudit(teamId, "ADD_MEMBER", null, employee.getFullName(), currentUser, "Added employee " + employee.getFullName() + " to team");

        return new TeamDtos.MemberDto(employee.getId(), employee.getFullName(), employee.getDesignation(), false, saved.getJoinedAt());
    }

    // 8. Remove Employee from Team
    @Transactional
    public void removeMember(Long teamId, Long employeeId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));

        TeamMember member = teamMemberRepository.findByTeamIdAndEmployeeIdAndStatus(teamId, employeeId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("Active team membership not found for employee ID: " + employeeId));

        // Block removal if employee is current Team Lead
        if (team.getTeamLead() != null && team.getTeamLead().getId().equals(employeeId)) {
            throw new IllegalArgumentException("Cannot remove Team Lead. Reassign Team Lead first.");
        }

        member.setLeftAt(LocalDate.now());
        member.setStatus("INACTIVE");
        member.setIsTeamLead(false);
        teamMemberRepository.save(member);

        recordAudit(teamId, "REMOVE_MEMBER", member.getEmployee().getFullName(), null, currentUser, "Removed employee " + member.getEmployee().getFullName() + " from team");
    }

    // 9. Get Team Members
    @Transactional(readOnly = true)
    public TeamDtos.TeamMemberListResponseDto getTeamMembers(Long teamId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));

        List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatus(teamId, "ACTIVE");
        List<TeamDtos.MemberDto> memberDtos = members.stream().map(m -> new TeamDtos.MemberDto(
                m.getEmployee().getId(),
                m.getEmployee().getFullName(),
                m.getEmployee().getDesignation(),
                m.getIsTeamLead(),
                m.getJoinedAt()
        )).toList();

        return new TeamDtos.TeamMemberListResponseDto(team.getId(), team.getTeamName(), memberDtos);
    }

    // 10. Change Team Lead
    @Transactional
    public TeamDtos.TeamResponseDto changeTeamLead(Long teamId, Long employeeId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));

        if (!"ACTIVE".equalsIgnoreCase(team.getStatus())) {
            throw new IllegalArgumentException("Cannot change team lead on an INACTIVE team");
        }

        Employee newLead = null;
        if (employeeId != null && employeeId > 0) {
            newLead = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

            if (!"ACTIVE".equalsIgnoreCase(newLead.getStatus())) {
                throw new IllegalArgumentException("Employee is not active");
            }

            // Employee MUST already be an active member of the team
            boolean isMember = teamMemberRepository.existsByTeamIdAndEmployeeIdAndStatus(teamId, employeeId, "ACTIVE");
            if (!isMember) {
                throw new IllegalArgumentException("Employee must already be an active member of the Team before being promoted to Team Lead");
            }
        }

        updateTeamLeadInternal(team, newLead, currentUser);
        Team saved = teamRepository.save(team);

        String newLeadName = newLead != null ? newLead.getFullName() : "None";
        recordAudit(teamId, "CHANGE_LEAD", null, newLeadName, currentUser, "Updated team lead to " + newLeadName);

        return mapToResponseDto(saved);
    }

    // 11. Bulk Add Members (Partial Success)
    @Transactional
    public TeamDtos.TeamMemberBulkAddResponse bulkAddMembers(Long teamId, List<Long> employeeIds, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));

        if (!"ACTIVE".equalsIgnoreCase(team.getStatus())) {
            throw new IllegalArgumentException("Cannot add members to an INACTIVE team");
        }

        List<TeamDtos.MemberAddResult> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        if (employeeIds != null) {
            for (Long empId : employeeIds) {
                Optional<Employee> empOpt = employeeRepository.findById(empId);
                if (empOpt.isEmpty()) {
                    results.add(new TeamDtos.MemberAddResult(empId, "FAILED", "EMPLOYEE_NOT_FOUND"));
                    failedCount++;
                    continue;
                }

                Employee emp = empOpt.get();
                if (!"ACTIVE".equalsIgnoreCase(emp.getStatus())) {
                    results.add(new TeamDtos.MemberAddResult(empId, "FAILED", "EMPLOYEE_INACTIVE"));
                    failedCount++;
                    continue;
                }

                if (emp.getOrganization() != null && !emp.getOrganization().getId().equals(orgId)) {
                    results.add(new TeamDtos.MemberAddResult(empId, "FAILED", "EMPLOYEE_CROSS_TENANT"));
                    failedCount++;
                    continue;
                }

                boolean alreadyMember = teamMemberRepository.existsByTeamIdAndEmployeeIdAndStatus(teamId, empId, "ACTIVE");
                if (alreadyMember) {
                    results.add(new TeamDtos.MemberAddResult(empId, "FAILED", "EMPLOYEE_ALREADY_MEMBER"));
                    failedCount++;
                    continue;
                }

                if (team.getDepartment() != null) {
                    try {
                        validateEmployeeDepartmentMatch(emp, team.getDepartment());
                    } catch (IllegalArgumentException e) {
                        results.add(new TeamDtos.MemberAddResult(empId, "FAILED", "EMPLOYEE_BELONGS_TO_DIFFERENT_DEPARTMENT"));
                        failedCount++;
                        continue;
                    }
                }

                TeamMember member = new TeamMember(team, emp, LocalDate.now(), false);
                teamMemberRepository.save(member);
                results.add(new TeamDtos.MemberAddResult(empId, "ADDED", null));
                successCount++;

                recordAudit(teamId, "ADD_MEMBER", null, emp.getFullName(), currentUser, "Bulk added employee " + emp.getFullName() + " to team");
            }
        }

        return new TeamDtos.TeamMemberBulkAddResponse(teamId, successCount, failedCount, results);
    }

    // 12. Get Teams by Department
    @Transactional(readOnly = true)
    public List<TeamDtos.TeamResponseDto> getTeamsByDepartment(Long departmentId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

        List<Team> teams = teamRepository.findByDepartmentIdAndOrganizationIdAndDeletedFalse(departmentId, orgId);
        return teams.stream().map(this::mapToResponseDto).toList();
    }

    // 13. Assign Department to Team
    @Transactional
    public TeamDtos.TeamResponseDto assignDepartment(Long teamId, Long departmentId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        Team team = teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));

        String oldDeptName = team.getDepartment() != null ? team.getDepartment().getName() : "None";

        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

            if (!"ACTIVE".equalsIgnoreCase(dept.getStatus())) {
                throw new IllegalArgumentException("Department is not active");
            }

            // Check existing active team members for department compatibility
            validateActiveMembersDepartmentCompatibility(teamId, dept);
            team.setDepartment(dept);
        } else {
            team.setDepartment(null);
        }

        Team saved = teamRepository.save(team);
        String newDeptName = saved.getDepartment() != null ? saved.getDepartment().getName() : "None";
        recordAudit(teamId, "ASSIGN_DEPARTMENT", oldDeptName, newDeptName, currentUser, "Updated team department to " + newDeptName);

        return mapToResponseDto(saved);
    }

    // 14. Get Team Audit Logs
    @Transactional(readOnly = true)
    public List<TeamAuditLog> getAuditLogs(Long teamId, User currentUser) {
        Long orgId = resolveOrganizationId(currentUser);
        teamRepository.findByIdAndOrganizationIdAndDeletedFalse(teamId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));

        return teamAuditLogRepository.findByTeamIdOrderByTimestampDesc(teamId);
    }

    // --- Private Helper Methods ---

    private void updateTeamLeadInternal(Team team, Employee newLead, User currentUser) {
        // Demote old lead in team_members
        if (team.getTeamLead() != null) {
            Optional<TeamMember> oldLeadMember = teamMemberRepository.findByTeamIdAndEmployeeIdAndStatus(team.getId(), team.getTeamLead().getId(), "ACTIVE");
            oldLeadMember.ifPresent(m -> {
                m.setIsTeamLead(false);
                teamMemberRepository.save(m);
            });
        }

        // Promote new lead in team_members
        if (newLead != null) {
            Optional<TeamMember> newLeadMember = teamMemberRepository.findByTeamIdAndEmployeeIdAndStatus(team.getId(), newLead.getId(), "ACTIVE");
            newLeadMember.ifPresent(m -> {
                m.setIsTeamLead(true);
                teamMemberRepository.save(m);
            });
        }

        team.setTeamLead(newLead);
    }

    private void validateEmployeeDepartmentMatch(Employee employee, Department teamDept) {
        if (employee.getDepartment() == null || !employee.getDepartment().equalsIgnoreCase(teamDept.getName())) {
            throw new IllegalArgumentException("Employee department (" + employee.getDepartment() + ") does not match Team department (" + teamDept.getName() + ")");
        }
    }

    private void validateActiveMembersDepartmentCompatibility(Long teamId, Department requiredDept) {
        List<TeamMember> activeMembers = teamMemberRepository.findByTeamIdAndStatus(teamId, "ACTIVE");
        List<Map<String, Object>> mismatchDetails = new ArrayList<>();

        for (TeamMember m : activeMembers) {
            Employee emp = m.getEmployee();
            if (emp.getDepartment() == null || !emp.getDepartment().equalsIgnoreCase(requiredDept.getName())) {
                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("employeeId", emp.getId());
                detail.put("employeeDepartment", emp.getDepartment());
                detail.put("requiredDepartment", requiredDept.getName());
                mismatchDetails.add(detail);
            }
        }

        if (!mismatchDetails.isEmpty()) {
            throw new DepartmentMismatchException(
                    "TEAM_MEMBER_DEPARTMENT_MISMATCH",
                    "Team cannot be assigned to this department because one or more active members belong to a different department.",
                    mismatchDetails
            );
        }
    }

    private void recordAudit(Long teamId, String action, String oldValue, String newValue, User currentUser, String details) {
        TeamAuditLog log = new TeamAuditLog(
                teamId,
                action,
                oldValue,
                newValue,
                currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getFullName() : "System",
                details
        );
        teamAuditLogRepository.save(log);
    }

    private TeamDtos.TeamResponseDto mapToResponseDto(Team team) {
        TeamDtos.TeamResponseDto dto = new TeamDtos.TeamResponseDto();
        dto.setTeamId(team.getId());
        dto.setTeamName(team.getTeamName());
        dto.setTeamCode(team.getTeamCode());
        dto.setDescription(team.getDescription());
        dto.setStatus(team.getStatus());

        if (team.getDepartment() != null) {
            dto.setDepartment(new TeamDtos.DepartmentSummary(team.getDepartment().getId(), team.getDepartment().getName()));
        } else {
            dto.setDepartment(null);
        }

        if (team.getTeamLead() != null) {
            dto.setTeamLead(new TeamDtos.TeamLeadSummary(team.getTeamLead().getId(), team.getTeamLead().getFullName()));
        } else {
            dto.setTeamLead(null);
        }

        long activeMemberCount = teamMemberRepository.countByTeamIdAndStatus(team.getId(), "ACTIVE");
        dto.setMemberCount(activeMemberCount);

        return dto;
    }

    // --- Custom Domain Exceptions ---

    public static class ActiveMembersExistException extends RuntimeException {
        private final String code;

        public ActiveMembersExistException(String code, String message) {
            super(message);
            this.code = code;
        }

        public String getCode() { return code; }
    }

    public static class DepartmentMismatchException extends RuntimeException {
        private final String code;
        private final List<Map<String, Object>> details;

        public DepartmentMismatchException(String code, String message, List<Map<String, Object>> details) {
            super(message);
            this.code = code;
            this.details = details;
        }

        public String getCode() { return code; }
        public List<Map<String, Object>> getDetails() { return details; }
    }
}
