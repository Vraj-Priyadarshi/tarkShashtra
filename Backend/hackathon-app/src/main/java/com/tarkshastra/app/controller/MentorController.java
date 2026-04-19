package com.tarkshastra.app.controller;

import com.tarkshastra.app.dto.request.CreateInterventionRequest;
import com.tarkshastra.app.dto.request.FlagStudentRequest;
import com.tarkshastra.app.dto.response.*;
import com.tarkshastra.app.entity.Intervention;
import com.tarkshastra.app.entity.RiskScore;
import com.tarkshastra.app.entity.StudentFlag;
import com.tarkshastra.app.entity.StudentProfile;
import com.tarkshastra.app.entity.User;
import com.tarkshastra.app.enums.RiskLabel;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.RiskScoreRepository;
import com.tarkshastra.app.repository.StudentProfileRepository;
import com.tarkshastra.app.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
public class MentorController {

    private final InterventionService interventionService;
    private final StudentFlagService studentFlagService;
    private final RiskScoreService riskScoreService;
    private final StudentProfileRepository studentProfileRepository;
    private final RiskScoreRepository riskScoreRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<MentorDashboardResponse> getDashboard(@AuthenticationPrincipal User mentor) {
        List<StudentProfile> mentees = studentProfileRepository.findByMentorId(mentor.getId());
        int high = 0, medium = 0, low = 0;
        for (StudentProfile sp : mentees) {
            RiskScore risk = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(
                    sp.getUser().getId()).orElse(null);
            if (risk != null) {
                switch (risk.getRiskLabel()) {
                    case HIGH -> high++;
                    case MEDIUM -> medium++;
                    case LOW -> low++;
                }
            }
        }
        List<StudentFlagResponse> flags = studentFlagService.getUnresolvedFlagsForMentor(mentor.getId());
        List<InterventionResponse> interventions = interventionService.getInterventionsForMentor(mentor.getId());
        long pendingFollowUps = interventions.stream()
                .filter(i -> i.getFollowUpDate() != null && i.getPostRiskScore() == null)
                .count();

        return ResponseEntity.ok(MentorDashboardResponse.builder()
                .totalMentees(mentees.size())
                .highRiskMentees(high)
                .mediumRiskMentees(medium)
                .lowRiskMentees(low)
                .unresolvedFlags(flags.size())
                .pendingFollowUps((int) pendingFollowUps)
                .menteeSummary(mentees.stream().map(this::toStudentResponse).collect(Collectors.toList()))
                .build());
    }

    @GetMapping("/mentees")
    public ResponseEntity<List<StudentProfileResponse>> getMentees(@AuthenticationPrincipal User mentor) {
        List<StudentProfile> mentees = studentProfileRepository.findByMentorId(mentor.getId());
        return ResponseEntity.ok(mentees.stream().map(this::toStudentResponse).collect(Collectors.toList()));
    }

    @GetMapping("/mentees/{studentId}/risk")
    public ResponseEntity<RiskScoreResponse> getMenteeRisk(@PathVariable UUID studentId) {
        return ResponseEntity.ok(riskScoreService.getRiskScoreResponse(studentId));
    }

    @GetMapping("/mentees/{studentId}/risk-trend")
    public ResponseEntity<RiskTrendResponse> getMenteeRiskTrend(@PathVariable UUID studentId) {
        return ResponseEntity.ok(riskScoreService.getRiskTrend(studentId));
    }

    @PostMapping("/interventions")
    public ResponseEntity<InterventionResponse> createIntervention(
            @AuthenticationPrincipal User mentor,
            @Valid @RequestBody CreateInterventionRequest request) {
        Intervention intervention = interventionService.createIntervention(request, mentor);
        List<InterventionResponse> responses = interventionService.getInterventionsForStudent(request.getStudentId());
        return ResponseEntity.ok(responses.get(0));
    }

    @GetMapping("/interventions")
    public ResponseEntity<List<InterventionResponse>> getMyInterventions(@AuthenticationPrincipal User mentor) {
        return ResponseEntity.ok(interventionService.getInterventionsForMentor(mentor.getId()));
    }

    @PutMapping("/interventions/action-items/{actionItemId}/complete")
    public ResponseEntity<MessageResponse> completeActionItem(@PathVariable UUID actionItemId) {
        interventionService.completeActionItem(actionItemId);
        return ResponseEntity.ok(new MessageResponse("Action item completed"));
    }

    @GetMapping("/flags")
    public ResponseEntity<List<StudentFlagResponse>> getMyFlags(@AuthenticationPrincipal User mentor) {
        return ResponseEntity.ok(studentFlagService.getUnresolvedFlagsForMentor(mentor.getId()));
    }

    @PutMapping("/flags/{flagId}/resolve")
    public ResponseEntity<MessageResponse> resolveFlag(@PathVariable UUID flagId) {
        studentFlagService.resolveFlag(flagId);
        return ResponseEntity.ok(new MessageResponse("Flag resolved"));
    }

    @PostMapping("/compute-risk/{studentId}")
    public ResponseEntity<RiskScoreResponse> computeStudentRisk(@PathVariable UUID studentId) {
        riskScoreService.computeOverallRisk(studentId);
        return ResponseEntity.ok(riskScoreService.getRiskScoreResponse(studentId));
    }

    private StudentProfileResponse toStudentResponse(StudentProfile sp) {
        var riskResp = riskScoreService.getRiskScoreResponse(sp.getUser().getId());
        return StudentProfileResponse.builder()
                .id(sp.getId())
                .userId(sp.getUser().getId())
                .fullName(sp.getFullName())
                .email(sp.getUser().getEmail())
                .rollNumber(sp.getRollNumber())
                .departmentName(sp.getDepartment().getName())
                .className(sp.getClassEntity().getName())
                .semester(sp.getSemester())
                .riskScore(riskResp.getRiskScore())
                .riskLabel(riskResp.getRiskLabel())
                .active(sp.getUser().getIsActive())
                .build();
    }
}
