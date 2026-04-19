package com.tarkshastra.app.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualStudentRequest {

    @NotBlank
    private String rollNumber;

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private UUID departmentId;

    @NotNull
    private UUID classId;

    @NotNull
    private Integer semester;

    private UUID mentorId;
}
