package com.example.ems.controller;

import com.example.ems.dto.LoginRequest;
import com.example.ems.dto.RegisterRequest;
import com.example.ems.entity.User;
import com.example.ems.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class LoginController {

    @Autowired
    private UserService userService;

    /** POST /api/login */
    @PostMapping("/login")
    public String login(@RequestBody @Valid LoginRequest request) {
        User user = userService.login(request.getWorkEmail(), request.getPassword());
        if (user == null) {
            return "Invalid work email or password";
        }
        return "Login Success - Role: " + user.getRequestedRole();
    }

    /** POST /api/register */
    @PostMapping("/register")
    public String register(@RequestBody @Valid RegisterRequest request) {
        return userService.register(request);
    }
}