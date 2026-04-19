package com.tarkshastra.app.dto.response;

import com.tarkshastra.app.enums.RiskLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskScoreResponse {

    private UUID studentId;
    private String fullName;
    private Double riskScore;
    private RiskLabel riskLabel;
    private Double attendanceScore;
    private Double marksScore;
    private Double assignmentScore;
    private Double lmsScore;
    private LocalDateTime computedAt;
    private List<SubjectRiskResponse> subjectRisks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubjectRiskResponse {
        private UUID subjectId;
        private String subjectName;
        private Double riskScore;
        private RiskLabel riskLabel;
        private Double attendance;
        private Double marks;
        private Double assignment;
        private Double lms;
    }
}
