package com.tarkshastra.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstituteDashboardResponse {

    private long totalStudents;
    private long totalTeachers;
    private long highRiskCount;
    private long mediumRiskCount;
    private long lowRiskCount;
    private Double averageRiskScore;
    private long totalInterventions;
    private List<DepartmentRiskSummary> departmentRiskSummaries;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DepartmentRiskSummary {
        private String departmentName;
        private UUID departmentId;
        private long highRiskCount;
        private long mediumRiskCount;
        private long lowRiskCount;
    }
}
