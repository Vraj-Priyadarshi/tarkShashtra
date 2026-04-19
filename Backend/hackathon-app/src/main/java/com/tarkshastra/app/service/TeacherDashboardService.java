package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.response.*;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.enums.RiskLabel;
import com.tarkshastra.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherDashboardService {

    private final SubjectTeacherMappingRepository subjectTeacherMappingRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final AggregationService aggregationService;

    public TeacherDashboardResponse getDashboard(UUID teacherId) {
        List<SubjectTeacherMapping> mappings = subjectTeacherMappingRepository.findByTeacherId(teacherId);

        List<TeacherDashboardResponse.SubjectSummary> subjects = mappings.stream()
                .map(m -> TeacherDashboardResponse.SubjectSummary.builder()
                        .subjectId(m.getSubject().getId())
                        .subjectName(m.getSubject().getName())
                        .subjectCode(m.getSubject().getCode())
                        .className(m.getClassEntity().getName())
                        .classId(m.getClassEntity().getId())
                        .build())
                .collect(Collectors.toList());

        // Count mentees at risk (mentees where this teacher is faculty mentor)
        List<StudentProfile> mentees = studentProfileRepository.findByMentorId(teacherId);
        int menteesAtRisk = 0;
        for (StudentProfile sp : mentees) {
            RiskScore risk = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(
                    sp.getUser().getId()).orElse(null);
            if (risk != null && risk.getRiskLabel() == RiskLabel.HIGH) {
                menteesAtRisk++;
            }
        }

        // Upcoming exam alert
        TeacherDashboardResponse.ExamAlertInfo examAlert = null;
        for (SubjectTeacherMapping m : mappings) {
            List<ExamSchedule> exams = examScheduleRepository.findByClassEntityIdAndExamDateAfter(
                    m.getClassEntity().getId(), LocalDate.now());
            for (ExamSchedule exam : exams) {
                long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), exam.getExamDate());
                if (daysUntil <= 14) {
                    int highRiskCount = countHighRiskInClass(m.getClassEntity().getId());
                    examAlert = TeacherDashboardResponse.ExamAlertInfo.builder()
                            .subjectName(exam.getSubject().getName())
                            .examDate(exam.getExamDate())
                            .daysUntilExam(daysUntil)
                            .highRiskMenteeCount(highRiskCount)
                            .build();
                    break;
                }
            }
            if (examAlert != null) break;
        }

        return TeacherDashboardResponse.builder()
                .subjects(subjects)
                .menteesAtRisk(menteesAtRisk)
                .pendingDataEntryCount(0)
                .upcomingExamAlert(examAlert)
                .build();
    }

    public SubjectAnalyticsResponse getSubjectAnalytics(UUID subjectId, UUID classId) {
        List<StudentProfile> students = studentProfileRepository.findByClassEntityId(classId);
        if (students.isEmpty()) {
            return SubjectAnalyticsResponse.builder()
                    .totalStudents(0)
                    .atRiskCount(0)
                    .build();
        }

        double totalAtt = 0, totalMarks = 0, totalAsg = 0, totalLms = 0;
        int count = 0;
        List<StudentProfileResponse> atRiskStudents = new ArrayList<>();

        for (StudentProfile sp : students) {
            UUID sid = sp.getUser().getId();
            Double att = aggregationService.getAttendanceScore(sid, subjectId);
            Double marks = aggregationService.getMarksScore(sid, subjectId);
            Double asg = aggregationService.getAssignmentScore(sid, subjectId);
            Double lms = aggregationService.getLmsScore(sid, subjectId);

            totalAtt += att != null ? att : 0;
            totalMarks += marks != null ? marks : 0;
            totalAsg += asg != null ? asg : 0;
            totalLms += lms != null ? lms : 0;
            count++;

            RiskScore risk = riskScoreRepository.findByStudentIdAndSubjectIdAndIsLatestTrue(sid, subjectId)
                    .orElse(null);
            if (risk != null && risk.getRiskLabel() == RiskLabel.HIGH) {
                atRiskStudents.add(toStudentResponse(sp, risk));
            }
        }

        return SubjectAnalyticsResponse.builder()
                .classAvgAttendance(count > 0 ? totalAtt / count : 0)
                .classAvgMarks(count > 0 ? totalMarks / count : 0)
                .classAvgAssignment(count > 0 ? totalAsg / count : 0)
                .classAvgLms(count > 0 ? totalLms / count : 0)
                .totalStudents(count)
                .atRiskCount(atRiskStudents.size())
                .atRiskStudents(atRiskStudents)
                .build();
    }

    private int countHighRiskInClass(UUID classId) {
        List<StudentProfile> students = studentProfileRepository.findByClassEntityId(classId);
        int count = 0;
        for (StudentProfile sp : students) {
            RiskScore risk = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(
                    sp.getUser().getId()).orElse(null);
            if (risk != null && risk.getRiskLabel() == RiskLabel.HIGH) count++;
        }
        return count;
    }

    private StudentProfileResponse toStudentResponse(StudentProfile sp, RiskScore risk) {
        return StudentProfileResponse.builder()
                .id(sp.getId())
                .userId(sp.getUser().getId())
                .fullName(sp.getFullName())
                .email(sp.getUser().getEmail())
                .rollNumber(sp.getRollNumber())
                .departmentName(sp.getDepartment().getName())
                .className(sp.getClassEntity().getName())
                .semester(sp.getSemester())
                .riskScore(risk != null ? risk.getRiskScore() : null)
                .riskLabel(risk != null ? risk.getRiskLabel() : null)
                .active(sp.getUser().getIsActive())
                .build();
    }

    public List<StudentProfileResponse> getStudentsByClass(UUID classId) {
        List<StudentProfile> students = studentProfileRepository.findByClassEntityId(classId);
        return students.stream()
                .map(sp -> toStudentResponse(sp, null))
                .collect(Collectors.toList());
    }
}
