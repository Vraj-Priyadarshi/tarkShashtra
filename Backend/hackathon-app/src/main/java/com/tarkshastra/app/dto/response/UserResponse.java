package com.tarkshastra.app.dto.response;

import com.tarkshastra.app.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String email;
    private String fullName;
    private Set<Role> roles;
    private UUID instituteId;
    private String instituteName;
    private boolean mustChangePassword;
    private boolean isActive;
    private LocalDateTime createdAt;

    // Student-specific
    private String rollNumber;
    private String departmentName;
    private String className;
    private Integer semester;
    private String mentorName;

    // Teacher-specific
    private String employeeId;
}