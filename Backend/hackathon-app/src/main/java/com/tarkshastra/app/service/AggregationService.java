package com.tarkshastra.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Aggregates raw academic data into normalized feature scores (0-100) for risk computation.
 */
@Service
@RequiredArgsConstructor
public class AggregationService {

    private final AttendanceService attendanceService;
    private final IAMarksService iaMarksService;
    private final AssignmentService assignmentService;
    private final LMSScoreService lmsScoreService;

    public Double getAttendanceScore(UUID studentId, UUID subjectId) {
        return attendanceService.getAttendancePercentage(studentId, subjectId);
    }

    public Double getMarksScore(UUID studentId, UUID subjectId) {
        return iaMarksService.getAverageNormalizedScore(studentId, subjectId);
    }

    public Double getAssignmentScore(UUID studentId, UUID subjectId) {
        return assignmentService.getAssignmentCompletionPercentage(studentId, subjectId);
    }

    public Double getLmsScore(UUID studentId, UUID subjectId) {
        return lmsScoreService.getLMSScore(studentId, subjectId);
    }
}
