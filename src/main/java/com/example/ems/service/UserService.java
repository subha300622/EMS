package com.example.ems.service;

import com.example.ems.dto.RegisterRequest;
import com.example.ems.entity.User;
import com.example.ems.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    // ──────────────────────────────────────────
    //  LOGIN — compares BCrypt-hashed password
    // ──────────────────────────────────────────

    public User login(String workEmail, String password) {
        Optional<User> optUser = userRepository.findByWorkEmail(workEmail);
        if (optUser.isEmpty()) return null;

        User user = optUser.get();
        // BCrypt comparison
        if (!passwordEncoder.matches(password, user.getPassword())) return null;

        return user;
    }

    // ──────────────────────────────────────────
    //  REGISTER — BCrypt-hashes password
    // ──────────────────────────────────────────

    public String register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return "Passwords do not match";
        }

        if (userRepository.existsByWorkEmail(request.getWorkEmail())) {
            return "This work email is already registered. Each email can have only one account.";
        }

        if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                && userRepository.existsByEmployeeId(request.getEmployeeId())) {
            return "Employee ID is already in use";
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setWorkEmail(request.getWorkEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setEmployeeId(request.getEmployeeId());
        user.setDepartment(request.getDepartment());
        user.setRequestedRole(request.getRequestedRole());
        user.setLocation(request.getLocation());
        // BCrypt hash the password before storing
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String userId = "EMP" + String.format("%03d", user.getId());
        user.setUserId(userId);
        userRepository.save(user);

        log.info("New user registered: {} ({})", user.getWorkEmail(), userId);
        return "Registration Successful! Your User ID: " + userId + " | Role: " + user.getRequestedRole();
    }
}