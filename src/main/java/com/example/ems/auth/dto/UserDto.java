package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class UserDto {
    @Schema(example = "string")
    private String id;
    @Schema(example = "string")
    private String name;
    @Schema(example = "john.doe@example.com")
    private String email;
    @Schema(example = "Software Engineer")
    private String role;

    public UserDto() {}

    public UserDto(String id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
