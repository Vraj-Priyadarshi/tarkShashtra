package com.tarkshastra.app.dto.response;

import com.tarkshastra.app.enums.RiskLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileResponse {

    private UUID id;
    private UUID userId;
    private String fullName;
    private String email;
    private String rollNumber;
    private String departmentName;
    private String className;
    private Integer semester;
    private String mentorName;
    private String mentorEmail;
    private Double riskScore;
    private RiskLabel riskLabel;
    private Double attendancePercentage;
    private boolean active;
}
