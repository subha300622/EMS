package com.example.ems.training;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateToken {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("BCRYPT_HASH:" + encoder.encode("Password123!"));
    }
}
