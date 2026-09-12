package com.example.ems.auth.event;

import com.example.ems.auth.entity.User;
import org.springframework.context.ApplicationEvent;

public class UserRegisteredEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final transient User user;
    private final String token;

    public UserRegisteredEvent(Object source, User user, String token) {
        super(source);
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }
}
