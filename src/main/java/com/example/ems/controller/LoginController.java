package com.example.ems.controller;

import com.example.ems.dto.LoginRequest;
import com.example.ems.entity.User;
import com.example.ems.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        User user = userService.login(
                request.getGmail(),
                request.getPassword());

        if (user == null) {
            return "Invalid Gmail or Password";
        }

        return "Login Success - Role : " + user.getRole();
    }
}