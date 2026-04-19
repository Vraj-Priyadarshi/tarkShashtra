package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.response.*;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final StudentProfileRepository studentProfileRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final SubjectClassMappingRepository subjectClassMappingRepository;
    private final AggregationService aggregationService;
    private final InterventionRepository interventionRepository;
    private final ConsistencyStreakService consistencyStreakService;

    public StudentDashboardResponse getDashboard(UUID studentId) {
        StudentProfile sp = studentProfileRepository.findByUserId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        RiskScore overall = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(studentId)
                .orElse(null);

        // Top contributing factors
        List<StudentDashboardResponse.RiskFactor> factors = new ArrayList<>();
        if (overall != null) {
            if (overall.getAttendanceScore() != null)
                factors.add(StudentDashboardResponse.RiskFactor.builder()
                        .factor("Attendance").value(overall.getAttendanceScore()).build());
            if (overall.getMarksScore() != null)
                factors.add(StudentDashboardResponse.RiskFactor.builder()
                        .factor("IA Marks").value(overall.getMarksScore()).build());
            if (overall.getAssignmentScore() != null)
                factors.add(StudentDashboardResponse.RiskFactor.builder()
                        .factor("Assignments").value(overall.getAssignmentScore()).build());
            if (overall.getLmsScore() != null)
                factors.add(StudentDashboardResponse.RiskFactor.builder()
                        .factor("LMS Activity").value(overall.getLmsScore()).build());

            // Sort by lowest value (worst factor first)
            factors.sort(Comparator.comparingDouble(f -> f.getValue() != null ? f.getValue() : 100.0));
        }

        // Improvement tips
        List<String> tips = generateImprovementTips(overall);

        ConsistencyStreakResponse streak = consistencyStreakService.getStreak(studentId);

        return StudentDashboardResponse.builder()
                .fullName(sp.getFullName())
                .rollNumber(sp.getRollNumber())
                .semester(sp.getSemester())
                .branch(sp.getDepartment().getName())
                .riskScore(overall != null ? overall.getRiskScore() : null)
                .riskLabel(overall != null ? overall.getRiskLabel() : null)
                .topContributingFactors(factors)
                .improvementTips(tips)
                .mentorName(sp.getMentor() != null ? sp.getMentor().getFullName() : null)
                .mentorEmail(sp.getMentor() != null ? sp.getMentor().getEmail() : null)
                .consistencyStreak(streak)
                .build();
    }

    public StudentAcademicDetailResponse getAcademicData(UUID studentId) {
        StudentProfile sp = studentProfileRepository.findByUserId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<SubjectClassMapping> mappings = subjectClassMappingRepository.findByClassEntityId(
                sp.getClassEntity().getId());

        List<StudentAcademicDetailResponse.SubjectAcademicDataResponse> subjectData = mappings.stream()
                .map(m -> {
                    UUID subjectId = m.getSubject().getId();
                    return StudentAcademicDetailResponse.SubjectAcademicDataResponse.builder()
                            .subjectId(subjectId)
                            .subjectName(m.getSubject().getName())
                            .attendancePercentage(aggregationService.getAttendanceScore(studentId, subjectId))
                            .iaMarksNormalized(aggregationService.getMarksScore(studentId, subjectId))
                            .assignmentCompletionPercentage(aggregationService.getAssignmentScore(studentId, subjectId))
                            .lmsScore(aggregationService.getLmsScore(studentId, subjectId))
                            .build();
                }).collect(Collectors.toList());

        // Compute overall averages from subject data
        Double overallAttendance = subjectData.stream()
                .map(StudentAcademicDetailResponse.SubjectAcademicDataResponse::getAttendancePercentage)
                .filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().orElse(0.0);
        Double overallMarks = subjectData.stream()
                .map(StudentAcademicDetailResponse.SubjectAcademicDataResponse::getIaMarksNormalized)
                .filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().orElse(0.0);
        Double overallAssignment = subjectData.stream()
                .map(StudentAcademicDetailResponse.SubjectAcademicDataResponse::getAssignmentCompletionPercentage)
                .filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().orElse(0.0);
        Double overallLms = subjectData.stream()
                .map(StudentAcademicDetailResponse.SubjectAcademicDataResponse::getLmsScore)
                .filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().orElse(0.0);

        // Get overall risk score
        RiskScore overallRisk = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(studentId)
                .orElse(null);

        return StudentAcademicDetailResponse.builder()
                .studentId(studentId)
                .fullName(sp.getFullName())
                .rollNumber(sp.getRollNumber())
                .overallAttendance(overallAttendance)
                .overallMarks(overallMarks)
                .overallAssignment(overallAssignment)
                .overallLms(overallLms)
                .overallRiskScore(overallRisk != null ? overallRisk.getRiskScore() : null)
                .overallRiskLabel(overallRisk != null ? String.valueOf(overallRisk.getRiskLabel()) : null)
                .subjects(subjectData)
                .build();
    }

    public List<InterventionResponse> getInterventionHistory(UUID studentId) {
        List<Intervention> interventions = interventionRepository.findByStudentId(studentId);
        return interventions.stream().map(i -> InterventionResponse.builder()
                .id(i.getId())
                .studentId(i.getStudent().getId())
                .studentName(i.getStudent().getFullName())
                .mentorName(i.getMentor().getFullName())
                .interventionType(i.getInterventionType())
                .interventionDate(i.getInterventionDate())
                .remarks(i.getRemarks())
                .followUpDate(i.getFollowUpDate())
                .preRiskScore(i.getPreRiskScore())
                .postRiskScore(i.getPostRiskScore())
                .actionItems(i.getActionItems().stream()
                        .map(ai -> InterventionResponse.ActionItemResponse.builder()
                                .id(ai.getId())
                                .description(ai.getDescription())
                                .status(ai.getStatus())
                                .build())
                        .collect(Collectors.toList()))
                .build()).collect(Collectors.toList());
    }

    private List<String> generateImprovementTips(RiskScore overall) {
        List<String> tips = new ArrayList<>();
        if (overall == null) return tips;

        if (overall.getAttendanceScore() != null && overall.getAttendanceScore() < 75)
            tips.add("Your attendance is below 75%. Try to attend all classes regularly.");
        if (overall.getMarksScore() != null && overall.getMarksScore() < 50)
            tips.add("Your IA marks are below average. Consider seeking help from your teachers or mentor.");
        if (overall.getAssignmentScore() != null && overall.getAssignmentScore() < 80)
            tips.add("Your assignment completion rate could improve. Submit all assignments on time.");
        if (overall.getLmsScore() != null && overall.getLmsScore() < 50)
            tips.add("Engage more with the LMS platform — review study materials and complete online activities.");
        if (tips.isEmpty())
            tips.add("Great job! Keep maintaining your academic performance.");

        return tips;
    }
}
