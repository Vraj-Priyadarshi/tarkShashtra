package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.response.*;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.enums.InterventionType;
import com.tarkshastra.app.enums.RiskLabel;
import com.tarkshastra.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoordinatorDashboardService {

    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final InterventionRepository interventionRepository;
    private final DepartmentRepository departmentRepository;

    public InstituteDashboardResponse getDashboard(UUID instituteId) {
        long totalStudents = studentProfileRepository.countByInstituteId(instituteId);
        long totalTeachers = teacherProfileRepository.countByInstituteId(instituteId);
        Long highRisk = riskScoreRepository.countByRiskLabelAndInstituteId(RiskLabel.HIGH, instituteId);
        Long mediumRisk = riskScoreRepository.countByRiskLabelAndInstituteId(RiskLabel.MEDIUM, instituteId);
        Long lowRisk = riskScoreRepository.countByRiskLabelAndInstituteId(RiskLabel.LOW, instituteId);
        Double avgRisk = riskScoreRepository.avgRiskScoreByInstituteId(instituteId);
        long totalInterventions = interventionRepository.countByStudent_Institute_Id(instituteId);

        // Department risk summaries
        List<Department> departments = departmentRepository.findByInstituteId(instituteId);
        List<InstituteDashboardResponse.DepartmentRiskSummary> deptSummaries = departments.stream()
                .map(dept -> {
                    List<StudentProfile> deptStudents = studentProfileRepository.findByDepartmentId(dept.getId());
                    long deptHigh = 0, deptMedium = 0, deptLow = 0;
                    for (StudentProfile sp : deptStudents) {
                        RiskScore risk = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(
                                sp.getUser().getId()).orElse(null);
                        if (risk != null) {
                            switch (risk.getRiskLabel()) {
                                case HIGH -> deptHigh++;
                                case MEDIUM -> deptMedium++;
                                case LOW -> deptLow++;
                            }
                        }
                    }
                    return InstituteDashboardResponse.DepartmentRiskSummary.builder()
                            .departmentId(dept.getId())
                            .departmentName(dept.getName())
                            .highRiskCount(deptHigh)
                            .mediumRiskCount(deptMedium)
                            .lowRiskCount(deptLow)
                            .build();
                }).collect(Collectors.toList());

        return InstituteDashboardResponse.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .highRiskCount(highRisk != null ? highRisk : 0)
                .mediumRiskCount(mediumRisk != null ? mediumRisk : 0)
                .lowRiskCount(lowRisk != null ? lowRisk : 0)
                .averageRiskScore(avgRisk)
                .totalInterventions(totalInterventions)
                .departmentRiskSummaries(deptSummaries)
                .build();
    }

    public Page<StudentProfileResponse> getStudentList(UUID instituteId, Pageable pageable) {
        return studentProfileRepository.findByInstituteId(instituteId, pageable)
                .map(this::toStudentResponse);
    }

    public List<InterventionEffectivenessResponse> getInterventionEffectiveness(UUID instituteId) {
        List<Intervention> interventions = interventionRepository.findByStudent_Institute_Id(
                instituteId, Pageable.unpaged()).getContent();

        Map<InterventionType, List<Intervention>> grouped = interventions.stream()
                .collect(Collectors.groupingBy(Intervention::getInterventionType));

        return grouped.entrySet().stream().map(entry -> {
            List<Intervention> list = entry.getValue();
            double avgPre = list.stream()
                    .filter(i -> i.getPreRiskScore() != null)
                    .mapToDouble(Intervention::getPreRiskScore).average().orElse(0);
            double avgPost = list.stream()
                    .filter(i -> i.getPostRiskScore() != null)
                    .mapToDouble(Intervention::getPostRiskScore).average().orElse(0);

            return InterventionEffectivenessResponse.builder()
                    .interventionType(entry.getKey())
                    .count(list.size())
                    .avgPreScore(avgPre)
                    .avgPostScore(avgPost)
                    .avgImprovement(avgPre - avgPost)
                    .build();
        }).collect(Collectors.toList());
    }

    private StudentProfileResponse toStudentResponse(StudentProfile sp) {
        RiskScore risk = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(
                sp.getUser().getId()).orElse(null);
        return StudentProfileResponse.builder()
                .id(sp.getId())
                .userId(sp.getUser().getId())
                .fullName(sp.getFullName())
                .email(sp.getUser().getEmail())
                .rollNumber(sp.getRollNumber())
                .departmentName(sp.getDepartment().getName())
                .className(sp.getClassEntity().getName())
                .semester(sp.getSemester())
                .mentorName(sp.getMentor() != null ? sp.getMentor().getFullName() : null)
                .mentorEmail(sp.getMentor() != null ? sp.getMentor().getEmail() : null)
                .riskScore(risk != null ? risk.getRiskScore() : null)
                .riskLabel(risk != null ? risk.getRiskLabel() : null)
                .active(sp.getUser().getIsActive())
                .build();
    }
}
