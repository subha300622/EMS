package com.example.ems.service;

import com.example.ems.entity.User;
import com.example.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User login(String gmail, String password) {
        return userRepository.findByGmailAndPassword(gmail, password);
    }
}