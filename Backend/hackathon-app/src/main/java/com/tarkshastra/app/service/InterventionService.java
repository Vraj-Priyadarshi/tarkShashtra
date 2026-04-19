package com.tarkshastra.app.service;

import com.tarkshastra.app.dto.request.CreateInterventionRequest;
import com.tarkshastra.app.dto.response.InterventionResponse;
import com.tarkshastra.app.entity.*;
import com.tarkshastra.app.enums.ActionItemStatus;
import com.tarkshastra.app.exception.ResourceNotFoundException;
import com.tarkshastra.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final ActionItemRepository actionItemRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final RiskScoreRepository riskScoreRepository;

    @Transactional
    public Intervention createIntervention(CreateInterventionRequest request, User mentor) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Get current risk score
        Double preRiskScore = riskScoreRepository.findByStudentIdAndSubjectIdIsNullAndIsLatestTrue(student.getId())
                .map(RiskScore::getRiskScore)
                .orElse(null);

        Intervention intervention = Intervention.builder()
                .student(student)
                .mentor(mentor)
                .interventionType(request.getInterventionType())
                .interventionDate(request.getInterventionDate())
                .remarks(request.getRemarks())
                .followUpDate(request.getFollowUpDate())
                .preRiskScore(preRiskScore)
                .actionItems(new ArrayList<>())
                .build();

        intervention = interventionRepository.save(intervention);

        // Create action items
        if (request.getActionItems() != null) {
            for (String desc : request.getActionItems()) {
                ActionItem item = ActionItem.builder()
                        .intervention(intervention)
                        .description(desc)
                        .status(ActionItemStatus.PENDING)
                        .build();
                intervention.getActionItems().add(actionItemRepository.save(item));
            }
        }

        return intervention;
    }

    public List<InterventionResponse> getInterventionsForStudent(UUID studentId) {
        return interventionRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<InterventionResponse> getInterventionsForMentor(UUID mentorId) {
        return interventionRepository.findByMentorId(mentorId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void completeActionItem(UUID actionItemId) {
        ActionItem item = actionItemRepository.findById(actionItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Action item not found"));
        item.setStatus(ActionItemStatus.COMPLETED);
        actionItemRepository.save(item);
    }

    private InterventionResponse toResponse(Intervention i) {
        String studentName = studentProfileRepository.findByUserId(i.getStudent().getId())
                .map(sp -> sp.getFullName())
                .orElse(i.getStudent().getEmail());

        Double scoreChange = null;
        if (i.getPreRiskScore() != null && i.getPostRiskScore() != null) {
            scoreChange = i.getPreRiskScore() - i.getPostRiskScore();
        }

        return InterventionResponse.builder()
                .id(i.getId())
                .studentName(studentName)
                .studentId(i.getStudent().getId())
                .mentorName(i.getMentor().getFullName())
                .interventionType(i.getInterventionType())
                .interventionDate(i.getInterventionDate())
                .remarks(i.getRemarks())
                .followUpDate(i.getFollowUpDate())
                .preRiskScore(i.getPreRiskScore())
                .postRiskScore(i.getPostRiskScore())
                .scoreChange(scoreChange)
                .actionItems(i.getActionItems().stream()
                        .map(a -> InterventionResponse.ActionItemResponse.builder()
                                .id(a.getId())
                                .description(a.getDescription())
                                .status(a.getStatus())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(i.getCreatedAt())
                .build();
    }
}
