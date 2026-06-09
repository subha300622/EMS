package com.example.ems.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Auto-generated unique user ID in format EMP001, EMP002, ...
     * Generated after first save using the database-assigned primary key.
     */
    @Column(unique = true)
    private String userId;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String workEmail;

    private String mobileNumber;

    // Optional — provided by user
    private String employeeId;

    private String department;

    private String requestedRole;

    // Optional
    private String location;

    private String password;
}