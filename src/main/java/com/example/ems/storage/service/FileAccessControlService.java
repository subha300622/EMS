package com.example.ems.storage.service;

import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.storage.entity.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FileAccessControlService {

    private static final Logger log = LoggerFactory.getLogger(FileAccessControlService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    public boolean canAccessFile(User user, FileMetadata file) {
        if (user == null || file == null) {
            log.warn("Access denied: User or File is null");
            return false;
        }

        String role = user.getRole() != null ? user.getRole().getName() : "EMPLOYEE";
        String requesterId = user.getUserId();
        String fileOwnerId = file.getUploadedByUserId();

        log.info("Evaluating access for requester={} (role={}, department={}) to file owned by owner={}", 
                requesterId, role, user.getDepartment(), fileOwnerId);

        // 1. SUPER_ADMIN and ADMIN have full access to all files
        if ("SUPER_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
            log.info("Access granted: Requester is admin/superadmin");
            return true;
        }

        // 2. EMPLOYEE can only view/download their own files
        if ("EMPLOYEE".equalsIgnoreCase(role)) {
            boolean isOwner = requesterId != null && requesterId.equals(fileOwnerId);
            log.info("Access result for EMPLOYEE self-check: {}", isOwner);
            return isOwner;
        }

        // 3. HR can access employees in their own department
        if ("HR".equalsIgnoreCase(role)) {
            String requesterDept = user.getDepartment();
            String ownerDept = file.getDepartmentId();
            
            // If the department stored in file metadata matches the requester department
            if (requesterDept != null && requesterDept.equalsIgnoreCase(ownerDept)) {
                log.info("Access granted: HR in same department as file metadata department");
                return true;
            }

            // Fallback: Query owner's current department in DB to see if it matches
            Optional<Employee> ownerEmpOpt = employeeRepository.findByEmployeeId(fileOwnerId);
            if (ownerEmpOpt.isPresent()) {
                String currentOwnerDept = ownerEmpOpt.get().getDepartment();
                if (requesterDept != null && requesterDept.equalsIgnoreCase(currentOwnerDept)) {
                    log.info("Access granted: HR in same department as owner's current database department");
                    return true;
                }
            }
            log.warn("Access denied: HR is not in the same department as the owner");
            return false;
        }

        // 4. MANAGER can access employees in same department OR employees they directly manage
        if ("MANAGER".equalsIgnoreCase(role)) {
            // Managers can access their own files
            if (requesterId != null && requesterId.equals(fileOwnerId)) {
                log.info("Access granted: MANAGER accessing own file");
                return true;
            }

            String requesterDept = user.getDepartment();
            String ownerDept = file.getDepartmentId();

            // Match by department in metadata
            if (requesterDept != null && requesterDept.equalsIgnoreCase(ownerDept)) {
                log.info("Access granted: MANAGER in same department as file department");
                return true;
            }

            Optional<Employee> ownerEmpOpt = employeeRepository.findByEmployeeId(fileOwnerId);
            if (ownerEmpOpt.isPresent()) {
                Employee ownerEmp = ownerEmpOpt.get();
                // Match by owner's current department in DB
                if (requesterDept != null && requesterDept.equalsIgnoreCase(ownerEmp.getDepartment())) {
                    log.info("Access granted: MANAGER in same department as owner's current database department");
                    return true;
                }
                // Match by direct manager relationship
                if (ownerEmp.getManager() != null && requesterId != null && requesterId.equals(ownerEmp.getManager().getEmployeeId())) {
                    log.info("Access granted: MANAGER directly manages the file owner");
                    return true;
                }
            }
            log.warn("Access denied: MANAGER does not share department or manage owner");
            return false;
        }

        log.warn("Access denied: Unknown role '{}'", role);
        return false;
    }
}
