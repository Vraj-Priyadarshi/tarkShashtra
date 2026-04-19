package com.tarkshastra.app.dto.response;

import com.tarkshastra.app.enums.RiskLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDashboardResponse {

    private String fullName;
    private String rollNumber;
    private int semester;
    private String branch;
    private Double riskScore;
    private RiskLabel riskLabel;
    private List<RiskFactor> topContributingFactors;
    private List<String> improvementTips;
    private String mentorName;
    private String mentorEmail;
    private ConsistencyStreakResponse consistencyStreak;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiskFactor {
        private String factor;
        private Double value;
        private Double classAverage;
        private Double contributionPercentage;
    }
}
