package com.example.ems.auth.service;

import com.example.ems.auth.dto.BootstrapResponse;
import com.example.ems.auth.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BootstrapService {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Cacheable(value = "userBootstrap", key = "#user.userId")
    public BootstrapResponse getBootstrapData(User user) {
        return new BootstrapResponse(
            userService.getUserProfile(user),
            new BootstrapResponse.AuthDataResponse(roleService.getPermissionsForUserId(user.getUserId())),
            userService.getUserContext(user)
        );
    }
}
