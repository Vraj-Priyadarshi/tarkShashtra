package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.response.RiskScoreResponse;
import com.tarkshastra.app.dto.response.RiskTrendResponse;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.enums.RiskLabel;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskScoreService {

    private final RiskScoreRepository riskScoreRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final SubjectClassMappingRepository subjectClassMappingRepository;
    private final AggregationService aggregationService;
    private final MLServiceClient mlServiceClient;
    private final AlertService alertService;

    @Value("${app.risk-thresholds.low:35}")
    private double lowThreshold;

    @Value("${app.risk-thresholds.medium:55}")
    private double mediumThreshold;

    @Transactional
    public RiskScore computeSubjectRisk(UUID studentId, UUID subjectId) {
        Double attendance = aggregationService.getAttendanceScore(studentId, subjectId);
        Double marks = aggregationService.getMarksScore(studentId, subjectId);
        Double assignment = aggregationService.getAssignmentScore(studentId, subjectId);
        Double lms = aggregationService.getLmsScore(studentId, subjectId);

        // Default nulls to 0.0 to avoid NOT NULL constraint violations
        double attVal = attendance != null ? attendance : 0.0;
        double mkVal = marks != null ? marks : 0.0;
        double asgVal = assignment != null ? assignment : 0.0;
        double lmsVal = lms != null ? lms : 0.0;

        Double riskScore = mlServiceClient.predictRiskScore(attVal, mkVal, asgVal, lmsVal);
        RiskLabel label = classifyRisk(riskScore);

        // Mark previous as not latest
        riskScoreRepository.markPreviousAsNotLatest(studentId, subjectId);

        User student = studentProfileRepository.findByUserId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"))
                .getUser();

        Subject subject = new Subject();
        subject.setId(subjectId);

        RiskScore rs = RiskScore.builder()
                .student(student)
                .subject(subject)
                .attendanceScore(attVal)
                .marksScore(mkVal)
                .assignmentScore(asgVal)
                .lmsScore(lmsVal)
                .riskScore(riskScore)
                .riskLabel(label)
                .isLatest(true)
                .computedAt(LocalDateTime.now())
                .build();

        return riskScoreRepository.save(rs);
    }

    @Transactional
    public RiskScore computeOverallRisk(UUID studentId) {
        StudentProfile sp = studentProfileRepository.findByUserId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Get all subjects for this student's class
        List<SubjectClassMapping> mappings = subjectClassMappingRepository.findByClassEntityId(sp.getClassEntity().getId());

        double totalRisk = 0;
        int count = 0;
        double totalAtt = 0, totalMk = 0, totalAsg = 0, totalLms = 0;

        for (SubjectClassMapping mapping : mappings) {
            UUID subjectId = mapping.getSubject().getId();
            RiskScore subjectRisk = computeSubjectRisk(studentId, subjectId);
            totalRisk += subjectRisk.getRiskScore();
            totalAtt += subjectRisk.getAttendanceScore() != null ? subjectRisk.getAttendanceScore() : 0;
            totalMk += subjectRisk.getMarksScore() != null ? subjectRisk.getMarksScore() : 0;
            totalAsg += subjectRisk.getAssignmentScore() != null ? subjectRisk.getAssignmentScore() : 0;
            totalLms += subjectRisk.getLmsScore() != null ? subjectRisk.getLmsScore() : 0;
            count++;
        }

        double overallRisk = count > 0 ? totalRisk / count : 50.0;
        RiskLabel label = classifyRisk(overallRisk);

        // Get previous label for alert check
        RiskLabel previousLabel = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(studentId)
                .map(RiskScore::getRiskLabel)
                .orElse(RiskLabel.LOW);

        riskScoreRepository.markPreviousOverallAsNotLatest(studentId);

        RiskScore overall = RiskScore.builder()
                .student(sp.getUser())
                .subject(null) // null = overall
                .attendanceScore(count > 0 ? totalAtt / count : null)
                .marksScore(count > 0 ? totalMk / count : null)
                .assignmentScore(count > 0 ? totalAsg / count : null)
                .lmsScore(count > 0 ? totalLms / count : null)
                .riskScore(overallRisk)
                .riskLabel(label)
                .isLatest(true)
                .computedAt(LocalDateTime.now())
                .build();

        RiskScore saved = riskScoreRepository.save(overall);

        // Trigger alert if crossed to HIGH
        alertService.checkAndSendHighRiskAlert(studentId, previousLabel, label);

        return saved;
    }

    public RiskScoreResponse getRiskScoreResponse(UUID studentId) {
        StudentProfile sp = studentProfileRepository.findByUserId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        RiskScore overall = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(studentId)
                .orElse(null);

        List<RiskScoreResponse.SubjectRiskResponse> subjectRisks = new ArrayList<>();
        List<SubjectClassMapping> mappings = subjectClassMappingRepository.findByClassEntityId(sp.getClassEntity().getId());

        for (SubjectClassMapping mapping : mappings) {
            UUID subjectId = mapping.getSubject().getId();
            riskScoreRepository.findByStudentIdAndSubjectIdAndIsLatestTrue(studentId, subjectId)
                    .ifPresent(rs -> subjectRisks.add(RiskScoreResponse.SubjectRiskResponse.builder()
                            .subjectId(subjectId)
                            .subjectName(mapping.getSubject().getName())
                            .riskScore(rs.getRiskScore())
                            .riskLabel(rs.getRiskLabel())
                            .attendance(rs.getAttendanceScore())
                            .marks(rs.getMarksScore())
                            .assignment(rs.getAssignmentScore())
                            .lms(rs.getLmsScore())
                            .build()));
        }

        return RiskScoreResponse.builder()
                .studentId(studentId)
                .fullName(sp.getFullName())
                .riskScore(overall != null ? overall.getRiskScore() : null)
                .riskLabel(overall != null ? overall.getRiskLabel() : null)
                .attendanceScore(overall != null ? overall.getAttendanceScore() : null)
                .marksScore(overall != null ? overall.getMarksScore() : null)
                .assignmentScore(overall != null ? overall.getAssignmentScore() : null)
                .lmsScore(overall != null ? overall.getLmsScore() : null)
                .computedAt(overall != null ? overall.getComputedAt() : null)
                .subjectRisks(subjectRisks)
                .build();
    }

    public RiskTrendResponse getRiskTrend(UUID studentId) {
        List<RiskScore> data = riskScoreRepository.findByStudentIdAndSubjectIdIsNullOrderByComputedAtAsc(studentId);
        List<RiskTrendResponse.RiskTrendPoint> points = new ArrayList<>();

        for (RiskScore rs : data) {
            points.add(RiskTrendResponse.RiskTrendPoint.builder()
                    .date(rs.getComputedAt().toLocalDate())
                    .riskScore(rs.getRiskScore())
                    .riskLabel(rs.getRiskLabel())
                    .build());
        }

        return RiskTrendResponse.builder()
                .studentId(studentId)
                .dataPoints(points)
                .build();
    }

    public Page<RiskScore> getLatestRiskScoresByInstitute(UUID instituteId, Pageable pageable) {
        return riskScoreRepository.findByStudent_Institute_IdAndSubjectIdIsNullAndIsLatestTrue(instituteId, pageable);
    }

    @Transactional
    public void batchRecomputeAllStudents(UUID instituteId) {
        List<StudentProfile> students = studentProfileRepository.findByInstituteId(instituteId,
                Pageable.unpaged()).getContent();
        for (StudentProfile sp : students) {
            try {
                computeOverallRisk(sp.getUser().getId());
            } catch (Exception e) {
                log.error("Failed to recompute risk for student {}: {}", sp.getRollNumber(), e.getMessage());
            }
        }
        log.info("Batch risk recompute completed for institute {}: {} students", instituteId, students.size());
    }

    public com.tarkshastra.app.dto.response.WhatIfResponse computeWhatIf(
            com.tarkshastra.app.dto.request.WhatIfRequest request) {
        UUID studentId = request.getStudentId();

        // Get current risk
        RiskScore currentOverall = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(studentId)
                .orElse(null);

        Double currentScore = currentOverall != null ? currentOverall.getRiskScore() : null;
        RiskLabel currentLabel = currentOverall != null ? currentOverall.getRiskLabel() : null;

        // Compute hypothetical per-subject risks
        double totalRisk = 0;
        int count = 0;
        List<RiskScoreResponse.SubjectRiskResponse> predictions = new ArrayList<>();

        for (var entry : request.getHypotheticalSubjects()) {
            Double predicted = mlServiceClient.predictRiskScore(
                    entry.getAttendance(), entry.getMarks(), entry.getAssignment(), entry.getLms());
            RiskLabel label = classifyRisk(predicted);

            predictions.add(RiskScoreResponse.SubjectRiskResponse.builder()
                    .subjectId(entry.getSubjectId())
                    .riskScore(predicted)
                    .riskLabel(label)
                    .attendance(entry.getAttendance())
                    .marks(entry.getMarks())
                    .assignment(entry.getAssignment())
                    .lms(entry.getLms())
                    .build());
            totalRisk += predicted;
            count++;
        }

        double predictedOverall = count > 0 ? totalRisk / count : 50.0;
        RiskLabel predictedLabel = classifyRisk(predictedOverall);

        return com.tarkshastra.app.dto.response.WhatIfResponse.builder()
                .currentRiskScore(currentScore)
                .currentRiskLabel(currentLabel)
                .predictedRiskScore(predictedOverall)
                .predictedRiskLabel(predictedLabel)
                .subjectPredictions(predictions)
                .build();
    }

    private RiskLabel classifyRisk(Double score) {
        if (score == null) return RiskLabel.LOW;
        if (score <= lowThreshold) return RiskLabel.LOW;
        if (score <= mediumThreshold) return RiskLabel.MEDIUM;
        return RiskLabel.HIGH;
    }
}
