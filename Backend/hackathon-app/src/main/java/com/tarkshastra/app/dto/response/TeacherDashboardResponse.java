package com.tarkshastra.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherDashboardResponse {

    private List<SubjectSummary> subjects;
    private int menteesAtRisk;
    private int pendingDataEntryCount;
    private ExamAlertInfo upcomingExamAlert;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubjectSummary {
        private UUID subjectId;
        private String subjectName;
        private String subjectCode;
        private String className;
        private UUID classId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExamAlertInfo {
        private String subjectName;
        private LocalDate examDate;
        private long daysUntilExam;
        private int highRiskMenteeCount;
    }
}
