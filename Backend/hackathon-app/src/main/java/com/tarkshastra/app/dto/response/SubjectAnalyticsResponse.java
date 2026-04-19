package com.tarkshastra.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectAnalyticsResponse {

    private String subjectName;
    private String subjectCode;
    private String className;
    private Double classAvgAttendance;
    private Double classAvgMarks;
    private Double classAvgAssignment;
    private Double classAvgLms;
    private int totalStudents;
    private int atRiskCount;
    private List<StudentProfileResponse> atRiskStudents;
}
