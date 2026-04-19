package com.tarkshastra.app.controller;

import com.tarkshastra.app.dto.request.WhatIfRequest;
import com.tarkshastra.app.dto.response.*;
import com.tarkshastra.app.entity.User;
import com.tarkshastra.app.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final RiskScoreService riskScoreService;
    private final StudentFlagService studentFlagService;
    private final StudentDashboardService studentDashboardService;
    private final ConsistencyStreakService consistencyStreakService;
    private final StudentSuggestionService studentSuggestionService;

    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardResponse> getDashboard(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(studentDashboardService.getDashboard(student.getId()));
    }

    @GetMapping("/my-risk")
    public ResponseEntity<RiskScoreResponse> getMyRisk(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(riskScoreService.getRiskScoreResponse(student.getId()));
    }

    @GetMapping("/my-risk-trend")
    public ResponseEntity<RiskTrendResponse> getMyRiskTrend(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(riskScoreService.getRiskTrend(student.getId()));
    }

    @GetMapping("/my-flags")
    public ResponseEntity<List<StudentFlagResponse>> getMyFlags(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(studentFlagService.getUnresolvedFlagsForStudent(student.getId()));
    }

    @GetMapping("/academic-data")
    public ResponseEntity<StudentAcademicDetailResponse> getAcademicData(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(studentDashboardService.getAcademicData(student.getId()));
    }

    @PostMapping("/what-if")
    public ResponseEntity<WhatIfResponse> whatIf(
            @AuthenticationPrincipal User student,
            @Valid @RequestBody WhatIfRequest request) {
        request.setStudentId(student.getId());
        return ResponseEntity.ok(riskScoreService.computeWhatIf(request));
    }

    @GetMapping("/interventions")
    public ResponseEntity<List<InterventionResponse>> getInterventionHistory(
            @AuthenticationPrincipal User student) {
        return ResponseEntity.ok(studentDashboardService.getInterventionHistory(student.getId()));
    }

    @GetMapping("/consistency-streak")
    public ResponseEntity<ConsistencyStreakResponse> getConsistencyStreak(
            @AuthenticationPrincipal User student) {
        return ResponseEntity.ok(consistencyStreakService.getStreak(student.getId()));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<SuggestionsResponse> getSuggestions(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(studentSuggestionService.getSuggestions(student.getId()));
    }

    @GetMapping("/roadmap")
    public ResponseEntity<RoadmapResponse> getRoadmap(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(studentSuggestionService.getRoadmap(student.getId()));
    }

    @PostMapping("/roadmap/regenerate")
    public ResponseEntity<RoadmapResponse> regenerateRoadmap(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(studentSuggestionService.regenerateRoadmap(student.getId()));
    }
}
