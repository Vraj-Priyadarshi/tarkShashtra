package com.tarkshastra.app.dto.response;

import com.tarkshastra.app.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherProfileResponse {

    private UUID id;
    private UUID userId;
    private String fullName;
    private String email;
    private String employeeId;
    private String departmentName;
    private Set<Role> roles;
    private boolean active;
}
