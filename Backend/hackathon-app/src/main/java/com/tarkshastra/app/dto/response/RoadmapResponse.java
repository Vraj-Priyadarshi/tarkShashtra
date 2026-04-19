package com.tarkshastra.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapResponse {

    private String roadmapTitle;
    private String duration;
    private String overallGoal;
    private List<WeekPlan> weeks;
    private SuccessMetrics successMetrics;
    private LocalDateTime generatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeekPlan {
        private int weekNumber;
        private String theme;
        private List<String> focusSubjects;
        private List<String> dailyTasks;
        private WeeklyTargets weeklyTargets;
        private String milestone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeeklyTargets {
        private String attendance;
        private String assignmentsToComplete;
        private String lmsSessions;
        private String studyHours;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuccessMetrics {
        private String attendanceTarget;
        private String marksTarget;
        private String assignmentTarget;
        private String lmsTarget;
    }
}
