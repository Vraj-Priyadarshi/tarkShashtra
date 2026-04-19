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
public class StudentAcademicDetailResponse {

    private UUID studentId;
    private String fullName;
    private String rollNumber;
    private Double overallAttendance;
    private Double overallMarks;
    private Double overallAssignment;
    private Double overallLms;
    private Double overallRiskScore;
    private String overallRiskLabel;
    private List<SubjectAcademicDataResponse> subjects;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubjectAcademicDataResponse {
        private UUID subjectId;
        private String subjectName;
        private String subjectCode;
        private Double attendancePercentage;
        private Double iaMarksNormalized;
        private Double assignmentCompletionPercentage;
        private Double lmsScore;
    }
}
