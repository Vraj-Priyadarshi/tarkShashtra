package com.tarkshastra.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkshastra.app.dto.response.RoadmapResponse;
import com.tarkshastra.app.dto.response.SuggestionsResponse;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentSuggestionService {

    private final StudentProfileRepository studentProfileRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final SubjectClassMappingRepository subjectClassMappingRepository;
    private final AggregationService aggregationService;
    private final MLServiceClient mlServiceClient;
    private final StudentRoadmapRepository studentRoadmapRepository;
    private final ObjectMapper objectMapper;

    /**
     * Gets LLM-powered suggestions for a student.
     * Always calls ML service fresh (no DB caching for suggestions).
     */
    public SuggestionsResponse getSuggestions(UUID studentId) {
        Map<String, Object> payload = buildStudentPayload(studentId);
        Map<String, Object> raw = mlServiceClient.getSuggestions(payload);
        if (raw == null) {
            return buildFallbackSuggestions();
        }
        try {
            return mapSuggestionsResponse(raw);
        } catch (Exception e) {
            log.error("Failed to parse suggestions response: {}", e.getMessage());
            return buildFallbackSuggestions();
        }
    }

    /**
     * Gets LLM-powered roadmap for a student.
     * Checks DB first for a saved roadmap; if none, calls ML and persists.
     */
    public RoadmapResponse getRoadmap(UUID studentId) {
        // Check for existing saved roadmap
        Optional<StudentRoadmap> existing = studentRoadmapRepository.findTopByStudentIdOrderByCreatedAtDesc(studentId);
        if (existing.isPresent()) {
            try {
                RoadmapResponse resp = parseRoadmapJson(existing.get().getRoadmapJson());
                resp.setGeneratedAt(existing.get().getCreatedAt());
                return resp;
            } catch (Exception e) {
                log.warn("Failed to parse saved roadmap, regenerating: {}", e.getMessage());
            }
        }
        return generateAndSaveRoadmap(studentId);
    }

    /**
     * Forces regeneration of roadmap (called when student wants a fresh one).
     */
    public RoadmapResponse regenerateRoadmap(UUID studentId) {
        return generateAndSaveRoadmap(studentId);
    }

    private RoadmapResponse generateAndSaveRoadmap(UUID studentId) {
        Map<String, Object> payload = buildStudentPayload(studentId);
        Map<String, Object> raw = mlServiceClient.getRoadmap(payload);
        if (raw == null) {
            return buildFallbackRoadmap();
        }
        try {
            RoadmapResponse resp = mapRoadmapResponse(raw);

            // Persist to DB
            User studentRef = new User();
            studentRef.setId(studentId);

            String roadmapJson = objectMapper.writeValueAsString(raw);
            StudentRoadmap entity = StudentRoadmap.builder()
                    .student(studentRef)
                    .roadmapJson(roadmapJson)
                    .build();
            StudentRoadmap saved = studentRoadmapRepository.save(entity);
            resp.setGeneratedAt(saved.getCreatedAt());
            return resp;
        } catch (Exception e) {
            log.error("Failed to save roadmap: {}", e.getMessage());
            return buildFallbackRoadmap();
        }
    }

    private Map<String, Object> buildStudentPayload(UUID studentId) {
        StudentProfile sp = studentProfileRepository.findByUserId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        RiskScore overall = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(studentId)
                .orElse(null);

        // Build overall metrics
        Map<String, Object> overallMetrics = new LinkedHashMap<>();
        overallMetrics.put("attendance", overall != null && overall.getAttendanceScore() != null ? overall.getAttendanceScore() : 0.0);
        overallMetrics.put("marks", overall != null && overall.getMarksScore() != null ? overall.getMarksScore() : 0.0);
        overallMetrics.put("assignment", overall != null && overall.getAssignmentScore() != null ? overall.getAssignmentScore() : 0.0);
        overallMetrics.put("lms", overall != null && overall.getLmsScore() != null ? overall.getLmsScore() : 0.0);
        overallMetrics.put("risk_score", overall != null && overall.getRiskScore() != null ? overall.getRiskScore() : 50.0);
        overallMetrics.put("risk_label", overall != null && overall.getRiskLabel() != null ? overall.getRiskLabel().name() : "MEDIUM");

        // Build subject-wise metrics
        List<SubjectClassMapping> mappings = subjectClassMappingRepository.findByClassEntityId(
                sp.getClassEntity().getId());

        List<Map<String, Object>> subjectWise = mappings.stream().map(m -> {
            UUID subjectId = m.getSubject().getId();
            String subjectName = m.getSubject().getName();

            Double att = aggregationService.getAttendanceScore(studentId, subjectId);
            Double marks = aggregationService.getMarksScore(studentId, subjectId);
            Double assignment = aggregationService.getAssignmentScore(studentId, subjectId);
            Double lms = aggregationService.getLmsScore(studentId, subjectId);

            RiskScore subRisk = riskScoreRepository.findByStudentIdAndSubjectIdAndIsLatestTrue(studentId, subjectId)
                    .orElse(null);

            Map<String, Object> sub = new LinkedHashMap<>();
            sub.put("subject", subjectName);
            sub.put("attendance", att != null ? att : 0.0);
            sub.put("marks", marks != null ? marks : 0.0);
            sub.put("assignment", assignment != null ? assignment : 0.0);
            sub.put("lms", lms != null ? lms : 0.0);
            sub.put("risk_score", subRisk != null ? subRisk.getRiskScore() : 50.0);
            sub.put("risk_label", subRisk != null ? subRisk.getRiskLabel().name() : "MEDIUM");
            return sub;
        }).collect(Collectors.toList());

        // Build full payload
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("student_id", studentId.toString());
        payload.put("student_name", sp.getFullName());
        payload.put("semester", sp.getSemester());
        payload.put("branch", sp.getDepartment().getName());
        payload.put("overall", overallMetrics);
        payload.put("subject_wise", subjectWise);

        return payload;
    }

    @SuppressWarnings("unchecked")
    private SuggestionsResponse mapSuggestionsResponse(Map<String, Object> raw) {
        List<Map<String, Object>> rawSuggestions = (List<Map<String, Object>>) raw.getOrDefault("suggestions", List.of());
        List<SuggestionsResponse.SuggestionItem> items = rawSuggestions.stream()
                .map(s -> SuggestionsResponse.SuggestionItem.builder()
                        .area(str(s.get("area")))
                        .currentValue(str(s.get("current_value")))
                        .targetValue(str(s.get("target_value")))
                        .action(str(s.get("action")))
                        .impact(str(s.get("impact")))
                        .build())
                .collect(Collectors.toList());

        List<String> priorityAreas = raw.get("priority_areas") instanceof List
                ? (List<String>) raw.get("priority_areas") : List.of();

        return SuggestionsResponse.builder()
                .summary(str(raw.get("summary")))
                .priorityAreas(priorityAreas)
                .suggestions(items)
                .motivationalNote(str(raw.get("motivational_note")))
                .build();
    }

    @SuppressWarnings("unchecked")
    private RoadmapResponse mapRoadmapResponse(Map<String, Object> raw) {
        List<Map<String, Object>> rawWeeks = (List<Map<String, Object>>) raw.getOrDefault("weeks", List.of());
        List<RoadmapResponse.WeekPlan> weeks = rawWeeks.stream().map(w -> {
            Map<String, Object> wt = (Map<String, Object>) w.getOrDefault("weekly_targets", Map.of());
            return RoadmapResponse.WeekPlan.builder()
                    .weekNumber(w.get("week_number") instanceof Number ? ((Number) w.get("week_number")).intValue() : 0)
                    .theme(str(w.get("theme")))
                    .focusSubjects(w.get("focus_subjects") instanceof List ? (List<String>) w.get("focus_subjects") : List.of())
                    .dailyTasks(w.get("daily_tasks") instanceof List ? (List<String>) w.get("daily_tasks") : List.of())
                    .weeklyTargets(RoadmapResponse.WeeklyTargets.builder()
                            .attendance(str(wt.get("attendance")))
                            .assignmentsToComplete(str(wt.get("assignments_to_complete")))
                            .lmsSessions(str(wt.get("lms_sessions")))
                            .studyHours(str(wt.get("study_hours")))
                            .build())
                    .milestone(str(w.get("milestone")))
                    .build();
        }).collect(Collectors.toList());

        Map<String, Object> sm = (Map<String, Object>) raw.getOrDefault("success_metrics", Map.of());

        return RoadmapResponse.builder()
                .roadmapTitle(str(raw.get("roadmap_title")))
                .duration(str(raw.get("duration")))
                .overallGoal(str(raw.get("overall_goal")))
                .weeks(weeks)
                .successMetrics(RoadmapResponse.SuccessMetrics.builder()
                        .attendanceTarget(str(sm.get("attendance_target")))
                        .marksTarget(str(sm.get("marks_target")))
                        .assignmentTarget(str(sm.get("assignment_target")))
                        .lmsTarget(str(sm.get("lms_target")))
                        .build())
                .build();
    }

    private RoadmapResponse parseRoadmapJson(String json) throws Exception {
        Map<String, Object> raw = objectMapper.readValue(json, new TypeReference<>() {});
        return mapRoadmapResponse(raw);
    }

    private String str(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    private SuggestionsResponse buildFallbackSuggestions() {
        return SuggestionsResponse.builder()
                .summary("Unable to generate AI suggestions at this time.")
                .priorityAreas(List.of())
                .suggestions(List.of())
                .motivationalNote("Keep working hard — every effort counts!")
                .build();
    }

    private RoadmapResponse buildFallbackRoadmap() {
        return RoadmapResponse.builder()
                .roadmapTitle("Improvement Roadmap")
                .duration("4 weeks")
                .overallGoal("Unable to generate a personalized roadmap at this time. Please try again later.")
                .weeks(List.of())
                .successMetrics(RoadmapResponse.SuccessMetrics.builder()
                        .attendanceTarget("75%")
                        .marksTarget("60%")
                        .assignmentTarget("80%")
                        .lmsTarget("50%")
                        .build())
                .build();
    }
}
